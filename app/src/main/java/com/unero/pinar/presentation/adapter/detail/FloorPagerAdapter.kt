package com.unero.pinar.presentation.adapter.detail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.unero.pinar.presentation.detail.LevelFragment

class FloorPagerAdapter(
    fragment: Fragment,
    private val floors: Int
): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = floors

    override fun createFragment(position: Int): Fragment = LevelFragment.newInstance(position + 1)
}