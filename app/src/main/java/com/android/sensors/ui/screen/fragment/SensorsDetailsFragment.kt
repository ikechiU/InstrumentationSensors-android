package com.android.sensors.ui.screen.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.android.sensors.R
import com.android.sensors.databinding.FragmentSensorsDetailsBinding
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.calladapter.flow.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SensorsDetailsFragment : BaseFragment() {

    private var _binding: FragmentSensorsDetailsBinding? = null
    private val binding: FragmentSensorsDetailsBinding get() = _binding!!
    private lateinit var sensor: SensorsModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSensorsDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (bundle == null) {
            Timber.e("$TAG, SensorDetails did not receive any SensorModel data")
            return
        }

        val args = SensorsDetailsFragmentArgs.fromBundle(bundle)
        sensor = args.sensorsModel!!

        Timber.d("$TAG, $sensor")

        binding.detailsTitle.text = sensor.title
        binding.detailsDescription.text = sensor.description

        binding.detailsSource.text = sensor.source
        binding.detailsSource.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.detailsMoreInfo.text = sensor.moreInfo
        binding.detailsMoreInfo.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        binding.detailsSource.setOnClickListener {
            visitUrl(getActivity, binding.detailsSource.text.toString())
        }

        binding.detailsMoreInfo.setOnClickListener {
            visitUrl(getActivity, binding.detailsMoreInfo.text.toString())
        }

        loadImage(sensor.imageUrl!!, binding.image)

        binding.cardContainer.setOnLongClickListener {
            longClick()
            true
        }

    }

    private fun longClick() {
        val builder = AlertDialog.Builder(getActivity)
        builder.setCancelable(false)
        builder.setMessage("Do you want to delete this sensor from network?")
        builder.setPositiveButton(
            "Yes"
        ) { _, _ ->
            if (isInternetAvailable(getActivity)) {
                deleteFromNetwork()
            } else {
                Toast.makeText(getActivity, "No internet", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(
            "No"
        ) { dialog, _ ->
            dialog.cancel()
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun deleteFromNetwork() {
        showProgressBar(binding.progressBar)
        viewModel.setDeleteSensor(sensor.id!!)

        viewModel.deleteSensorRemote.observe(viewLifecycleOwner, {operationStatus ->
            if (operationStatus is Resource.Success) {
                Toast.makeText(getActivity, "Delete action: " + operationStatus.data.operationResult, Toast.LENGTH_SHORT).show()
                viewModel.stopLoading()
                findNavController().navigate(R.id.action_sensorsDetailsFragment_to_sensorsFragment)
            } else if (operationStatus is Resource.Error) {
                Toast.makeText(getActivity, operationStatus.errorData, Toast.LENGTH_SHORT).show();
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sensor_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.delete) {
            val builder = AlertDialog.Builder(getActivity)
            builder.setCancelable(false)
            builder.setMessage("Do you want to delete this sensor from database?")
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
                sensor.id?.let { viewModel.deleteSensorLocal(it) }
                Toast.makeText(getActivity, "${sensor.title} deleted successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_sensorsDetailsFragment_to_sensorsFragment)
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, _ ->
                dialog.cancel()
            }
            val alert: AlertDialog = builder.create()
            alert.show()

        }

        if (id == android.R.id.home) {
            findNavController().navigate(R.id.action_sensorsDetailsFragment_to_sensorsFragment)
        }

        if (id == R.id.update) {
            val directions =
                SensorsDetailsFragmentDirections.actionSensorsDetailsFragmentToAddUpdateSensorFragment(sensor)
            findNavController().navigate(directions)
        }

        return super.onOptionsItemSelected(item)
    }


    private fun visitUrl(context: Context, url: String) {
        val link  = if (!url.startsWith("http://") && !url.startsWith("https://"))
            "http://$url"
        else
            url

        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(link)
        val chooser = Intent.createChooser(i, "Visit page")
        context.startActivity(chooser)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}