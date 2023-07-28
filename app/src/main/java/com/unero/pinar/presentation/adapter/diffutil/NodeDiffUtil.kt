package com.unero.pinar.presentation.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.unero.pinar.data.model.POI

class NodeDiffUtil(
    private val oldList: List<POI>,
    private val newList: List<POI>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> {
                false
            }
            else -> true
        }
    }
}