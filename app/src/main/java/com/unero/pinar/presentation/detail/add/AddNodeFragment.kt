package com.unero.pinar.presentation.detail.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.*
import com.unero.pinar.R
import com.unero.pinar.data.model.POI
import com.unero.pinar.data.model.PointPosition
import com.unero.pinar.databinding.FragmentAddNodeBinding
import com.unero.pinar.presentation.sharedviewmodel.NavigationViewModel
import com.unero.pinar.utils.RendererHelper.drawPath
import com.unero.pinar.utils.UIHelper.arrayAdapterBuilder
import com.unero.pinar.utils.UIHelper.popBackFragment
import com.unero.pinar.utils.UIHelper.showToast
import com.unero.pinar.utils.pathPattern
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddNodeFragment : Fragment() {

    private var _binding: FragmentAddNodeBinding? = null
    private val binding get() = _binding as FragmentAddNodeBinding

    private val viewModel: AddSceneViewModel by viewModel()
    private val sharedViewModel: NavigationViewModel by sharedViewModel()
    private val args: AddNodeFragmentArgs by navArgs()

    private var newPosition = PointPosition()

    // View
    private val sceneView: ArSceneView get() = binding.sceneView
    private val bottomSheet get() = binding.bottomSheetScene
    private val behavior by lazy { BottomSheetBehavior.from(bottomSheet.root) }

    private var pathCounter = 0
    private val floor by lazy { args.floor }

    // Neighbour
    private var pathNeighbour = arrayListOf<String>()
    private var roomNeighbour = arrayListOf<String>()
    private var nodeList = mutableListOf<POI>()

    private lateinit var pathModel: ArModelNode

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNodeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabBack.setOnClickListener { popBackFragment(it) }
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        initBottomSheet()
        setupScene()
        setupType()

        viewModel.nodeList.observe(viewLifecycleOwner) {
            bottomSheet.btnAddNeighbour.isEnabled = it.isNotEmpty()
            if (it.isNotEmpty()) {
                val adapter = arrayAdapterBuilder(
                    requireContext(),
                    viewModel.mapNeighbours(it).toTypedArray()
                )
                bottomSheet.inputNeighbour.setAdapter(adapter)
            }
        }

        bottomSheet.btnAdd.setOnClickListener {
            val node = extract()
            viewModel.addNode(sharedViewModel.sharedCurrentBuilding, node)
            nodeList.add(node)
            viewModel.insertNewNode(nodeList)
        }

        viewModel.status.observe(viewLifecycleOwner) {
            if (it.peekContent()) {
                if (bottomSheet.inputType.text.toString().lowercase() == "path") pathCounter++

                sceneView.addChild(
                    drawPath(
                        requireContext(),
                        lifecycle,
                        Position(newPosition.x, newPosition.y, newPosition.z)
                    )
                )

                if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                    binding.fabAdd.show()
                }
                clearForm()
            }
        }

        viewModel.report.observe(viewLifecycleOwner) {
            if (!it.hasBeenHandled) {
                viewModel.newReport(it.peekContent())
            }
        }

        viewModel.reportStatus.observe(viewLifecycleOwner) {
            if (!it.hasBeenHandled)
                showToast(requireContext(), if (it.peekContent()) "Report Success" else "Report Failed")
        }
    }

    private fun clearForm() = with(bottomSheet) {
        inputId.setText("")
        inputName.setText("")
        pathNeighbour.clear()
        roomNeighbour.clear()
        tvNearPath.text = "Empty"
        tvNearRoom.text = "Empty"
    }

    private fun extract(): POI = POI(
        bottomSheet.inputId.text.toString(),
        bottomSheet.inputName.text.toString(),
        floor,
        bottomSheet.inputType.text.toString().lowercase(),
        pathNeighbour,
        roomNeighbour,
        newPosition
    )

    private fun setupType() {
        val arrayAdapter = arrayAdapterBuilder(
            requireContext(),
            resources.getStringArray(R.array.type_node)
        )
        bottomSheet.inputType.setAdapter(arrayAdapter)
    }

    private fun initBottomSheet() = with(bottomSheet) {
        inputType.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                1 -> {
                    inputId.setText("path.$floor.$pathCounter")
                    inputName.setText("Path $floor.$pathCounter")
                }
                2 -> {
                    inputId.setText("start$floor")
                    inputName.setText("Start $floor")
                }
                else -> {
                    inputId.setText("")
                    inputName.setText("")
                }
            }
        }

        btnAddNeighbour.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val neighbourId = inputNeighbour.text.toString()
                val isPath = neighbourId.contains(pathPattern)

                if (isPath && !pathNeighbour.contains(neighbourId)) {
                    pathNeighbour.add(inputNeighbour.text.toString())
                    tvNearPath.text = pathNeighbour.joinToString(", ")
                }
                if (!isPath && !roomNeighbour.contains(neighbourId)) {
                    roomNeighbour.add(inputNeighbour.text.toString())
                    tvNearRoom.text = roomNeighbour.joinToString(", ")
                }
            }
        }

        btnClose.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            clearForm()
            binding.fabAdd.show()
        }
    }

    private fun setupScene() {
        sceneView.apply {
            configureSession { arSession, _ ->
                lightEstimationMode = LightEstimationMode.DISABLED
                planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                focusMode = Config.FocusMode.AUTO

                if (arSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    depthMode = Config.DepthMode.AUTOMATIC
                }
            }

            addChild(CursorNode(requireContext(), lifecycle))

            onArFrame = { arFrame ->
                val camera = arFrame.camera
                updateStatusText(camera.trackingState, camera.trackingFailureReason)
                updateStatusIcon(camera)

                if (camera.trackingState == TrackingState.TRACKING) {
                    binding.fabAdd.setOnClickListener {
                        updatePosition(camera.pose)

                        if (behavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            binding.fabAdd.hide()

                            val hit = arFrame.hitTest()

                            if (hit != null) {
                                newPosition = PointPosition(
                                    hit.hitPose.tx(), hit.hitPose.ty(), hit.hitPose.tz()
                                )
                            }
                        }
                    }
                }
            }
            onArSessionFailed = { showToast(requireContext(), it.toString()) }
        }
    }

    private fun updatePosition(pose: Pose) {
        bottomSheet.tvPosition.text = "[${simply(pose.tx())}," +
                " ${simply(pose.ty())}, ${simply(pose.tz())}]"
    }

    private fun simply(poseValue: Float): String = String.format("%.2f", poseValue)

    // Update TrackingState and TrackingFailureReason in the UI.
    private fun updateStatusText(state: TrackingState, reason: TrackingFailureReason?) {
        binding.tvTrackingState.text = when (state) {
            TrackingState.TRACKING -> state.name
            else -> when (reason) {
                TrackingFailureReason.INSUFFICIENT_LIGHT -> "${state.name}: Insufficient Light"
                TrackingFailureReason.EXCESSIVE_MOTION -> "${state.name}: Excessive Motion"
                TrackingFailureReason.INSUFFICIENT_FEATURES -> "${state.name}: Insufficient Features"
                TrackingFailureReason.CAMERA_UNAVAILABLE -> "${state.name}: Camera Unavailable"
                TrackingFailureReason.BAD_STATE -> "${state.name}: ${reason.name}"
                else -> "${state.name}: Unknown"
            }
        }
    }

    // Update Icon Tracking on SceneView
    private fun updateStatusIcon(cameraState: Camera) {
        binding.ivTrackingIcon.setImageResource(
            when (cameraState.trackingState) {
                TrackingState.TRACKING -> android.R.drawable.presence_online
                TrackingState.PAUSED -> android.R.drawable.presence_away
                else -> android.R.drawable.presence_offline
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
