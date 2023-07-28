package com.unero.pinar.presentation.scene

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.HitResult
import com.unero.pinar.data.model.PointPosition
import com.unero.pinar.utils.Event
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SceneViewModel : ViewModel() {
    private val _state = MutableLiveData<Event<SceneState>>()
    val state get() = _state

    fun setState(newState: SceneState) {
        _state.value = Event(newState)
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading get() = _isLoading

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private var localizationHit: HitResult? = null
    private var position: Float3 = Float3(0f, 0f, 0f)

    fun localization(hitResult: HitResult) {
        viewModelScope.launch(Dispatchers.IO) {
            localizationHit = hitResult
            position = hitResult.hitPose.position
        }
    }

    fun pointToFloat3(point: PointPosition): Position = Position(
        point.x, point.y, point.z
    )

    fun liftPointFloat3(point: PointPosition): Position = Position(
        point.x, 0.5f, point.z
    )

    companion object {
        const val TAG = "SceneViewModel"
    }
}