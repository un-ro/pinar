package com.unero.pinar.domain.repository

import com.unero.pinar.data.model.POI
import com.unero.pinar.utils.Response
import kotlinx.coroutines.flow.Flow

interface PathNodeRepository {
    fun getPathNodes(buildingId: String): Flow<Response<List<POI>>>
    suspend fun addPathNode(buildingId: String, poi: POI): Flow<Response<Void?>>
    suspend fun insertNeighbour(buildingId: String, poi: POI, newNeighbour: String): Flow<Response<Void?>>
}