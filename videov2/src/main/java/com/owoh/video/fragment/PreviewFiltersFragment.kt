package com.owoh.video.fragment

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType
import com.owoh.R
import com.owoh.video.adapter.PreviewResourceAdapter
import com.owoh.video.engine.render.PreviewRenderer
import com.owoh.video.media.VideoRenderer
import com.owoh.video.widget.GridDecoration
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

/**
 * 分镜页面和滤镜的页面
 */
class PreviewFiltersFragment : Fragment() {
    private var mPreviewResourceAdapter: PreviewResourceAdapter? = null

    private val mResourceData = ArrayList<ResourceData>()
    private var mCurrentPosition = -1
    //滤镜分类
    private var mFilterType: Int = 0
    //视频的模式，1拍摄模式 2.编辑模式
    private var mVideoModeType: Int = 0
    private var mVideoRenderer: VideoRenderer? = null


    // 布局管理器
    private var mActivity: Activity? = null

    val currentColorFilter: DynamicColorData?
        get() {
            if (mResourceData != null && mCurrentPosition >= 0 && mCurrentPosition < mResourceData.size) {
                val folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + mResourceData[mCurrentPosition].unzipFolder
                try {
                    val color = ResourceJsonCodec.decodeFilterData(folderPath)
                    val colorData = color.filterList[0]
                    colorData.vsPath = folderPath + colorData.vertexShader
                    colorData.fsPath = folderPath + File.separator + colorData.fragmentShader

                    return color.filterList[0]
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
            return null
        }

    fun setVideoRenderer(mVideoRenderer: VideoRenderer) {
        this.mVideoRenderer = mVideoRenderer
    }

    //Retention 是元注解，简单地讲就是系统提供的，用于定义注解的“注解”
    @Retention(RetentionPolicy.SOURCE)
    //这里指定int的取值只能是以下范围
    @IntDef(TYPE_CAMERA_FILTER, TYPE_COLOR_FILTER)
    internal annotation class FilterDef

    @Retention(RetentionPolicy.SOURCE)
    //这里指定int的取值只能是以下范围
    @IntDef(TYPE_VIDEO_SHOT, TYPE_VIDEO_EIDTEXT)
    internal annotation class VideoModeDef

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            mFilterType = bundle.getInt(KEY_FILTER_TYPE)
            mVideoModeType = bundle.getInt(KEY_VIDEO_MODE_TYPE)
        }

        initView()
        initFilterData()
    }

    //初始化分镜或者是滤镜资源
    private fun initFilterData() {
        if (mFilterType == TYPE_CAMERA_FILTER) {
            ResourceHelper.initCameraFilterResource(mActivity, mResourceData)
        } else if (mFilterType == TYPE_COLOR_FILTER) {
            ResourceHelper.initColorFilterResource(mActivity, mResourceData)
        }
        //        mResourceData.addAll( mFilterType == TYPE_COLOR_FILTER ? ResourceHelper.getColorFilter() : ResourceHelper.getCamerFilter());
        mPreviewResourceAdapter!!.notifyDataSetChanged()
    }


    private lateinit var binding: com.owoh.databinding.FragmentPreviewResourceBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_resource, container, false)

        return binding?.root
    }

    private fun initView() {
        binding?.apply {
            title.text = getString(R.string.camera_filter)

            val manager = GridLayoutManager(mActivity, 5)
            previewResourceList.addItemDecoration(GridDecoration(16, 5))
            previewResourceList.layoutManager = manager
            mPreviewResourceAdapter = PreviewResourceAdapter(mActivity, mResourceData)
            previewResourceList.adapter = mPreviewResourceAdapter
            mPreviewResourceAdapter!!.setOnResourceChangeListener { resourceData, currentPosition ->
                mCurrentPosition = currentPosition
                parseResource(resourceData.type, resourceData.unzipFolder)
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

    /**
     * 解码资源
     *
     * @param type        资源类型
     * @param unzipFolder 资源所在文件夹
     */
    private fun parseResource(type: ResourceType?, unzipFolder: String) {
        if (type == null) {
            return
        }
        try {
            when (type) {
                // 单纯的滤镜
                ResourceType.FILTER -> {
                    val folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder
                    val color = ResourceJsonCodec.decodeFilterData(folderPath)
                    color.colorType = ResourceType.FILTER.index
                    Log.e("Harrison", "FILTER")
                    //滤镜分拍摄模式还是编辑模式
                    if (mVideoModeType == TYPE_VIDEO_EIDTEXT) {
                        Log.e("Harrison", "TYPE_VIDEO_EIDTEXT")
                        if (mVideoRenderer != null)
                            mVideoRenderer!!.changeDynamicColorFilter(color)
                    } else if (mVideoModeType == TYPE_VIDEO_SHOT) {
                        Log.e("Harrison", "TYPE_VIDEO_SHOT")
                        PreviewRenderer.getInstance().changeDynamicColorFilter(color)
                    }
                }// 摄像机分镜
                ResourceType.CAMERA_FILTER -> {
                    val folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder
                    val color = ResourceJsonCodec.decodeFilterData(folderPath)
                    color.colorType = ResourceType.CAMERA_FILTER.index
                    PreviewRenderer.getInstance().changeDynamicCameraFilter(color)
                }

                // 贴纸
                ResourceType.STICKER -> {
                    val folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder
                    val sticker = ResourceJsonCodec.decodeStickerData(folderPath)
                    PreviewRenderer.getInstance().changeDynamicResource(sticker)
                }

                // TODO 多种结果混合
                ResourceType.MULTI -> {
                }

                // 所有数据均为空
                ResourceType.NONE -> {
                    //滤镜分拍摄模式还是编辑模式
                    if (mVideoModeType == TYPE_VIDEO_EIDTEXT) {
                        if (mVideoRenderer != null)
                            mVideoRenderer!!.changeDynamicColorFilter(DynamicColor().setColorType(ResourceType.FILTER.index))
                    } else if (mVideoModeType == TYPE_VIDEO_SHOT) {
                        if (mFilterType == TYPE_COLOR_FILTER) {
                            PreviewRenderer.getInstance().removeDynamic(DynamicColor().setColorType(ResourceType.FILTER.index))
                        } else {
                            //移除分镜的
                            PreviewRenderer.getInstance().removeDynamic(DynamicColor().setColorType(ResourceType.CAMERA_FILTER.index))
                        }
                    }
                }
                else -> {
                }
            }
        } catch (e: Exception) {

        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        private val TAG = "PreviewFiltersFragment"
        private val KEY_FILTER_TYPE = "filterType"
        private val KEY_VIDEO_MODE_TYPE = "videoModeType"

        //数据模式， 分镜
        const val TYPE_CAMERA_FILTER = 0
        //颜色方面的滤镜
        const val TYPE_COLOR_FILTER = 1

        //拍摄
        const val TYPE_VIDEO_SHOT = 0
        //编辑
        const val TYPE_VIDEO_EIDTEXT = 1


        fun getInstance(@FilterDef filterType: Int, @VideoModeDef videoModeType: Int): PreviewFiltersFragment {
            val bundle = Bundle()
            bundle.putInt(KEY_FILTER_TYPE, filterType)
            bundle.putInt(KEY_VIDEO_MODE_TYPE, videoModeType)
            val filtersFragment = PreviewFiltersFragment()
            filtersFragment.arguments = bundle
            return filtersFragment
        }
    }
}
