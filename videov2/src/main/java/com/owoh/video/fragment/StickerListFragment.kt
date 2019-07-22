package com.owoh.video.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.owoh.R
import com.owoh.video.ItemSticker
import com.owoh.video.adapter.StickerItemListAdapter
import com.owoh.video.utils.DownLoadService
import com.owoh.video.widget.GridDecoration
import org.greenrobot.eventbus.EventBus

/**
 * Created by burgess
 * 编辑图片界面的贴图功能下的每一个viewPager的fragment
 */
class StickerListFragment : Fragment() {
    private lateinit var binding: com.owoh.databinding.FragmentStickerItemListBinding
    private lateinit var stickerListBo: ItemSticker.ItemStickerIndexList
    private var currentSeleted: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stickerListBo = arguments?.getSerializable(BO) as ItemSticker.ItemStickerIndexList
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sticker_item_list, container, false)
        initView()
        return binding.root
    }

    private fun initView() {

        val manager = GridLayoutManager(activity, 5)
        binding.recyclerview.layoutManager = manager as RecyclerView.LayoutManager?
        binding.recyclerview.addItemDecoration(GridDecoration(16, 5))
        var listAdapter = StickerItemListAdapter(context, stickerListBo?.stickers)
        binding.recyclerview.adapter = listAdapter
        DownLoadService.getInstance().setDownloadListener { path ->
            stickerListBo?.stickers?.get(currentSeleted)?.big_image = path
            Log.e("Harrison","******type**"+stickerListBo?.stickers?.get(currentSeleted)?.type)
            EventBus.getDefault().post(stickerListBo?.stickers?.get(currentSeleted))
        }
        var savePath = Environment.getExternalStorageDirectory().toString() + "/OwOh/download/sticker"
        listAdapter.setOnItemClickListener {
            currentSeleted = it
            DownLoadService.getInstance().downloadFile(stickerListBo?.stickers?.get(it)?.big_image, savePath)

        }

    }

    companion object {
        val BO = "BO"
    }
}