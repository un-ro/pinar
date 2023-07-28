package com.unero.pinar.domain.repository

import com.unero.pinar.data.model.Building
import com.unero.pinar.utils.Response
import kotlinx.coroutines.flow.Flow

interface BuildingRepository {
    fun getBuildings(): Flow<Response<List<Building>>>

    suspend fun addBuilding(building: Building): Flow<Response<Void?>>
}