package com.android.sensors.ui.screen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import com.android.sensors.databinding.ActivitySensorsBinding
import com.android.sensors.ui.viewmodel.SensorsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SensorsActivity : AppCompatActivity() {

    private lateinit var viewModel: SensorsViewModel
    private lateinit var binding: ActivitySensorsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySensorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SensorsViewModel::class.java]

    }
}