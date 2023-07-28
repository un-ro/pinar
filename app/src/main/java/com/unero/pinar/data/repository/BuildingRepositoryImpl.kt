package com.unero.pinar.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unero.pinar.data.model.Building
import com.unero.pinar.data.model.Location
import com.unero.pinar.domain.repository.BuildingRepository
import com.unero.pinar.utils.BUILDING_PATH
import com.unero.pinar.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class BuildingRepositoryImpl: BuildingRepository {
    private val db = Firebase.firestore
    private val collection = db.collection(BUILDING_PATH)

    override fun getBuildings() = callbackFlow {
        val snapshotListener = collection.addSnapshotListener { snapshot, e ->
            val response = if (snapshot != null) {
                val books = snapshot.toObjects(Building::class.java)
                Timber.tag("BuildingRepository").d("getBuildings: ${books.size}")
                Response.Success(books)
            } else {
                Response.Error(e?.message ?: e.toString())
            }
            trySend(response).isSuccess
        }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override suspend fun addBuilding(building: Building): Flow<Response<Void?>> = flow {
        try {
            emit(Response.Loading)

            val data = hashMapOf(
                "floors" to building.floors,
                "description" to building.description,
                "thumbnail" to building.thumbnail,
                "type" to building.type,
                "location" to Location(
                    building.location.latitude,
                    building.location.longitude
                )
            )

            if (building.type != "academy") data["name"] = building.name

            val newBuilding = collection.document(building.id).set(data).await()
            emit(Response.Success(newBuilding))
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: e.toString()))
        }
    }
}