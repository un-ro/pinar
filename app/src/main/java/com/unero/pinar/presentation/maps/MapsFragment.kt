package com.unero.pinar.presentation.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.unero.pinar.R
import com.unero.pinar.databinding.FragmentMapsBinding
import com.unero.pinar.presentation.maps.dialog.MapsDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MapsFragment : Fragment() {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding as FragmentMapsBinding

    private val viewModel by sharedViewModel<MapsViewModel>()

    private val mapCallback = OnMapReadyCallback { googleMap ->
        googleMap.apply {
            isTrafficEnabled = true
            isBuildingsEnabled = true
            moveCamera(CameraUpdateFactory.newLatLngZoom(PIN_POINT, ZOOM))
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style
            ))
            uiSettings.isScrollGesturesEnabled = true
            viewModel.locations.observe(viewLifecycleOwner) { result ->
                for (building in result) {
                    val location = LatLng(building.location.latitude, building.location.longitude)
                    addMarker(
                        MarkerOptions()
                            .position(location)
                            .title(building.id)
                    )
                }
            }
            setOnMarkerClickListener {
                showMapDialog(it.title ?: "NA")
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getBuildings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = binding.map.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(mapCallback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMapDialog(id: String) {
        val dialogFragment = MapsDialogFragment.newInstance(id)
        dialogFragment.show(childFragmentManager, "maps")
    }

    companion object {
        val PIN_POINT = LatLng(-7.945504830918152, 112.61518381573975)
        const val ZOOM = 17f
    }
}