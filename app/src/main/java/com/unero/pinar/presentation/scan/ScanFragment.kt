package com.unero.pinar.presentation.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import com.unero.pinar.data.model.POI
import com.unero.pinar.databinding.FragmentScanBinding
import com.unero.pinar.utils.PermissionHelper.hasCameraPermission
import com.unero.pinar.utils.PermissionHelper.requestCameraPermission
import com.unero.pinar.utils.UIHelper.alertBuilder
import com.unero.pinar.utils.UIHelper.popBackFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding as FragmentScanBinding
    private val args: ScanFragmentArgs by navArgs()
    private val destination: POI? by lazy { args.destination }

    private val viewModel: ScanViewModel by viewModel()

    // QR Reader
    private val codeScanner by lazy { CodeScanner(requireContext(), binding.scannerView) }

    private var isFound = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabBack.setOnClickListener { popBackFragment(it) }

        alertBuilder(
            requireContext(),
            "Scan QR Code!",
            "QR Code usually in the center of lobby each level of Building."
        ).setPositiveButton("OK") { dialog, _ -> dialog.dismiss()
        }.show()

        if (hasCameraPermission(requireContext())) setupQRCamera()
        else requestCameraPermission(requireActivity())

        viewModel.qrValue.observe(viewLifecycleOwner) {
            if (!it.hasBeenHandled) {
                val data = it.peekContent()

                if (data != null) {
                    if (!isFound) {
                        isFound = true
                        if (destination != null) {
                            if (destination!!.floor != data) {
                                alertBuilder(
                                    requireContext(),
                                    "Different Floor",
                                    "Please scan in the same floor as destination"
                                ).setNeutralButton("OK") { dialog, _ ->
                                    isFound = false
                                    dialog.dismiss()
                                }.show()
                            } else {
                                alertBuilder(
                                    requireContext(),
                                    "Match",
                                    "You are on level ${destination!!.floor}"
                                ).setPositiveButton("OK") {dialog, _ ->
                                    findNavController().navigate(ScanFragmentDirections
                                        .toSceneFragment(destination!!)
                                    )
                                    dialog.dismiss()
                                }.show()
                            }
                        } else {
                            alertBuilder(
                                requireContext(),
                                "Found Level",
                                "You are on level $data"
                            ).setPositiveButton("OK") {dialog, _ ->
                                findNavController().navigate(ScanFragmentDirections
                                    .toAddNodeFragment(data)
                                )
                                dialog.dismiss()
                            }.show()
                        }
                    }
                } else {
                    alertBuilder(
                        requireContext(),
                        "Different QRCode",
                        "Please scan PINAR QRCode Only"
                    ).setNeutralButton("OK") {dialog, _ ->
                        dialog.dismiss()
                    }.show()
                }
            }
        }
    }

    private fun setupQRCamera() = with(codeScanner) {
        camera = CodeScanner.CAMERA_BACK
        formats = listOf(BarcodeFormat.QR_CODE)
        scanMode = ScanMode.CONTINUOUS

        decodeCallback = DecodeCallback {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.onQrRecognized(it.text.toIntOrNull())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}