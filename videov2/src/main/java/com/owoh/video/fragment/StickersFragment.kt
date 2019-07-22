package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cgfay.filterlibrary.utils.DensityUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.owoh.R
import com.owoh.video.ItemSticker
import com.owoh.video.adapter.StickerIndexAdapter
import com.owoh.video.adapter.StickerViewPageAdapter
import com.owoh.video.media.VideoRenderer
import com.owoh.video.widget.SpaceItemDecoration
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 *
 */
class StickersFragment : Fragment() {
    private var stickersIndexAdapter: StickerIndexAdapter? = null

    private val mStickerIndex = ArrayList<ItemSticker.ItemStickerIndex>()
    private val mStickerIndexList = ArrayList<ItemSticker.ItemStickerIndexList>()

    private var mVideoRenderer: VideoRenderer? = null

    private var mHandler = Handler(Looper.getMainLooper())
    private var mActivity: Activity? = null

    fun setVideoRenderer(mVideoRenderer: VideoRenderer) {
        this.mVideoRenderer = mVideoRenderer
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
    }

    private lateinit var binding: com.owoh.databinding.FragmentStickerListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sticker_list, container, false)
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
        EXECUTOR.execute {

            var gson = Gson()
            var jsonParser = JsonParser()
            var jsonElements = jsonParser.parse(ItemSticker.jason).asJsonArray;//获取JsonArray对象
            for (value in jsonElements) {
                var item = gson.fromJson(value, ItemSticker::class.javaObjectType)

                var itemStickerIndex = ItemSticker.ItemStickerIndex()
                itemStickerIndex.id = item.getId()
                itemStickerIndex.name_cn = item.getName_cn()
                itemStickerIndex.name_tw = item.getName_tw()
                itemStickerIndex.name_en = item.getName_en()
                itemStickerIndex.type = item.getType()
                itemStickerIndex.seq = item.getSeq()
                mStickerIndex.add(itemStickerIndex)

                var itemStickerList = ItemSticker.ItemStickerIndexList()

                var stickers: List<ItemSticker.StickersBean>? = item.getStickers()
                stickers?.let {
                    for (value in stickers) {
                        value.type = item.getType()
                        itemStickerList.stickers?.add(value)
                    }
                }
                var stickers2: List<ItemSticker.StickersBean>? = itemStickerList.stickers
                stickers2?.let {
                    for (value in stickers2) {
                        Log.e("Harrison", "********" + value.type + "***" + value.big_image)
                    }
                }

                mStickerIndexList.add(itemStickerList)

            }

            mHandler.post {
                if (mStickerIndex.size > 0) {
                    mStickerIndex[0].isSelected = true
                }
                stickersIndexAdapter?.notifyDataSetChanged()
                var stickerListAdapter = activity?.let { StickerViewPageAdapter(it, childFragmentManager, mStickerIndexList) }
                stickerListAdapter.let {
                    binding.viewPager.adapter = stickerListAdapter
                    binding.viewPager.currentItem = 0;
                }
            }
        }

    }


    private fun initView() {
        binding?.apply {
            title.text = getString(R.string.video_sticker)

            val manager = LinearLayoutManager(mActivity)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            previewResourceList.layoutManager = manager

            stickersIndexAdapter = StickerIndexAdapter(mActivity, mStickerIndex)
            previewResourceList.adapter = stickersIndexAdapter
            var db = DensityUtils.dp2px(activity, 8f)
            previewResourceList.addItemDecoration(SpaceItemDecoration(db, db))
            stickersIndexAdapter?.setOnItemClickListener { position ->
                binding.viewPager.currentItem = position
//                }
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


    companion object {

        private val TAG = "PreviewFiltersFragment"

        var EXECUTOR: Executor = Executors.newCachedThreadPool()
        val instance: StickersFragment
            get() {
                val bundle = Bundle()
                val filtersFragment = StickersFragment()
                filtersFragment.arguments = bundle
                return filtersFragment
            }
    }
}
