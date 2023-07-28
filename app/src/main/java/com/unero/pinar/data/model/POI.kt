package com.unero.pinar.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class POI(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var floor: Int = 0,
    var type: String = "",
    val nearestPath: List<String> = mutableListOf(),
    val nearestRoom: List<String> = mutableListOf(),
    var position: PointPosition = PointPosition(),
): Parcelable
