package com.unero.pinar.presentation.scene

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.TrackingState
import com.unero.pinar.R
import com.unero.pinar.databinding.FragmentSceneBinding
import com.unero.pinar.presentation.sharedviewmodel.NavigationViewModel
import com.unero.pinar.utils.RendererHelper.drawArrow
import com.unero.pinar.utils.RendererHelper.drawLabel
import com.unero.pinar.utils.UIHelper
import com.unero.pinar.utils.UIHelper.alertBuilder
import com.unero.pinar.utils.UIHelper.showToast
import dev.romainguy.kotlin.math.distance
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.CursorNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SceneFragment : Fragment() {
    private var _binding: FragmentSceneBinding? = null
    private val binding get() = _binding as FragmentSceneBinding
    private val args: SceneFragmentArgs by navArgs()

    private val viewModel: SceneViewModel by viewModel()
    private val navigationViewModel by sharedViewModel<NavigationViewModel>()

    private val sceneView: ArSceneView get() = binding.arSceneView

    private var hitTest: HitResult? = null
    private val floor by lazy { args.destination.floor }
    private var isNear = false
    private lateinit var cursorNode: CursorNode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setState(SceneState.PRELOAD)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSceneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabBack.setOnClickListener {
            alertBuilder(
                requireContext(),
                "Confirmation",
                "Are you have arrived to destination?"
            ).setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                UIHelper.popBackFragment(it)
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Exit") { dialog, _ ->
                dialog.dismiss()
                UIHelper.popBackFragment(it)
            }.show()
        }
        setupNavigation()
        sceneListener()

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.pb.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun sceneListener() {
        viewModel.state.observe(viewLifecycleOwner) { event ->
            if (!event.hasBeenHandled) {
                binding.tvState.text = event.peekContent().name.uppercase()
                when(event.peekContent()) {
                    SceneState.PRELOAD -> {
                        viewModel.setLoading(true)
                        lifecycleScope.launch(Dispatchers.IO) {
                            navigationViewModel.setDestination(floor, args.destination)
                        }
                        navigationViewModel.calculatedPath.observe(viewLifecycleOwner) {
                            if (it.isNotEmpty()) {
                                viewModel.setState(SceneState.LOCALIZATION)
                                viewModel.setLoading(false)
                            }
                        }
                    }
                    SceneState.LOCALIZATION -> {
                        setupARScene()
                        alertBuilder(
                            requireContext(),
                            "Localization Needed",
                            "Move your phone slowly to left and right into the ground from QRCode.",
                        ).setNeutralButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                        cursorNode = CursorNode(requireContext(), lifecycle)
                        sceneView.addChild(cursorNode)
                        sceneView.onArFrame = { arFrame ->
                            if (arFrame.camera.trackingState == TrackingState.TRACKING) {
                                if (hitTest == null) {
                                    hitTest = arFrame.hitTest()
                                    if (hitTest != null) {
                                        viewModel.localization(hitTest!!)
                                    }
                                }
                                val currentDistance = distance(
                                    arFrame.camera.pose.position,
                                    viewModel.liftPointFloat3(args.destination.position)
                                )
                                binding.tvFps.text = getString(
                                    R.string.distance,
                                    String.format("%.2f", currentDistance)
                                )

                                if (currentDistance < 1.0f) {
                                    if (!isNear) {
                                        isNear = true
                                        alertBuilder(
                                            requireContext(),
                                            "Destination is near!",
                                            "Check around if your destination is near"
                                        ).setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }
                                            .show()
                                    }
                                }
                            }
                        }
                        navigationViewModel.filterNodesByFloor(floor).forEach {
                            if (it.type == "room") {
                                lifecycleScope.launch {
                                    sceneView.addChild(
                                        drawLabel(
                                            requireContext(),
                                            lifecycle,
                                            it.id,
                                            it.name,
                                            viewModel.pointToFloat3(it.position)
                                        )
                                    )
                                    yield()
                                }
                            }
                        }
                        viewModel.setLoading(false)
                        viewModel.setState(SceneState.ROUTING)
                    }
                    SceneState.ROUTING -> {
                        viewModel.setLoading(true)
                        navigationViewModel.calculatedPath.observe(viewLifecycleOwner) {
                            if (it.isNotEmpty()) {
                                it.asReversed().forEach { poi ->
                                    if (poi.type != "room") {
                                        lifecycleScope.launch {
                                            sceneView.addChild(
                                                drawArrow(
                                                    requireContext(),
                                                    lifecycle,
                                                    viewModel.pointToFloat3(poi.position),
                                                    viewModel.pointToFloat3(args.destination.position)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        viewModel.setLoading(false)
                        binding.cardNavigation.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupARScene() {
        sceneView.apply {
            focusMode = Config.FocusMode.FIXED
            lightEstimationMode = LightEstimationMode.DISABLED
            // Fix camera issue
            focusMode = Config.FocusMode.FIXED
            onArSessionFailed = { exception ->
                showToast(requireContext(), exception.message ?: "Error")
            }
        }
    }

    private fun setupNavigation() {
        binding.pointDestination.apply {
            tvCode.text = args.destination.id
            tvName.text = args.destination.name
            ivClass.load(R.drawable.classroom)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}