package com.owoh.video.adapter

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.owoh.video.ItemSticker
import com.owoh.video.fragment.StickerListFragment
import com.owoh.video.fragment.StickerListFragment.Companion.BO


class StickerViewPageAdapter(
    private val context: Context,
    fm: FragmentManager,
    private val categoryList: ArrayList<ItemSticker.ItemStickerIndexList>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val fragment = StickerListFragment()
        fragment.arguments = Bundle()
        fragment.arguments?.putSerializable(BO, categoryList[position])
        return fragment
    }

    override fun getCount(): Int {
        return categoryList.size
    }
}
