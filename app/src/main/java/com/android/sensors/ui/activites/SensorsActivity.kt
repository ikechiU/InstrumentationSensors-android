package com.android.sensors.ui.activites

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sensors.databinding.ActivitySensorsBinding
import com.android.sensors.ui.adapter.SensorsAdapter
import com.android.sensors.ui.viewmodel.SensorsViewModel
import com.android.sensors.utils.Const.IS_EDITING_MODE
import com.android.sensors.utils.Const.PARCELABLE_EXTRA_IS_EDITING_MODE
import dagger.hilt.android.AndroidEntryPoint
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.Const.SWIPE_DELAY
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.isInternetAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SensorsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySensorsBinding
    private lateinit var viewModel: SensorsViewModel
    private lateinit var adapter: SensorsAdapter
    private var sensorsListLocal: List<SensorsModel>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SensorsViewModel::class.java]

        setUpRecyclerView()

        binding.swipe.setOnRefreshListener {
            populateListFromRemote()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (binding.swipe.isRefreshing) {
                    binding.swipe.isRefreshing = false
                }
            }, SWIPE_DELAY)
        }

        binding.fab.setOnClickListener {
            val intent = Intent(this, AddSensorsActivity::class.java)
            toAddSensorActivity.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        populateListFromDb()
    }

    private fun setUpRecyclerView() {
        adapter = SensorsAdapter(this) { sensorModel ->
            val intent = Intent(this, AddSensorsActivity::class.java)
            intent.putExtra(PARCELABLE_EXTRA_IS_EDITING_MODE, sensorModel)
            intent.putExtra(IS_EDITING_MODE, true)
            toAddSensorActivity.launch(intent)
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter
    }

    private var toAddSensorActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            populateListFromDb()
        }
    }

    private fun populateListFromDb() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getAllSensorsLocal.observe(this@SensorsActivity, {
                if(it.isNullOrEmpty()) {
                    Timber.d("${it.size}")
                    populateListFromRemote()
                } else {
                    adapter.submitList(it)
                    sensorsListLocal = it
                    Timber.d("TAG, listLocal 1: ${sensorsListLocal!!.size}")
                }
            })
        }
    }

    private fun populateListFromRemote() {
        if (isInternetAvailable(this)) {
            binding.emptyList.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.getAllSensorsRemote.observe(this@SensorsActivity, {
                    when (it) {

                        is Resource.Loading -> {
                            Toast.makeText(this@SensorsActivity, "Loading...", Toast.LENGTH_SHORT).show();
                            binding.recyclerview.visibility = View.INVISIBLE
                        }

                        is Resource.Success -> {
                            binding.recyclerview.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE

                            Timber.d("TAG, populateListFromRemote: ${it.data.size}")

                            if(!sensorsListLocal.isNullOrEmpty()) {
                                viewModel.deleteSensorsLocal(sensorsListLocal!!)
                            }

                            viewModel.insertSensorsLocal(it.data)
                        }

                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@SensorsActivity, it.errorData, Toast.LENGTH_SHORT).show();
                        }
                    }

                })
            }

        } else {
            binding.emptyList.visibility = View.VISIBLE
        }
    }

}