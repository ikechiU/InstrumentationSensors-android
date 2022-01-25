package com.android.sensors.ui.activites

import android.app.Activity
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.android.sensors.R
import com.android.sensors.data.remote.model.AddSensor
import com.android.sensors.databinding.ActivityAddSensorsBinding
import com.android.sensors.domain.SensorsModel
import com.android.sensors.ui.viewmodel.SensorsViewModel
import com.android.sensors.utils.Const
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.isInternetAvailable
import com.android.sensors.utils.loadImage
import com.android.sensors.utils.visitUrl
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AddSensorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSensorsBinding
    private lateinit var viewModel: SensorsViewModel
    private var isEditingMode = false
    private lateinit var sensor: SensorsModel
    private var isUpdatingMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddSensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditingMode = intent.hasExtra(Const.IS_EDITING_MODE)

        if(isEditingMode) {
            binding.editSource.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            binding.editMoreInfo.paintFlags = Paint.UNDERLINE_TEXT_FLAG

            sensor = intent.getParcelableExtra(Const.PARCELABLE_EXTRA_IS_EDITING_MODE)!!
            setEditingModeSensorDetails()
            binding.editSensorContainer.visibility = View.VISIBLE
            binding.addSensorContainer.visibility = View.GONE

            supportActionBar?.title = sensor.title
        } else {
            supportActionBar?.title = "Add Sensor"
            binding.addSensorContainer.visibility = View.VISIBLE
            binding.editSensorContainer.visibility = View.GONE
        }

        viewModel = ViewModelProvider(this)[SensorsViewModel::class.java]

        buttonClick()

        longClick()

        binding.editSource.setOnClickListener {
            visitUrl(this, binding.editSource.text.toString())
        }

        binding.editMoreInfo.setOnClickListener {
            visitUrl(this, binding.editMoreInfo.text.toString())
        }
    }

    private fun buttonClick() {
        if (isEditingMode) {
            binding.buttonClick.text = "Save"
            editSensor()
        } else {
            binding.buttonClick.text = "Add sensor"
            addSensor()
        }
    }

    private fun editSensor() {
        binding.buttonClick.setOnClickListener {
            if(!binding.title.text.isNullOrEmpty() && !binding.description.text.isNullOrEmpty() && !binding.source.text.isNullOrEmpty()) {

                viewModel.setUpdateSensor(sensor.title!!, AddSensor(
                    binding.title.text.toString(),
                    binding.description.text.toString(),
                    binding.source.text.toString(),
                    binding.moreInfo.text.toString(),
                    binding.imageUrl.text.toString()
                ))
                viewModel.updateSensorRemote.observe(this, {
                    if (it is Resource.Success) {
                        Toast.makeText(this, it.data.title + " updated", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else if (it is Resource.Error) {
                        Toast.makeText(this, it.errorData, Toast.LENGTH_SHORT).show();
                    }
                })

            } else {
                Toast.makeText(this, "Please fill in title, description and source.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSensor() {
        binding.buttonClick.setOnClickListener {
            if(!binding.title.text.isNullOrEmpty() && !binding.description.text.isNullOrEmpty() && !binding.source.text.isNullOrEmpty()) {

                viewModel.setAddSensor(AddSensor(
                    binding.title.text.toString(),
                    binding.description.text.toString(),
                    binding.source.text.toString(),
                    binding.moreInfo.text.toString(),
                    binding.imageUrl.text.toString()
                ))
                viewModel.addSensorRemote.observe(this, {
                    if (it is Resource.Success) {
                        Toast.makeText(this, it.data.title + " added", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else if (it is Resource.Error) {
                        Toast.makeText(this, it.errorData, Toast.LENGTH_SHORT).show();
                    }
                })

            } else {
                Toast.makeText(this, "Please fill in title, description and source.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun longClick() {
        binding.cardContainer.setOnLongClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setMessage("Are you want to delete this sensor from network?")
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
                if(isInternetAvailable(this)){
                    deleteFromNetwork()
                } else {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, _ ->
                dialog.cancel()
            }
            val alert: AlertDialog = builder.create()
            alert.show()

            true
        }
    }

    private fun deleteFromNetwork() {
        viewModel.setSensorId(sensor.title!!)

        viewModel.getSensorRemote.observe(this, {
            when(it) {

                is Resource.Loading -> {
                    Timber.d("Loading")
                }

                is Resource.Success -> {
                    it.data.sensorId?.let { id -> viewModel.setDeleteSensor(id) }

                    viewModel.deleteSensorRemote.observe(this, {operationStatus ->
                        if (operationStatus is Resource.Success) {
                            Toast.makeText(this, "Delete action: " + operationStatus.data.operationResult, Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else if (operationStatus is Resource.Error) {
                            Toast.makeText(this, operationStatus.errorData, Toast.LENGTH_SHORT).show();
                        }

                    })
                }

                is Resource.Error -> {
                    Toast.makeText(this, it.errorData, Toast.LENGTH_SHORT).show();
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.delete) {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setMessage("Do you want to delete this sensor from database?")
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
                sensor.id?.let { viewModel.deleteSensorLocal(it) }
                Toast.makeText(this, "${sensor.title} deleted successfully!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, _ ->
                dialog.cancel()
            }
            val alert: AlertDialog = builder.create()
            alert.show()

        }

        if (id == R.id.update) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            isUpdatingMode = true
            isEditingMode = false
            binding.addSensorContainer.visibility = View.VISIBLE
            binding.editSensorContainer.visibility = View.GONE
            setAddSensorDetails()
            invalidateMenu()
        }

        if (id == android.R.id.home){
            binding.addSensorContainer.visibility = View.GONE
            binding.editSensorContainer.visibility = View.VISIBLE
            isUpdatingMode = false
            isEditingMode = true
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            invalidateMenu()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.delete)?.isVisible = isEditingMode
        menu?.findItem(R.id.update)?.isVisible = isEditingMode
        return true
    }

    private fun setEditingModeSensorDetails() {
        binding.editTitle.text = sensor.title
        binding.editDescription.text = sensor.description
        binding.editSource.text = sensor.source
        binding.editMoreInfo.text = sensor.moreInfo
        loadImage(this, sensor.imageUrl!!, binding.image)
    }

    private fun setAddSensorDetails() {
        binding.title.setText(sensor.title)
        binding.description.setText(sensor.description)
        binding.source.setText(sensor.source)
        binding.moreInfo.setText(sensor.moreInfo)
        binding.imageUrl.setText(sensor.imageUrl)
    }

    override fun onBackPressed() {
        if (isUpdatingMode) {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setMessage("Do you want to go to the main page?")
            builder.setPositiveButton(
                "Yes"
            ) { _, _ ->
                super.onBackPressed()
            }
            builder.setNegativeButton(
                "No"
            ) { dialog, _ ->
                dialog.cancel()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        } else {
            super.onBackPressed()
        }
    }
}