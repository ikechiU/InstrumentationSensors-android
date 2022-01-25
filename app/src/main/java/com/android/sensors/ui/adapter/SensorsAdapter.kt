package com.android.sensors.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.databinding.SensorItemBinding
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.loadImage

class SensorsAdapter(private val context: Context, private val onSensorClickListener: (SensorsModel) -> Unit):
    PagingDataAdapter<SensorsDbModel, SensorsAdapter.SensorsViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<SensorsDbModel>() {
        override fun areItemsTheSame(
            oldItem: SensorsDbModel,
            newItem: SensorsDbModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SensorsDbModel,
            newItem: SensorsDbModel
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorsViewHolder {
        val binding = SensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SensorsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SensorsViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(context, it) }
        holder.binding.root.setOnClickListener {
            getItem(position)?.let { sensorDbModel ->
                val data = SensorsModel(
                    sensorDbModel.id,
                    sensorDbModel.title,
                    sensorDbModel.description,
                    sensorDbModel.source,
                    sensorDbModel.moreInfo,
                    sensorDbModel.imageUrl
                )
                onSensorClickListener.invoke(data)
            }
        }
    }

    class SensorsViewHolder(val binding: SensorItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, sensorsDbModel: SensorsDbModel) {
            binding.title.text = sensorsDbModel.title
            binding.description.text = sensorsDbModel.description
            loadImage(context, sensorsDbModel.imageUrl!!, binding.image)
        }
    }

}