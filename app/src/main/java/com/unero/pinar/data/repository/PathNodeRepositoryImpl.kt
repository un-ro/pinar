package com.unero.pinar.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.unero.pinar.data.model.POI
import com.unero.pinar.domain.repository.PathNodeRepository
import com.unero.pinar.utils.BUILDING_PATH
import com.unero.pinar.utils.POI_PATH
import com.unero.pinar.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PathNodeRepositoryImpl: PathNodeRepository {
    private val db = Firebase.firestore
    private val rootCollection = db.collection(BUILDING_PATH)

    override fun getPathNodes(buildingId: String) = callbackFlow {
        val snapshotListener = rootCollection.document(buildingId).collection(POI_PATH)
            .addSnapshotListener { snapshot, e ->
                val response = if (snapshot != null) {
                    val nodes = snapshot.toObjects(POI::class.java)
                    Response.Success(nodes)
                } else {
                    Response.Error(e?.message ?: e.toString())
                }
                trySend(response).isSuccess
            }
        awaitClose { snapshotListener.remove() }
    }

    override suspend fun addPathNode(buildingId: String, poi: POI) = flow {
        try {
            emit(Response.Loading)
            val newNode = rootCollection.document(buildingId)
                .collection(POI_PATH).document(poi.id).set(poi).await()
            emit(Response.Success(newNode))
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: e.toString()))
        }
    }

    override suspend fun insertNeighbour(
        buildingId: String,
        poi: POI,
        newNeighbour: String
    ): Flow<Response<Void?>> = flow {
        try {
            emit(Response.Loading)
            val updateNode = rootCollection.document(buildingId).collection(POI_PATH).document(poi.id)
                .update("neighbours", FieldValue.arrayUnion(newNeighbour)).await()
            emit(Response.Success(updateNode))
        } catch (e: Exception) {
            emit(Response.Error(e.message ?: e.toString()))
        }
    }
}