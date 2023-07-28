package com.unero.pinar.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.unero.pinar.BuildConfig
import com.unero.pinar.R
import com.unero.pinar.data.model.Building
import com.unero.pinar.databinding.FragmentHomeBinding
import com.unero.pinar.presentation.adapter.home.HomeAdapter
import com.unero.pinar.utils.Response
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding as FragmentHomeBinding

    private val homeViewModel by viewModel<HomeViewModel>()

    private val homeAdapter: HomeAdapter = HomeAdapter()

    private var isLoading: Boolean = false
        set(value) {
            field = value
            binding.loadingView.isGone = !value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.DEBUG) {
            binding.fabAdd.apply {
                show()
                setOnClickListener {
                    findNavController().navigate(HomeFragmentDirections.toAddBuildingFragment(null))
                }
            }
        }

        setupUI()

        binding.btnRetry.setOnClickListener {
            homeViewModel.getBuildings()
            setupUI()
            showException(false)
        }
    }

    private fun setupUI() {
        homeViewModel.buildingState.observe(viewLifecycleOwner) { response ->
            isLoading = when (response) {
                Response.Loading -> true
                is Response.Success -> {
                    val data = response.data

                    if (data.isNotEmpty())
                        setupRV(response.data)
                    else
                        showException(true, "No data found", false)
                    false
                }
                is Response.Error -> {
                    showException(true, response.message)
                    false
                }
            }
        }
    }

    private fun showException(
        show: Boolean,
        message: String = getString(R.string.error_occurred),
        showButton: Boolean = true
    ) {
        binding.exceptionLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.tvException.text = message
        binding.btnRetry.visibility = if (showButton) View.VISIBLE else View.GONE
    }

    private fun setupRV(list: List<Building>) {
        val filtered = list.filter { it.type == TYPE }
        homeAdapter.setData(filtered)
        binding.rv.apply {
            adapter = homeAdapter
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TYPE = "academy"
    }
}