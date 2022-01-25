package com.android.sensors.ui.activites

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sensors.databinding.ActivitySensorsBinding
import com.android.sensors.ui.adapter.SensorsAdapter
import com.android.sensors.ui.viewmodel.SensorsViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.paging.map
import com.android.sensors.domain.SensorsModel
import com.android.sensors.ui.adapter.SensorsLoadingAdapter
import com.android.sensors.utils.Const.IS_EDITING_MODE
import com.android.sensors.utils.Const.PARCELABLE_EXTRA_IS_EDITING_MODE
import com.android.sensors.utils.Const.SWIPE_DELAY
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.isInternetAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SensorsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySensorsBinding
    private lateinit var viewModel: SensorsViewModel
    private lateinit var sensorsAdapter: SensorsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SensorsViewModel::class.java]

        setUpRecyclerView()
        fetchSensors()

        binding.swipe.setOnRefreshListener {
            fetchSensors()

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

    private fun setUpRecyclerView() {
        sensorsAdapter = SensorsAdapter(this)
        { sensorModel ->
            val intent = Intent(this, AddSensorsActivity::class.java)
            intent.putExtra(PARCELABLE_EXTRA_IS_EDITING_MODE, sensorModel)
            intent.putExtra(IS_EDITING_MODE, true)
            toAddSensorActivity.launch(intent)
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = sensorsAdapter

        binding.recyclerview.adapter = sensorsAdapter.withLoadStateHeaderAndFooter(
            header = SensorsLoadingAdapter { sensorsAdapter.retry() },
            footer = SensorsLoadingAdapter { sensorsAdapter.retry() }
        )
    }

    private fun fetchSensors() {

        if (isInternetAvailable(this)) {
            loadSensors()
        } else {
            Toast.makeText(this, "No internet.", Toast.LENGTH_SHORT).show()
            loadSensors()
        }
    }

    private fun loadSensors() {
        lifecycleScope.launch {
            viewModel.fetchSensors().collectLatest { pagingData ->

                sensorsAdapter.submitData(pagingData)
                binding.progressBar.visibility = View.GONE

            }
        }
    }


    private var toAddSensorActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchSensors()
                Timber.d("Result code ${result.resultCode} == ${Activity.RESULT_OK}")
            }
        }

}