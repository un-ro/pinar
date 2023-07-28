package com.unero.pinar.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Building(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var floors: Int = 0,
    var type: String = "",
    var thumbnail: String = "",
    var location: Location = Location(),
): Parcelable