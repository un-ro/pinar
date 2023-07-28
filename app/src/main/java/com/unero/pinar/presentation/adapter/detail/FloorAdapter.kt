package com.unero.pinar.presentation.adapter.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unero.pinar.R
import com.unero.pinar.data.model.POI
import com.unero.pinar.databinding.ItemRoomBinding
import com.unero.pinar.presentation.adapter.diffutil.NodeDiffUtil
import com.unero.pinar.presentation.detail.DetailFragmentDirections

class FloorAdapter(
    private val isPathReady: Boolean
) : RecyclerView.Adapter<FloorAdapter.ViewHolder>() {

    private var data = listOf<POI>()

    fun setData(newData: List<POI>) {
        val diffUtil = NodeDiffUtil(data, newData)
        val diffResult = calculateDiff(diffUtil)
        data = newData
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        val binding: ItemRoomBinding,
        private val isPathReady: Boolean
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: POI) {
            if (item.name == item.id) {
                binding.tvName.text = item.name
            } else {
                binding.apply {
                    tvName.text = item.name
                    tvCode.text = item.id
                }
            }

            binding.ivClass.apply {
                load(R.drawable.classroom)
                contentDescription = "Classroom"
            }

            binding.btnDestination.apply {
                visibility = if (isPathReady) View.VISIBLE else View.GONE
                setOnClickListener {
                    findNavController().navigate(
                        DetailFragmentDirections.toScanFragment(item)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, isPathReady)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}