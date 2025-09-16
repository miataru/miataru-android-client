/*
 * Copyright (c) 2013-2025, Daniel Kirstenpfad, www.miataru.com
 *
 * DeviceAdapter.kt
 * miataru
 *
 * Created by Daniel Kirstenpfad on 2025-01-25.
 */

package com.miataru.ui.devices

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.miataru.R
import com.miataru.data.Device

class DeviceAdapter(
    private val onEditClick: (Device) -> Unit,
    private val onDeleteClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    
    private var devices: List<Device> = emptyList()
    
    fun updateDevices(devices: List<Device>) {
        this.devices = devices
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }
    
    override fun getItemCount(): Int = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.text_device_name)
        private val textDeviceId: TextView = itemView.findViewById(R.id.text_device_id)
        private val textColor: TextView = itemView.findViewById(R.id.text_device_color)
        private val colorIndicator: View = itemView.findViewById(R.id.view_color_indicator)
        private val buttonEdit: ImageButton = itemView.findViewById(R.id.button_edit)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.button_delete)
        
        fun bind(device: Device) {
            textName.text = device.name
            textDeviceId.text = device.deviceId
            textColor.text = device.color
            
            // Set color indicator
            try {
                val color = Color.parseColor(device.color)
                colorIndicator.setBackgroundColor(color)
            } catch (e: IllegalArgumentException) {
                colorIndicator.setBackgroundColor(Color.parseColor("#2196F3"))
            }
            
            buttonEdit.setOnClickListener {
                onEditClick(device)
            }
            
            buttonDelete.setOnClickListener {
                onDeleteClick(device)
            }
        }
    }
}
