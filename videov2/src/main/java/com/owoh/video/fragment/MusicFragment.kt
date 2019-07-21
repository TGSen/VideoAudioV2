package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.owoh.R
import com.owoh.video.adapter.MusicAdapter
import com.owoh.video.media.bgmusic.ItemMusic
import com.owoh.video.utils.DownLoadService
import com.owoh.video.widget.GridDecoration


/**
 *
 */
class MusicFragment : Fragment() {
    private var musicAdapter: MusicAdapter? = null

    private val mResourceData = ArrayList<ItemMusic>()

    private var onMusicChangeListener: OnMusicChangeListener? = null


    private var mActivity: Activity? = null
    private var currentSeleted: Int = -1

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
        initMusicData()
    }

    private fun initMusicData() {
        mResourceData.clear()
        var gson = Gson()
        var jsonParser = JsonParser()
        var jsonElements = jsonParser.parse(ItemMusic.jason).asJsonArray;//获取JsonArray对象
        for (value in jsonElements) {
            var item = gson.fromJson(value, ItemMusic::class.javaObjectType)
            mResourceData.add(item)
        }
        musicAdapter!!.notifyDataSetChanged()
    }

    private var mTitle: String? = null
    public fun setTitle(title: String) {

        mTitle = title

    }

    private fun initView() {
        binding?.apply {
            if (TextUtils.isEmpty(mTitle)) {
                title.text = getString(R.string.music)
            } else {
                title.text = mTitle
            }


            val manager = GridLayoutManager(mActivity, 5)
            previewResourceList.layoutManager = manager as RecyclerView.LayoutManager?
            musicAdapter = MusicAdapter(mActivity, mResourceData)
            previewResourceList.adapter = musicAdapter
            previewResourceList.addItemDecoration(GridDecoration(16, 5))
            DownLoadService.getInstance().setDownloadListener { path -> onMusicChangeListener?.change(path, mResourceData[currentSeleted].getName_cn()) }
            var savePath = Environment.getExternalStorageDirectory().toString() + "/OwOh/download/music"
            musicAdapter?.setOnItemClickListener { position ->
                //            onMusicChangeListener?.change(mResourceData[position].getMusic())
//            //切换音乐
                if (onMusicChangeListener != null) {
                    if (position == 0) {
                        onMusicChangeListener?.change("", "")
                        return@setOnItemClickListener
                    }
                    currentSeleted = position
                    Log.e("Harrison", "*******download:" + mResourceData[position].getMusic())
                    DownLoadService.getInstance().downloadFile(mResourceData[position].getMusic(), savePath)
                }
            }
        }

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

    fun setOnMusicChangeListener(listener: OnMusicChangeListener) {
        this.onMusicChangeListener = listener
    }

    /**
     * music 切换监听
     */
    interface OnMusicChangeListener {
        fun change(url: String?, name: String?)
    }

    companion object {

        private val TAG = "PreviewFiltersFragment"


        val instance: MusicFragment
            get() {
                val bundle = Bundle()
                val filtersFragment = MusicFragment()
                filtersFragment.arguments = bundle
                return filtersFragment
            }
    }
}
