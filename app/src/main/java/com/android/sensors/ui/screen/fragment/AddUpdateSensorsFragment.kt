package com.android.sensors.ui.screen.fragment

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.sensors.R
import com.android.sensors.databinding.FragmentAddUpdateSensorsBinding
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.Const
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.safeNavigate
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.concurrent.ExecutionException


@AndroidEntryPoint
class AddUpdateSensorFragment : BaseFragment() {

    private var _binding: FragmentAddUpdateSensorsBinding? = null
    private val binding: FragmentAddUpdateSensorsBinding get() = _binding!!
    private var sensor: SensorsModel? = null
    private var uri: Uri? = null
    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>
    private val df = DecimalFormat("#.###")

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddUpdateSensorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (bundle == null) {
            addSensor()
        } else {
            val args = SensorsDetailsFragmentArgs.fromBundle(bundle)
            sensor = args.sensorsModel!!

            Timber.d("$TAG, $sensor")
            updateSensor(sensor!!)
        }

        binding.addImage.setOnClickListener {
            if (isStoragePermissionGranted()) {
                cropActivityResultLauncher.launch(null)
            }
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract) { uri ->
            if (uri != null) {
                this.uri = uri
                loadImage(uri, binding.sensorImage)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sensor_add_update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if(sensor != null) {
                val directions =
                    AddUpdateSensorFragmentDirections.actionAddUpdateSensorFragmentToSensorsDetailsFragment(sensor!!)
                findNavController().safeNavigate(directions)
            } else {
                navigateToSensorsFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addSensor() {
        (activity as AppCompatActivity).supportActionBar?.title = "Add Sensor"
        binding.buttonClick.text = "Add sensor"
        checkBeforePosting()
    }

    private fun checkBeforePosting() {
        binding.buttonClick.setOnClickListener {
            if (!binding.title.text.isNullOrEmpty() && !binding.description.text.isNullOrEmpty() &&
                !binding.source.text.isNullOrEmpty() && binding.sensorImage.drawable != null) {

                if(uri != null) {
                    withUri(uri!!, null)
                }
                else {
                    Timber.d("Uri is null")
                    Toast.makeText(getActivity, "Uri is null", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(getActivity, "Please fill in title, description, source and image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun withUri(uri: Uri, sensor: SensorsModel?) {
        val file = toMultipartBody(createTemporaryFile(uri))
        val imageBytes = File(uri.path!!).length()
        Timber.d("File size is  $imageBytes")

        if(imageBytes < Const.ONE_MEGA_BYTE){
            if(file != null) {
                if(sensor == null)
                    postSensor(file)
                else
                    updatingSensor(sensor, file)
            } else {
                Timber.d("MultipartBody.Part file is null")
                Toast.makeText(getActivity, "MultipartBody.Part file is null", Toast.LENGTH_SHORT).show()
            }
        } else {
            val imageMb = df.format((imageBytes / Const.ONE_MEGA_BYTE))
            Toast.makeText(getActivity, "Image size (${imageMb}Mb) should not be greater than 1Mb.", Toast.LENGTH_LONG).show()
        }
    }

    private fun createTemporaryFile(uri: Uri): File? {
        val inputStream  = context?.contentResolver?.openInputStream(uri)
        val storageDir = getActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile(System.currentTimeMillis().toString(), ".jpg", storageDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    private fun postSensor(file: MultipartBody.Part) {
        showProgressBar(binding.progressBar)

        lifecycleScope.launch {
            viewModel.setCreateSensor(
                toRequestBody(binding.title.text.toString().trim()),
                toRequestBody(binding.description.text.toString().trim()),
                toRequestBody(binding.source.text.toString().trim()),
                toRequestBody(binding.moreInfo.text.toString().trim()),
                file
            )

            viewModel.createSensor.observe(viewLifecycleOwner, Observer {

                when(it) {
                    is Resource.Loading -> {
                        Timber.d("loading")
                    }
                    is Resource.Success -> {
                        viewModel.stopLoading()
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(getActivity, it.data.title + " added", Toast.LENGTH_SHORT).show()
                            navigateToSensorsFragment()
                        })
                    }
                    is Resource.Error -> {
                        viewModel.stopLoading()
                        if(it.errorData.isNotEmpty())
                            Toast.makeText(getActivity, it.errorData, Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }
    }

    private fun updateSensor(sensor: SensorsModel) {
        (activity as AppCompatActivity).supportActionBar?.title = "Update Sensor"
        binding.buttonClick.text = "Update sensor"
        binding.title.setText(sensor.title)
        binding.description.setText(sensor.description)
        binding.source.setText(sensor.source)
        binding.moreInfo.setText(sensor.moreInfo)
        loadImage(sensor.imageUrl!!, binding.sensorImage)
        checkBeforeUpdating(sensor)
    }

    private fun checkBeforeUpdating(sensor: SensorsModel) {
        binding.buttonClick.setOnClickListener {
            if (!binding.title.text.isNullOrEmpty() && !binding.description.text.isNullOrEmpty() &&
                !binding.source.text.isNullOrEmpty() && binding.sensorImage.drawable != null) {

                if(uri != null) {
                    withUri(uri!!, sensor)
                } else { //Get the previous image file from network
                    CoroutineScope(IO).launch {
                        try{
                            val cachedFile: File = Glide.with(getActivity).asFile().load(sensor.imageUrl).submit().get()
                            withContext(Dispatchers.Main) {
                                val file = toMultipartBody(cachedFile)

                                if(file != null) {
                                    val imageBytes = cachedFile.length()
                                    Timber.d("File size is  $imageBytes")

                                    if(imageBytes < Const.ONE_MEGA_BYTE){
                                        updatingSensor(sensor, file)
                                    } else {
                                        val imageMb = df.format((imageBytes / Const.ONE_MEGA_BYTE))
                                        Toast.makeText(getActivity, "Image size (${imageMb}Mb) should not be greater than 1Mb.", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Timber.d("MultipartBody.Part file is null")
                                    Toast.makeText(getActivity, "MultipartBody.Part file is null", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: ExecutionException) {
                            Timber.d(e.localizedMessage)
                            withContext(Dispatchers.Main) {
                                viewModel.stopLoading()
                                Toast.makeText(getActivity, "Failed to get image file, check network and try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(getActivity, "Please fill in title, description, source and image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatingSensor(sensor: SensorsModel, file: MultipartBody.Part) {
        showProgressBar(binding.progressBar)

        lifecycleScope.launch {
            viewModel.setUpdateSensor(
                sensor.id!!,
                toRequestBody(binding.title.text.toString().trim()),
                toRequestBody(binding.description.text.toString().trim()),
                toRequestBody(binding.source.text.toString().trim()),
                toRequestBody(binding.moreInfo.text.toString().trim()),
                file
            )

            viewModel.updateSensor.observe(getActivity, Observer {

                when(it) {
                    is Resource.Loading -> {
                        Timber.d("loading")
                    }
                    is Resource.Success -> {
                        viewModel.stopLoading()
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Toast.makeText(getActivity, it.data.title + " updated", Toast.LENGTH_SHORT).show()
                            navigateToSensorsFragment()
                        })
                    }
                    is Resource.Error -> {
                        viewModel.stopLoading()
                        if(it.errorData.isNotEmpty())
                            Toast.makeText(getActivity, it.errorData, Toast.LENGTH_SHORT).show()
                    }
                }
            })

        }
    }

    private fun navigateToSensorsFragment() {
        lifecycleScope.launchWhenResumed {
            val directions =
                AddUpdateSensorFragmentDirections.actionAddUpdateSensorFragmentToSensorsFragment()
            findNavController().safeNavigate(directions)
        }
    }

    private fun toRequestBody(string: String): RequestBody {
        return string.toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    private fun toMultipartBody(file: File?): MultipartBody.Part? {
        Timber.d("$TAG, File is: $file")

        if(file != null) {
            Timber.d("File size is  ${file.length()}")

            if (file.exists()) {
                Timber.d("$TAG, file is: $file")
                return MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                )
            }
        }

        return null
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (
            ContextCompat.checkSelfPermission(
                getActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                getActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                getActivity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                Const.PERMISSIONS_REQUEST_READ_STORAGE
            )

            return false
        } else {
            // Permission has already been granted
            return true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}