package com.unero.pinar.utils

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.ktx.storage

object StorageHelper {
    fun getThumbnail(fileName: String): Task<Uri> {
        val storageRef = Firebase.storage.reference
        val thumbnailRef = storageRef.child("buildings/thumbnails")
        return thumbnailRef.child(fileName).downloadUrl
    }

    fun getListThumbnail(): Task<ListResult> {
        val rootRef = Firebase.storage.reference
        return rootRef.child("buildings/thumbnails").listAll()
    }
}