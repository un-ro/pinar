package com.unero.pinar.presentation.adapter.home

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.unero.pinar.R
import com.unero.pinar.data.model.Building
import com.unero.pinar.databinding.ItemBuildingBinding
import com.unero.pinar.presentation.adapter.diffutil.BuildingDiffUtil
import com.unero.pinar.presentation.home.HomeFragmentDirections
import com.unero.pinar.utils.StorageHelper.getThumbnail
import com.unero.pinar.utils.UIHelper.buildingName
import com.unero.pinar.utils.UIHelper.goToFragment

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private var data = listOf<Building>()

    fun setData(newData: List<Building>) {
        val diffUtil = BuildingDiffUtil(data, newData)
        val diffResults = calculateDiff(diffUtil)
        data = newData
        diffResults.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(
        private val binding: ItemBuildingBinding,
        private val context: Context
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Building) {
            binding.tvName.text = buildingName(item, context)
            if (item.thumbnail.isEmpty()) {
                loadThumbnail(null)
            } else {
                getThumbnail(item.thumbnail)
                    .addOnSuccessListener { loadThumbnail(it) }
                    .addOnFailureListener { loadThumbnail(null) }
            }
            binding.root.setOnClickListener {
                val action = HomeFragmentDirections.toDetailFragment(item)
                goToFragment(it, action)
            }
        }

        private fun loadThumbnail(uri: Uri?) {
            if (uri != null) {
                binding.ivBuilding.load(uri) {
                    crossfade(true)
                    crossfade(200)
                    placeholder(R.drawable.ic_image)
                }
            } else {
                binding.ivBuilding.load(R.drawable.ic_image)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemBuildingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}