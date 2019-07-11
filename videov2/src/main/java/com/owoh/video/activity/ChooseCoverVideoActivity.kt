package com.owoh.video.activity


import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.cgfay.filterlibrary.utils.BitmapUtils
import com.cgfay.filterlibrary.utils.DensityUtils
import com.owoh.R
import com.owoh.databinding.ActivityEditextCoverVideoBinding
import com.owoh.video.adapter.ThumbVideoAdapter
import com.owoh.video.media.VideoRenderThread
import com.owoh.video.media.VideoRenderer
import com.owoh.video.media.bean.EFFECT_TYPE_SINGLE
import com.owoh.video.media.bean.RENDER_TYPE_AT_TIME
import com.owoh.video.media.bean.VideoEffectType
import com.owoh.video.video.ExtractFrameWorkThread
import com.owoh.video.video.VideoEditInfo
import com.owoh.video.widget.VideoPreviewView
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * @author Harrison 唐广森
 * @description: 选择封面
 */
class ChooseCoverVideoActivity : AppCompatActivity(), View.OnClickListener {
    private var videoPath: String? = null
    private var mVideoRenderer: VideoRenderer? = null
    private var mMaxWidth: Int = 0 //可裁剪区域的最大宽度
    private val MARGIN = 46
    private var mExtractFrameWorkThread: ExtractFrameWorkThread? = null
    private var mVideoEditAdapter: ThumbVideoAdapter? = null
    private var mVideoPreviewView: VideoPreviewView? = null

    override fun onClick(v: View?) {

    }

    private lateinit var binding: ActivityEditextCoverVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_editext_cover_video)
        mVideoRenderer = VideoRenderer()
        val mVideoEffectType = VideoEffectType()
                .setCurrentEffectType(EFFECT_TYPE_SINGLE)
                .setCurrentRendererType(RENDER_TYPE_AT_TIME)

        mVideoRenderer?.initRenderer(this.applicationContext, mVideoEffectType)
        iniView()
        initData()
    }

    private fun iniView() {
        binding.apply {
            binding.imgVideo?.visibility = View.GONE

            btSave.setOnClickListener(this@ChooseCoverVideoActivity)
            btCloseImag.setOnClickListener(this@ChooseCoverVideoActivity)

            mMaxWidth = DensityUtils.getDisplayWidthPixels(this@ChooseCoverVideoActivity)
            thumbRecyclerView?.layoutManager = LinearLayoutManager(this@ChooseCoverVideoActivity, LinearLayoutManager.HORIZONTAL, false)
            mVideoEditAdapter = ThumbVideoAdapter(this@ChooseCoverVideoActivity, mMaxWidth / 11)
            thumbRecyclerView?.adapter = mVideoEditAdapter
            // thumbRecyclerView?.addOnScrollListener(mOnScrollListener)

            //贴纸得显示时间选择

            mVideoPreviewView = VideoPreviewView(this@ChooseCoverVideoActivity)
            layoutAspect.addView(mVideoPreviewView, 0)
            layoutAspect.requestLayout()

        }

    }

    private val mHandler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                ExtractFrameWorkThread.MSG_SAVE_SUCCESS -> {
                    val info = msg.obj as VideoEditInfo
                    Log.e("Harrison", "*******")
                    mVideoEditAdapter?.addItemVideoInfo(info)
                    mVideoEditAdapter?.notifyDataSetChanged()
                }
            }
        }
    }


    private fun initData() {

        val bundle = intent.getBundleExtra(BUNDLE_VIDEO_PATH)
        videoPath = bundle.getString(KEY_VIDEO_PATH)
        //设置播放的视频路径
        mVideoRenderer?.setVideoPaths(videoPath)

        // 绑定需要渲染的SurfaceView
        mVideoRenderer?.setSurfaceView(mVideoPreviewView)
        //设置videoPlayer 状态监听
        mVideoRenderer?.setVideoPlayerStatusChangeLisenter(object : VideoRenderThread.VideoPlayerStatusChangeLisenter {
            override fun videoStart(totalTime: Int) {
                EXECUTOR.execute {
                    //获取第一帧图片并模糊

                    val bitmap = BitmapUtils.createVideoThumbnail(videoPath)
                    //                ImageBlur.blurBitmap(bitmap, 10);
                    runOnUiThread(Runnable {
                        binding.rootView.background = BitmapDrawable(resources, bitmap)
                        //如果存在的 话，那么就不用在截取了
                    })

                    val outPutFileDirPath = externalCacheDir?.absolutePath + "/"
                    val extractW = mMaxWidth / MAX_COUNT_RANGE
                    val extractH = DensityUtils.dp2px(this@ChooseCoverVideoActivity, 62f)
                    mExtractFrameWorkThread = ExtractFrameWorkThread(
                            extractW, extractH, mHandler, videoPath,
                            outPutFileDirPath, 0, totalTime.toLong(), MAX_COUNT_RANGE
                    )
                    mExtractFrameWorkThread?.start()
                }

            }

            override fun videoStop() {

            }

            override fun videoRestart() {


            }

            override fun videoCompleted() {

            }


        })


    }

    companion object {
        private val TAG = "Harrison"
        private val KEY_VIDEO_PATH = "videoPath"
        private val BUNDLE_VIDEO_PATH = "bundle"

        var EXECUTOR: Executor = Executors.newCachedThreadPool()

        // 设置video paths
        const val MSG_VIDEO_PLAY_PROGRESS = 0x001
        const val MSG_VIDEO_PLAY_STATUS_STOP = 0x002
        const val MSG_VIDEO_PLAY_STATUS_START = 0x003

        private const val MIN_CUT_DURATION = 0L// 最小剪辑时间3s
        private const val MAX_CUT_DURATION = 10 * 1000L//视频最多剪切多长时间
        private const val MAX_COUNT_RANGE = 10//seekBar的区域内一共有多少张图片

        fun gotoThis(context: Context, path: String) {
            val intent = Intent(context, ChooseCoverVideoActivity::class.java)
            val bundle = Bundle()
            bundle.putString(KEY_VIDEO_PATH, path)
            intent.putExtra(BUNDLE_VIDEO_PATH, bundle)
            context.startActivity(intent)
        }
    }


}
