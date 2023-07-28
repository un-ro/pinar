package com.unero.pinar.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.unero.pinar.databinding.FragmentLevelBinding
import com.unero.pinar.presentation.adapter.detail.FloorAdapter
import com.unero.pinar.presentation.sharedviewmodel.NavigationViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LevelFragment : Fragment() {

    private var _binding: FragmentLevelBinding? = null
    private val binding get() = _binding as FragmentLevelBinding

    private val viewModel by sharedViewModel<NavigationViewModel>()

    private var floor: Int = 0

    private lateinit var floorAdapter: FloorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floor = arguments?.getInt("FLOOR") ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomList = viewModel.roomNodesByFloor(floor)

        if (roomList.isNotEmpty()) {
            floorAdapter = FloorAdapter(viewModel.isStartAvailable())
            floorAdapter.setData(roomList)
            binding.rvLevel.apply {
                adapter = floorAdapter
                setHasFixedSize(true)
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        LinearLayoutManager.VERTICAL
                    )
                )
            }
        } else {
            binding.noData.visibility = View.VISIBLE
            binding.rvLevel.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FLOOR = "FLOOR"

        fun newInstance(floor: Int) = LevelFragment().apply {
            arguments = bundleOf(FLOOR to floor)
        }
    }
}