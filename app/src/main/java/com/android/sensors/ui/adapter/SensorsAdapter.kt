package com.android.sensors.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.sensors.databinding.SensorItemBinding
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.loadImage


class SensorsAdapter(private val context: Context, private val onSensorClickListener: (SensorsModel) -> Unit) :
    ListAdapter<SensorsModel, SensorsAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<SensorsModel>() {
        override fun areItemsTheSame(
            oldItem: SensorsModel,
            newItem: SensorsModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SensorsModel,
            newItem: SensorsModel
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, getItem(position))
        holder.binding.root.setOnClickListener {
            onSensorClickListener.invoke(getItem(position))
        }
    }

    class ViewHolder(val binding: SensorItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context, sensorsModel: SensorsModel) {
            binding.title.text = sensorsModel.title
            binding.description.text = sensorsModel.description
            loadImage(context, sensorsModel.imageUrl!!, binding.image)
        }
    }

}