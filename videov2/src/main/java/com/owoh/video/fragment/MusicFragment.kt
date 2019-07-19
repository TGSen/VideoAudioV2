package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.owoh.R
import com.owoh.video.adapter.MusicAdapter
import com.owoh.video.media.VideoRenderer
import com.owoh.video.media.bgmusic.ItemMusic
import com.owoh.video.utils.DownLoadService



/**
 *
 */
class MusicFragment : Fragment() {
    private var musicAdapter: MusicAdapter? = null

    private val mResourceData = ArrayList<ItemMusic>()

    private var onMusicChangeListener: OnMusicChangeListener? = null

    // 内容显示列表
    private var mContentView: View? = null

    // 贴纸列表
    private var mResourceView: RecyclerView? = null

    private var mActivity: Activity? = null



    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.fragment_preview_resource, container, false)
        return mContentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {

        }
        initView(mContentView!!)
        initMusicData()
    }

    private fun initMusicData() {
        mResourceData.clear()
        var gson = Gson()
        var jsonParser = JsonParser()
        var jsonElements = jsonParser.parse(ItemMusic.jason).asJsonArray;//获取JsonArray对象
        for (value in jsonElements) {
            var item =  gson.fromJson(value, ItemMusic::class.javaObjectType)
                    mResourceData . add (item)
        }
        musicAdapter!!.notifyDataSetChanged()
    }


    private fun initView(view: View) {
        mResourceView = view.findViewById(R.id.preview_resource_list)

        val manager = GridLayoutManager(mActivity, 5)
        mResourceView!!.layoutManager = manager
        musicAdapter = MusicAdapter(mActivity!!, mResourceData)
        mResourceView!!.adapter = musicAdapter
        DownLoadService.getInstance().setDownloadListener { path -> onMusicChangeListener?.change(path) }
        var savePath = Environment.getExternalStorageDirectory().toString() + "/OwOh/download/music"
        musicAdapter!!.setOnItemClickListener { position ->
//            onMusicChangeListener?.change(mResourceData[position].getMusic())
//            //切换音乐
            if (onMusicChangeListener != null) {
                Log.e("Harrison","*******download:"+mResourceData[position].getMusic())
                DownLoadService.getInstance().downloadFile( mResourceData[position].getMusic(),savePath)
            }
        }
    }

    override fun onDestroyView() {
        mContentView = null
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
        fun change(url: String?)
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
