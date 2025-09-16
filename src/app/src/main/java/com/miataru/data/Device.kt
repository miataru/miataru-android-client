/*
 * Copyright (c) 2013-2025, Daniel Kirstenpfad, www.miataru.com
 *
 * Device.kt
 * miataru
 *
 * Created by Daniel Kirstenpfad on 2025-01-25.
 */

package com.miataru.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Device(
    val id: String,
    val name: String,
    val deviceId: String,
    val color: String
) : Parcelable {
    companion object {
        fun createEmpty(): Device = Device(
            id = "",
            name = "",
            deviceId = "",
            color = "#2196F3" // Default blue color
        )
    }
}
