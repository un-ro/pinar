package com.unero.pinar.presentation.detail.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unero.pinar.data.model.POI
import com.unero.pinar.data.model.research.FailReport
import com.unero.pinar.domain.repository.PathNodeRepository
import com.unero.pinar.domain.repository.ReportRepository
import com.unero.pinar.presentation.scene.SceneViewModel
import com.unero.pinar.utils.Event
import com.unero.pinar.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AddSceneViewModel(
    private val repository: PathNodeRepository,
    private val reporter: ReportRepository
): ViewModel() {
    private var _status = MutableLiveData<Event<Boolean>>()
    val status get() = _status

    fun addNode(buildingId: String, node: POI) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPathNode(buildingId, node).collect { response ->
                when (response) {
                    is Response.Success -> _status.postValue(Event(true))
                    is Response.Error -> _status.postValue(Event(false))
                    Response.Loading -> Timber.tag(SceneViewModel.TAG).i("Loading")
                }
            }
        }
    }

    private var _reportStatus = MutableLiveData<Event<Boolean>>()
    val reportStatus get() = _reportStatus

    fun inputReport(failReport: FailReport) {
        viewModelScope.launch(Dispatchers.IO) {
            reporter.newReport(failReport)
                .addOnSuccessListener { _reportStatus.postValue(Event(true)) }
                .addOnFailureListener { _reportStatus.postValue(Event(false)) }
                .await()
        }
    }

    private var _report = MutableLiveData<Event<FailReport>>()
    val report get() = _report

    fun newReport(failReport: FailReport) {
        _report.value = Event(failReport)
    }

    private var _nodeList = MutableLiveData<List<POI>>()
    val nodeList get() = _nodeList

    fun insertNewNode(nodeList: List<POI>) {
        _nodeList.value = nodeList
    }

    fun mapNeighbours(nodeList: List<POI>): List<String> = nodeList.map { it.id }
}