package com.unero.pinar.utils

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.unero.pinar.R
import dev.romainguy.kotlin.math.Quaternion
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.math.toNewQuaternion
import io.github.sceneview.math.toVector3
import kotlinx.coroutines.future.await

object RendererHelper {
    fun drawPath(
        context: Context,
        lifecycle: Lifecycle,
        modelPosition: Position,
    ): ArNode = ArNode().apply {
        loadModelAsync(
            context,
            lifecycle,
            "models/cylinder.glb",
        )
        position = modelPosition
        scale = Scale(0.5f)
        anchorPoseUpdateInterval = 10.0
        anchor = createAnchor()
    }

    suspend fun drawLabel(
        context: Context,
        lifecycle: Lifecycle,
        roomId: String? = null,
        roomName: String? = null,
        modelPosition: Position,
    ): ArNode {
        var node: ArNode? = null
        ViewRenderable.builder()
            .setView(context, if (roomName != null) R.layout.item_renderable else R.layout.item_arrow)
            .setSizer { Scale(0.5f, 0.5f, 0.0f).toVector3() }
            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
            .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
            .build(lifecycle)
            .thenAccept { viewRender ->
                viewRender.let {
                    it.isShadowCaster = false
                    it.isShadowReceiver = false
                }

                if (roomId != null && roomName != null) {
                    val cardView = viewRender.view
                    val textId = cardView.findViewById<TextView>(R.id.tv_id)
                    val textName = cardView.findViewById<TextView>(R.id.tv_name)

                    textId.text = roomId
                    textName.text = roomName
                }

                val textNode = ArNode().apply {
                    setModel(viewRender)
                    model
                    position = Position(modelPosition.x, -0.2f, modelPosition.z)
                    quaternion = Quaternion(0.0f, 0.15f, 0.0f, 0.2f)
                    anchorPoseUpdateInterval = 10.0
                    anchor = createAnchor()
                }

                node = textNode
            }.await()
        return node!!
    }

    suspend fun drawArrow(
        context: Context,
        lifecycle: Lifecycle,
        modelPosition: Position,
        targetPosition: Position,
    ): ArNode {
        var node: ArNode? = null
        ViewRenderable.builder()
            .setView(context, R.layout.item_arrow)
            .setSizer { Scale(0.5f, 0.5f, 0.0f).toVector3() }
            .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
            .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
            .build(lifecycle)
            .thenAccept { viewRender ->
                viewRender.let {
                    it.isShadowCaster = false
                    it.isShadowReceiver = false
                }

                val arrowNode = ArNode().apply {
                    setModel(viewRender)
                    model
                    position = Position(modelPosition.x, -1.0f, modelPosition.z)
                    anchorPoseUpdateInterval = 10.0
                    anchor = createAnchor()
                    modelQuaternion = calculateRotation(modelPosition, targetPosition)
                }

                node = arrowNode
            }.await()
        return node!!
    }

    private fun calculateRotation(from: Position, to: Position): Quaternion {
        val diff = Vector3.subtract(from.toVector3(), to.toVector3())
        val diffNormalized = diff.normalized()
        val rotationToTarget = com.google.ar.sceneform.math.Quaternion.lookRotation(
            diffNormalized, Vector3.up()
        )

        return com.google.ar.sceneform.math.Quaternion.multiply(
            rotationToTarget,
            com.google.ar.sceneform.math.Quaternion.axisAngle(
                Vector3(-1.0f, 0.0f, 0.0f), 270f
            )
        ).toNewQuaternion()
    }
}