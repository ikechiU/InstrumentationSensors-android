package com.android.sensors.ui.screen.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.sensors.databinding.FragmentSensorsBinding
import com.android.sensors.ui.adapter.SensorsAdapter
import com.android.sensors.ui.adapter.SensorsLoadingAdapter
import com.android.sensors.utils.Const
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.R
import androidx.fragment.app.Fragment


@AndroidEntryPoint
class SensorsFragment : BaseFragment() {

    private var _binding: FragmentSensorsBinding? = null
    private val binding: FragmentSensorsBinding get() = _binding!!
    private lateinit var sensorsAdapter: SensorsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentSensorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setUpRecyclerView()
        fetchSensors()

        binding.swipe.setOnRefreshListener {
            fetchSensors()

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                if (binding.swipe.isRefreshing) {
                    binding.swipe.isRefreshing = false
                }
            }, Const.SWIPE_DELAY)
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(com.android.sensors.R.id.action_sensorsFragment_to_addUpdateSensorFragment)
        }
    }


    private fun setUpRecyclerView() {
        sensorsAdapter = SensorsAdapter(getActivity) { sensorModel ->
            val directions =
                SensorsFragmentDirections.actionSensorsFragmentToSensorsDetailsFragment(
                    sensorModel
                )
            findNavController().navigate(directions)
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(getActivity)
        binding.recyclerview.adapter = sensorsAdapter

        binding.recyclerview.adapter = sensorsAdapter.withLoadStateHeaderAndFooter(
            header = SensorsLoadingAdapter { sensorsAdapter.retry() },
            footer = SensorsLoadingAdapter { sensorsAdapter.retry() }
        )
    }

    private fun fetchSensors() {
        if (isInternetAvailable(getActivity)) {
            showProgressBar(binding.progressBar)
            loadSensors()
        } else {
            loadSensors()
            lifecycleScope.launch {
                delay(10000)
                if (isInternetAvailable(getActivity)) {
                    loadSensors()
                } else {
                    viewModel.stopLoading()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getActivity, "No internet.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadSensors() {
        lifecycleScope.launch {
            viewModel.fetchSensors().collectLatest { pagingData ->
                sensorsAdapter.submitData(pagingData)
                viewModel.stopLoading()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding =  null
    }

}