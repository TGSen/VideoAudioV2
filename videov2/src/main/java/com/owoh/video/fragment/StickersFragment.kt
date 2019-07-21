package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.owoh.R
import com.owoh.video.ItemSticker
import com.owoh.video.adapter.StickersAdapter
import com.owoh.video.media.VideoRenderer
import java.util.*

/**
 *
 */
class StickersFragment : Fragment() {
    private var stickersAdapter: StickersAdapter? = null

    private val mResourceData = ArrayList<ItemSticker>()

    private var mVideoRenderer: VideoRenderer? = null
    private var onStickerAddListener: OnStickerPanlListener? = null


    private var mActivity: Activity? = null

    fun setVideoRenderer(mVideoRenderer: VideoRenderer) {
        this.mVideoRenderer = mVideoRenderer
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
    }

    private lateinit var binding: com.owoh.databinding.FragmentPreviewResourceBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_resource, container, false)

        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {

        }
        initView()
        initStickerData()
    }

    private fun initStickerData() {
        stickersAdapter!!.notifyDataSetChanged()
    }


    private fun initView() {
        binding?.apply {
            title.text = getString(R.string.video_sticker)

            val manager = GridLayoutManager(mActivity, 5)
            previewResourceList.layoutManager = manager
            mResourceData.addAll(ItemSticker.stickerList)
            stickersAdapter = StickersAdapter(mActivity!!, mResourceData)
            previewResourceList.adapter = stickersAdapter
            stickersAdapter!!.setOnItemClickListener { position ->
                if (onStickerAddListener != null) {
                    onStickerAddListener!!.addSticker(mResourceData[position])
                }
            }
        }

    }


    private fun initView(view: View) {


    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    fun setOnStickerAddListener(listener: OnStickerPanlListener) {
        this.onStickerAddListener = listener
    }

    /**
     * music 切换监听
     */
    interface OnStickerPanlListener {
        fun addSticker(url: ItemSticker)


    }

    companion object {

        private val TAG = "PreviewFiltersFragment"


        val instance: StickersFragment
            get() {
                val bundle = Bundle()
                val filtersFragment = StickersFragment()
                filtersFragment.arguments = bundle
                return filtersFragment
            }
    }
}
