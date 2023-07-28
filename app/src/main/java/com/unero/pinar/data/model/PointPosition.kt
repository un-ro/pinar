package com.unero.pinar.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointPosition(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
): Parcelable
