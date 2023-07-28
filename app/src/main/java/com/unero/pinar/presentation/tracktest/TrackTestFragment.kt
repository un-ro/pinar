package com.unero.pinar.presentation.tracktest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.ar.core.Config
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.unero.pinar.data.model.research.FailReport
import com.unero.pinar.databinding.FragmentTrackTestBinding
import com.unero.pinar.utils.UIHelper.alertBuilder
import com.unero.pinar.utils.UIHelper.popBackFragment
import com.unero.pinar.utils.UIHelper.showToast
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.CursorNode
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackTestFragment : Fragment() {
    private var _binding: FragmentTrackTestBinding? = null
    private val binding get() = _binding as FragmentTrackTestBinding

    private val viewModel: TrackTestViewModel by viewModel()
    private val args: TrackTestFragmentArgs by navArgs()

    private var isFound = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackTestBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { popBackFragment(it) }

        binding.sceneView.apply {
            configureSession { arSession, _ ->
                lightEstimationMode = LightEstimationMode.AMBIENT_INTENSITY
                planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                focusMode = Config.FocusMode.AUTO

                if (arSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                    depthMode = Config.DepthMode.AUTOMATIC
            }
            addChild(CursorNode(requireContext(), lifecycle))
            lightEstimationMode = LightEstimationMode.DISABLED
            onArSessionFailed = { exception ->
                showToast(requireContext(), exception.message ?: "Error")
            }
            onArFrame = { arFrame ->
                val trackingState = arFrame.camera.trackingState
                val reason = arFrame.camera.trackingFailureReason
                binding.tvDebug.text = "${trackingState.name} $reason"

                if (trackingState != TrackingState.TRACKING) {
                    if (reason != TrackingFailureReason.NONE &&
                        reason != TrackingFailureReason.BAD_STATE) {
                        triggerReport(trackingState.name, reason.name)
                    }
                }
            }
        }

        viewModel.status.observe(viewLifecycleOwner) {
            if (!it.hasBeenHandled) {
                if (it.peekContent()) {
                    isFound = !it.peekContent()
                    showToast(requireContext(), "New Report send!")
                }
            }
        }
    }

    private fun triggerReport(state: String, reason: String){
        if (!isFound) {
            isFound = true

            alertBuilder(
                requireContext(),
                "Found Error Tracking",
                "$state - $reason"
            ).setPositiveButton("Send It!") {dialog, _ ->
                lifecycleScope.launch {
                    viewModel.newReport(
                        FailReport(args.buildingId, state, reason, "DEV")
                    )
                }
                dialog.dismiss()
            }.setNegativeButton("Nah") { dialog, _ ->
                isFound = false
                dialog.dismiss()
            }.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}