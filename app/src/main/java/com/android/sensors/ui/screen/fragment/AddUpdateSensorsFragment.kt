package com.android.sensors.ui.screen.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
                findNavController().navigate(directions)
            } else {
                findNavController().navigate(R.id.action_addUpdateSensorFragment_to_sensorsFragment)
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
                    val file = toMultipartBody(uri!!, null)
                    val imageBytes = File(uri!!.path!!).length()
                    Timber.d("File size is  $imageBytes")

                    if(imageBytes < Const.ONE_MEGA_BYTE){
                        if(file != null) {
                            postSensor(file)
                        } else {
                            Timber.d("MultipartBody.Part file is null")
                            Toast.makeText(getActivity, "MultipartBody.Part file is null", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val imageMb = df.format((imageBytes / Const.ONE_MEGA_BYTE))
                        Toast.makeText(getActivity, "Image size (${imageMb}Mb) should not be greater than 1Mb.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Timber.d("Uri is null")
                    Toast.makeText(getActivity, "Uri is null", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(getActivity, "Please fill in title, description, source and image.", Toast.LENGTH_SHORT).show()
            }
        }
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
                            findNavController().navigate(R.id.action_addUpdateSensorFragment_to_sensorsFragment)
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
                    val file = toMultipartBody(uri!!, null)

                    if(file != null) {
                        val imageBytes = File(uri!!.path!!).length()
                        Timber.d("File size is  $imageBytes")

                        if(imageBytes < Const.ONE_MEGA_BYTE){
                            updatingSensor(sensor, file)
                        } else {
                            val imageMb =  df.format((imageBytes / Const.ONE_MEGA_BYTE))
                            Toast.makeText(getActivity, "Image size (${imageMb}Mb) should not be greater than 1Mb.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Timber.d("MultipartBody.Part file is null")
                        Toast.makeText(getActivity, "MultipartBody.Part file is null", Toast.LENGTH_SHORT).show()
                    }
                } else { //Get the previous image file from network
                    CoroutineScope(IO).launch {
                        try{
                            val cachedFile: File = Glide.with(getActivity).asFile().load(sensor.imageUrl).submit().get()
                            withContext(Dispatchers.Main) {
                                val file = toMultipartBody(null, cachedFile)

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
                            findNavController().navigate(R.id.action_addUpdateSensorFragment_to_sensorsFragment)
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

    private fun toRequestBody(string: String): RequestBody {
        return string.toRequestBody("multipart/form-data".toMediaTypeOrNull())
    }

    private fun toMultipartBody(uri: Uri?, file: File?): MultipartBody.Part? {
        Timber.d("$TAG, Uri is: $uri")

        if (uri != null) { //use uri

            uri.path?.let { filePath ->
                val imageFile = File(filePath)
                Timber.d("File size is  ${imageFile.length()}")

                if (imageFile.exists()) {
                    Timber.d("$TAG, file is: $imageFile")
                    return getMultipartBodyPart(imageFile)
                }
            }

        } else { //use file
            if(file != null) {
                Timber.d("File size is  ${file.length()}")

                if (file.exists()) {
                    Timber.d("$TAG, file is: $file")
                    return getMultipartBodyPart(file)
                }
            }
        }

        return null
    }

    private fun getMultipartBodyPart(file: File): MultipartBody.Part {
        val multipartBody: MultipartBody.Part?

        val requestBody =
            file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        multipartBody = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestBody
        )

        return multipartBody
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