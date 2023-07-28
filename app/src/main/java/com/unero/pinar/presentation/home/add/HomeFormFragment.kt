package com.unero.pinar.presentation.home.add

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.*
import com.unero.pinar.R
import com.unero.pinar.data.model.Building
import com.unero.pinar.data.model.Location
import com.unero.pinar.databinding.FragmentHomeFormBinding
import com.unero.pinar.utils.Event
import com.unero.pinar.utils.PermissionHelper.hasLocationPermission
import com.unero.pinar.utils.PermissionHelper.requestLocationPermission
import com.unero.pinar.utils.UIHelper.popBackFragment
import com.unero.pinar.utils.UIHelper.showSnackbar
import com.unero.pinar.utils.UIHelper.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFormFragment : Fragment() {
    private var _binding: FragmentHomeFormBinding? = null
    private val binding get() = _binding as FragmentHomeFormBinding
    private val args: HomeFormFragmentArgs by navArgs()
    private val building: Building? by lazy { args.building }

    private val viewModel: HomeFormViewModel by viewModel()

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 30000
            fastestInterval = 10000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            isWaitForAccurateLocation = true
        }
    }

    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                if (result.locations.size > 0) {
                    binding.apply {
                        etLatitude.setText(result.locations[0].latitude.toString())
                        etLongitude.setText(result.locations[0].longitude.toString())
                    }
                }
            }
        }
    }

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getThumbnail()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener { popBackFragment(it) }
        setupThumbnail()
        setupType()

        binding.btnLocation.setOnClickListener {
            if (hasLocationPermission(requireContext())) {
                lifecycleScope.launch {
                    locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                    delay(5000)
                    locationClient.removeLocationUpdates(locationCallback)
                }
            } else {
                requestLocationPermission(requireActivity())
            }
        }

        binding.btnAdd.setOnClickListener {
            lifecycleScope.launch {
                if (checkingInput()) {
                    showToast(requireContext(), "Adding...")
                    viewModel.addBuilding(extract())
                } else {
                    showToast(requireContext(), "Please fill required fields")
                }
            }
        }

        if (building != null) {
            with(binding) {
                lblTitle.text = "Edit Building"
                etId.isEnabled = false
                etId.setText(building!!.id)
                etName.setText(building!!.name)
                etDescription.setText(building!!.description)
                etFloor.setText(building!!.floors.toString())
                etLatitude.setText(building!!.location.latitude.toString())
                etLongitude.setText(building!!.location.longitude.toString())
                etThumbnail.setText(building!!.thumbnail)
                btnAdd.text = "Save"
            }
        }

        binding.etType.setOnItemClickListener { _, _, itemPosition, _ ->
            binding.etName.isEnabled = itemPosition != 0
        }
    
        viewModel.message.observe(viewLifecycleOwner) { listenStatus(it) }
    }

    private fun checkingInput(): Boolean {
        val requireDone = (
                binding.etId.text.toString().isNotEmpty() &&
                binding.etFloor.text.toString().isNotEmpty() &&
                binding.etDescription.text.toString().isNotEmpty() &&
                binding.etLatitude.text.toString().isNotEmpty() &&
                binding.etLongitude.text.toString().isNotEmpty() &&
                binding.etThumbnail.text.toString().isNotEmpty())

        val optionalDone = if (binding.etType.text.toString().lowercase() != "academy") {
            binding.etName.text.toString().isNotEmpty()
        } else {
            true
        }

        return requireDone && optionalDone
    }

    private fun listenStatus(event: Event<Boolean>) {
        if (!event.hasBeenHandled) {
            if (event.peekContent()) {
                showToast(requireContext(), "Success Change/Add Building")
                popBackFragment(requireView())
            } else
                showSnackbar(requireView(), "Error Change/Add Building")
        }
    }

    private fun extract(): Building = Building(
            binding.etId.text.toString(),
            binding.etName.text.toString().ifBlank { "" },
            binding.etDescription.text.toString(),
            binding.etFloor.text.toString().toInt(),
            binding.etType.text.toString().lowercase(),
            binding.etThumbnail.text.toString(),
            Location(
                binding.etLatitude.text.toString().toDouble(),
                binding.etLongitude.text.toString().toDouble()
            )
        )

    private fun setupThumbnail() {
        viewModel.thumbnailList.observe(viewLifecycleOwner) {
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                R.layout.item_dropdown,
                it
            )
            binding.etThumbnail.setAdapter(arrayAdapter)
        }
    }

    private fun setupType() {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            resources.getStringArray(R.array.type)
        )
        binding.etType.setAdapter(arrayAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}