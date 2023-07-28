package com.unero.pinar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
): Parcelable
