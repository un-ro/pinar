package com.unero.pinar.presentation.sharedviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unero.pinar.data.model.POI
import com.unero.pinar.data.model.PointPosition
import com.unero.pinar.domain.repository.PathNodeRepository
import com.unero.pinar.utils.Response
import dev.romainguy.kotlin.math.distance
import io.github.sceneview.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

// Shared VM for navigation fragments
class NavigationViewModel(
    private val repository: PathNodeRepository
): ViewModel() {
    // Shared Value
    var sharedNodeList = mutableListOf<POI>()
    var sharedCurrentBuilding = ""

    private var _nodeList = MutableLiveData<Response<List<POI>>>()
    val nodeList get() = _nodeList

    fun getNodes(buildingId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPathNodes(buildingId).collect {
                _nodeList.postValue(it)
            }
        }
    }

    private var pathList = mutableListOf<String>()
    private var endNode = POI()

    private val _calculatedPath = MutableLiveData<List<POI>>()
    val calculatedPath get() = _calculatedPath

    fun setDestination(floor: Int, destination: POI) {
        viewModelScope.launch(Dispatchers.IO) {
            // Clear previous result
            pathList.clear()
            _calculatedPath.postValue(emptyList())

            val startNode = findNode("start$floor")
            endNode = destination

            if (startNode != null) {
                pathList.add(startNode.id)

                if (startNode.nearestPath.size > 1)
                    searchPath(getMinCostPath(startNode)!!)
                else
                    searchPath(startNode.nearestPath.first())

                while (!pathList.contains(endNode.id)) println("Do Nothing")

                val list = mutableListOf<POI>()
                pathList.forEach { findNode(it).let { poi -> list.add(poi!!) } }
                _calculatedPath.postValue(list)
            }
        }
    }

    private fun searchPath(id: String) {
        val currentNode = findNode(id)
        if (currentNode != null) {
            pathList.add(currentNode.id)
            val rooms = currentNode.nearestRoom

            // If current Node has found Destination
            if (rooms.isNotEmpty() && rooms.contains(endNode.id))
                pathList.add(endNode.id)
            else if (currentNode.nearestPath.size > 1)
                searchPath(getMinCostPath(currentNode)!!)
            else
                searchPath(currentNode.nearestPath[0])
        } else throw IOException("Can't find current Node!")
    }

    private fun getMinCostPath(node: POI): String? {
        val branchNode = mutableMapOf<String, Float>()

        // Calculate each path to Destination
        node.nearestPath.forEach {
            val nextNode = findNode(it)
            if (nextNode != null) {
                branchNode[nextNode.id] = calculateDistance(nextNode, endNode)
                Timber.tag("SEARCHING").d("${nextNode.id} ${branchNode[nextNode.id]}" )
            }
        }

        // Find the minimal distance
        val minCost: Float = branchNode.values.toList().min()

        // Set the next Node
        return branchNode.entries.find { it.value == minCost }?.key
    }

    private fun calculateDistance(nodeA: POI, nodeB: POI): Float =
        distance(
            convertPointToPosition(nodeA.position),
            convertPointToPosition(nodeB.position)
        )

    private fun convertPointToPosition(position: PointPosition): Position =
        Position(position.x, position.y, position.z)

    // Filter & Search Functions
    fun filterNodes(): List<POI> = sharedNodeList.filter { it.type != "path" }
    fun isStartAvailable(): Boolean = sharedNodeList.any { it.type == "start" }

    fun roomNodesByFloor(floor: Int): List<POI> = sharedNodeList.filter {
        it.floor == floor && it.type == "room"
    }

    fun filterNodesByFloor(floor: Int): List<POI> = sharedNodeList.filter { it.floor == floor }

    private fun findNode(id: String): POI? = sharedNodeList.find { it.id == id }
}