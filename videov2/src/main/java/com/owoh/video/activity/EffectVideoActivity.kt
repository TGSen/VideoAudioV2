package com.owoh.video.activity

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.constraint.Group
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.transition.Transition
import android.transition.TransitionManager
import android.transition.TransitionValues
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils
import com.cgfay.filterlibrary.mp4compose.FillMode
import com.cgfay.filterlibrary.mp4compose.composer.Mp4Composer
import com.cgfay.filterlibrary.mp4compose.filter.GlFilterGroup
import com.cgfay.filterlibrary.utils.BitmapUtils
import com.cgfay.filterlibrary.utils.DensityUtils
import com.cgfay.filterlibrary.utils.StringUtils
import com.owoh.R
import com.owoh.video.ItemSticker
import com.owoh.video.adapter.EffectResourceAdapter
import com.owoh.video.adapter.ThumbVideoAdapter
import com.owoh.video.filter.GLColorFilter
import com.owoh.video.filter.GLEffectFilter
import com.owoh.video.filter.GLStickerFilter
import com.owoh.video.fragment.PreviewFiltersFragment
import com.owoh.video.fragment.StickersFragment
import com.owoh.video.fragment.VoiceAdjustFragment
import com.owoh.video.media.VideoRenderThread
import com.owoh.video.media.VideoRenderer
import com.owoh.video.media.bean.EFFECT_TYPE_SINGLE
import com.owoh.video.media.bean.RENDER_TYPE_AT_TIME
import com.owoh.video.media.bean.VideoEffect
import com.owoh.video.media.bean.VideoEffectType
import com.owoh.video.video.ExtractFrameWorkThread
import com.owoh.video.video.VideoEditInfo
import com.owoh.video.widget.*
import com.owoh.video.widget.sticker.*
import com.tencent.bugly.crashreport.CrashReport
import pl.droidsonroids.gif.GifDrawable
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author Harrison 唐广森
 * @description: 特效编辑
 * 带有时间的特效，重叠的时候，有添加和后加的重叠的区别
 * @date :2019/4/1 14:29
 */
class EffectVideoActivity : AppCompatActivity(), View.OnClickListener {
    // 显示滤镜页面
    private var isShowingFilters = false
    private val mDynamicColorFilter = SparseArray<DynamicColor>()

    private var videoPath: String? = null
    private var mVideoPreviewView: VideoPreviewView? = null
    private var mVideoRenderer: VideoRenderer? = null
    private var mMaxWidth: Int = 0 //可裁剪区域的最大宽度
    private var currentEffectKey: Int = 0
    private var isStartClick: Boolean = false

    // 滤镜页面
    private var mColorFilterFragment: PreviewFiltersFragment? = null
    // 滤镜页面
    private var mStickerFragment: StickersFragment? = null
    //
    private var mVoiceAdjustFragment: VoiceAdjustFragment? = null


    /**
     * 记录 video 的特效时间
     */
    private val mVideoEffects = ArrayList<VideoEffect>()

    private val mHandler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_VIDEO_PLAY_PROGRESS -> try {
                    val progress = mVideoRenderer!!.videoProgress
                    val time = StringUtils.generateTime(progress.toLong())
                    tvStartTime!!.text = if (TextUtils.isEmpty(time)) "00:00" else time
                    mSeekBar!!.progress = progress
                    this.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS)

                    //改变贴纸的显示时间
                    mStickerSeekBar!!.progress = progress
                } catch (e: Exception) {
                    Log.e("Harrison", "****" + e.localizedMessage)
                }

                MSG_VIDEO_PLAY_STATUS_STOP ->
                    //贴纸的暂停不需要显示
                    if (layoutStickerTool!!.visibility == View.GONE)
                        mVideoPlayStatus!!.visibility = View.VISIBLE
                MSG_VIDEO_PLAY_STATUS_START -> mVideoPlayStatus!!.visibility = View.GONE
                ExtractFrameWorkThread.MSG_SAVE_SUCCESS -> {
                    val info = msg.obj as VideoEditInfo
                    Log.e("Harrison", "info:" + info.path!!)
                    mVideoEditAdapter!!.addItemVideoInfo(info)
                    mVideoEditAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }
    private var mRootView: ConstraintLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mThumbRecyclerView: RecyclerView? = null
    private var tvTotalTime: TextView? = null
    private var tvStartTime: TextView? = null
    private var mStickerTimeTv: TextView? = null
    private var mVideoPlayStatus/*,imgVideoSmall*/: ImageView? = null
    private var mSeekBar: VideoEffectSeekBar? = null
    private var mStickerSeekBar: DragSeekBar? = null
    private val mResourceData = ArrayList<ResourceData>()
    private var mPreviewResourceAdapter: EffectResourceAdapter? = null
    private val mStartClickTime: Long = 0
    private var currentVideoEffectIndex = -1
    private var newVideoEffect: VideoEffect? = null
    private var btSave: TextView? = null
    private var isVideoPlayCompleted: Boolean = false
    private var mainGroup: Group? = null
    private var effectGroup: Group? = null
    private var layoutStickerTool: ConstraintLayout? = null
    private var mAspectLayout: FrameLayout? = null
    private var btCloseImag: ImageView? = null
    private var mStickerView: StickerView? = null
    private var mRangeSeekBar: RangeSeekBar? = null
    private val MARGIN = 46
    private var mExtractFrameWorkThread: ExtractFrameWorkThread? = null
    private var mVideoEditAdapter: ThumbVideoAdapter? = null
    private var averageMsPx: Float = 0.toFloat()
    private var isCombine: Boolean = false
    private var glStickerFilter: GLStickerFilter? = null
    private var mEffectFilter: GLEffectFilter? = null
    //该视频是否有该截图
    private var isVideoRange: Boolean = false

    private var leftProgress: Long = 0
    private var rightProgress: Long = 0 //裁剪视频左边区域的时间位置, 右边时间位置
    private var scrollPos: Long = 0
    private val mScaledTouchSlop: Int = 0
    private var lastScrollX: Int = 0
    private var isSeeking: Boolean = false
    private var isOverScaledTouchSlop: Boolean = false
    private var thumbnailsCount: Int = 0
    private var rangeWidth: Int = 0

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            Log.e(TAG, "-------newState:>>>>>$newState")
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isSeeking = false
                //                videoStart();
            } else {
                isSeeking = true
                //                if (isOverScaledTouchSlop) {
                //                   // videoPause();
                //                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            isSeeking = false
            val scrollX = scrollXDistance
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false
                return
            }
            isOverScaledTouchSlop = true
            Log.e(TAG, "-------scrollX:>>>>>$scrollX")
            //初始状态,why ? 因为默认的时候有56dp的空白！
            if (scrollX == -MARGIN) {
                scrollPos = 0
            } else {
                // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
                // videoPause();
                isSeeking = true
                scrollPos = (averageMsPx * (MARGIN + scrollX)).toLong()
                Log.e(TAG, "-------scrollPos:>>>>>$scrollPos")
                leftProgress = mRangeSeekBar!!.selectedMinValue + scrollPos
                rightProgress = mRangeSeekBar!!.selectedMaxValue + scrollPos
                Log.e(TAG, "-------leftProgress:>>>>>$leftProgress")
                // mMediaPlayer.seekTo((int) leftProgress);
            }
            lastScrollX = scrollX
        }
    }

    /**
     * 水平滑动了多少px
     *
     * @return int px
     */
    private val scrollXDistance: Int
        get() {
            val layoutManager = mRecyclerView!!.layoutManager as LinearLayoutManager?
            val position = layoutManager!!.findFirstVisibleItemPosition()
            val firstVisibleChildView = layoutManager.findViewByPosition(position)
            val itemWidth = firstVisibleChildView!!.width
            return position * itemWidth - firstVisibleChildView.left
        }


    private val mOnRangeSeekBarChangeListener =
        RangeSeekBar.OnRangeSeekBarChangeListener { bar, minValue, maxValue, action, isMin, pressedThumb ->
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    leftProgress = minValue + scrollPos
                    rightProgress = maxValue + scrollPos
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> mStickerView!!.setStickerTime(minValue, maxValue)
                else -> {
                }
            }//                    isSeeking = false;
            //                    videoPause();
            //暂停视频
            //                    isSeeking = true;
            //                    mMediaPlayer.seekTo((int) (pressedThumb == RangeSeekBar.Thumb.MIN ?
            //                            leftProgress : rightProgress));
            //                    isSeeking = false;
            //                    //从minValue开始播
            //                    mMediaPlayer.seekTo((int) leftProgress);
            ////                    videoStart();
            //                    mTvShootTip
            //                            .setText(String.format("裁剪 %d s", (rightProgress - leftProgress) / 1000));
            //设置已选择的时间
            mStickerTimeTv!!.text = String.format(
                resources.getString(R.string.sticker_choose_time),
                BigDecimal(((maxValue - minValue) / 1000.0f).toDouble()).setScale(
                    2,
                    BigDecimal.ROUND_HALF_UP
                ).toDouble().toString()
            )
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReport.initCrashReport(applicationContext, "c5db1d8f24", false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_editext_effect_video)
        mVideoRenderer = VideoRenderer()
        val mVideoEffectType = VideoEffectType()
            .setCurrentEffectType(EFFECT_TYPE_SINGLE)
            .setCurrentRendererType(RENDER_TYPE_AT_TIME)

        mVideoRenderer!!.initRenderer(this.applicationContext, mVideoEffectType)

        initView()
        initData()

        initStickerView()
    }

    /**
     * 初始化贴纸 的view
     */
    private fun initStickerView() {
        mStickerView = findViewById(R.id.stickerView)

        val deleteIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.mipmap.sticker_ic_close_white_18dp
            ),
            BitmapStickerIcon.LEFT_TOP
        )
        deleteIcon.iconEvent = DeleteIconEvent()

        val zoomIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.mipmap.sticker_ic_scale_white_18dp
            ),
            BitmapStickerIcon.RIGHT_BOTOM
        )
        zoomIcon.iconEvent = ZoomIconEvent()

        val flipIcon = BitmapStickerIcon(
            ContextCompat.getDrawable(
                this,
                R.mipmap.sticker_ic_flip_white_18dp
            ),
            BitmapStickerIcon.RIGHT_TOP
        )
        flipIcon.iconEvent = object : StickerIconEvent {
            override fun onActionDown(stickerView: StickerView, event: MotionEvent) {
                mStickerView!!.setCurrentSticker()
            }

            override fun onActionMove(stickerView: StickerView, event: MotionEvent) {

            }

            override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
                if (layoutStickerTool!!.visibility == View.VISIBLE && stickerView.isCurrentSticker) {
                    mStickerView!!.setBorder(false)
                    return
                }
                mStickerTimeTv!!.text = String.format(
                    resources.getString(R.string.sticker_choose_time),
                    BigDecimal(stickerView.rangeTime).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble().toString()
                )

                mainGroup!!.visibility = View.GONE
                effectGroup!!.visibility = View.GONE
                layoutStickerTool!!.visibility = View.VISIBLE
                //关闭贴纸列表
                val transaction = supportFragmentManager.beginTransaction()
                hideFragment(transaction)
                transaction.commit()
                layoutStickerTool!!.post(Runnable {
                    val margin = calculation()
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(mRootView!!)
                    constraintSet.clear(R.id.layout_aspect)
                    constraintSet.constrainWidth(R.id.layout_aspect, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                    constraintSet.constrainHeight(R.id.layout_aspect, ConstraintLayout.LayoutParams.WRAP_CONTENT)

                    constraintSet.connect(R.id.layout_aspect, ConstraintSet.TOP, R.id.btCloseImag, ConstraintSet.BOTTOM)
                    constraintSet.connect(
                        R.id.layout_aspect,
                        ConstraintSet.LEFT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.LEFT
                    )
                    constraintSet.connect(
                        R.id.layout_aspect,
                        ConstraintSet.RIGHT,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.RIGHT
                    )
                    constraintSet.connect(
                        R.id.layout_aspect,
                        ConstraintSet.BOTTOM,
                        R.id.layoutStickerTool,
                        ConstraintSet.TOP
                    )

                    constraintSet.applyTo(mRootView!!)
                    val layoutParams = mAspectLayout!!.layoutParams as ConstraintLayout.LayoutParams

                    layoutParams.width = margin[0]
                    layoutParams.height = margin[1]
                    mAspectLayout!!.layoutParams = layoutParams
                    //改变该触摸的sticker 范围
                    mRangeSeekBar!!.setNormalizedMaxValue(stickerView.currentStickerMaxValue.toDouble())
                    mRangeSeekBar!!.setNormalizedMinValue(stickerView.currentStickerMinValue.toDouble())


                    if (isVideoRange)
                        return@Runnable

                    EXECUTOR.execute {
                        isVideoRange = true
                        //如果存在的 话，那么就不用在截取了
                        val outPutFileDirPath = externalCacheDir!!.absolutePath + "/"
                        val extractW = mMaxWidth / MAX_COUNT_RANGE
                        val extractH = DensityUtils.dp2px(this@EffectVideoActivity, 62f)
                        mExtractFrameWorkThread = ExtractFrameWorkThread(
                            extractW, extractH, mHandler, videoPath,
                            outPutFileDirPath, 0, mSeekBar!!.max.toLong(), MAX_COUNT_RANGE
                        )
                        thumbnailsCount = (mSeekBar!!.max * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE).toInt()
                        rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount
                        averageMsPx = mSeekBar!!.max * 1.0f / rangeWidth * 1.0f
                        mExtractFrameWorkThread!!.start()
                    }
                })


            }
        }

        mStickerView!!.icons = Arrays.asList(deleteIcon, zoomIcon, flipIcon)
        mStickerView!!.setBackgroundColor(Color.TRANSPARENT)
        mStickerView!!.isLocked = false
        mStickerView!!.isConstrained = true


        mStickerView!!.onStickerOperationListener = object : StickerView.OnStickerOperationListener {
            override fun onStickerAdded(sticker: Sticker) {
                Log.e(TAG, "onStickerAdded")
            }

            override fun onStickerClicked(sticker: Sticker) {
                //stickerView.removeAllSticker();

                Log.e(TAG, "onStickerClicked")
            }

            override fun onStickerDeleted(sticker: Sticker) {
                Log.e(TAG, "onStickerDeleted")
            }

            override fun onStickerDragFinished(sticker: Sticker) {
                Log.e(TAG, "onStickerDragFinished")
            }

            override fun onStickerTouchedDown(sticker: Sticker) {
                Log.e(TAG, "onStickerTouchedDown")
            }

            override fun onStickerZoomFinished(sticker: Sticker) {
                Log.e(TAG, "onStickerZoomFinished")
            }

            override fun onStickerFlipped(sticker: Sticker) {
                Log.e(TAG, "onStickerFlipped")
            }

            override fun onStickerDoubleTapped(sticker: Sticker) {
                Log.e(TAG, "onDoubleTapped: double tap will be with two click")
            }

            override fun onStickerTouchedOutSide() {
                val ft = supportFragmentManager.beginTransaction()
                hideFragment(ft)
                ft.commit()
            }
        }


    }

    /**
     * //得计算比例，宽和高都是720*1280 的比例，得注意底部的高度
     *
     * @return
     */
    private fun calculation(): IntArray {


        val border = IntArray(2)

        val exWidth = 720
        val exHeight = 1280
        //左右两边最小的边距
        val minLeft = 40
        val minBottom = 40
        //计算一个我认为最佳的比例
        val width = DensityUtils.getDisplayWidthPixels(this@EffectVideoActivity)
        val height = DensityUtils.getDisplayHeightPixels(this@EffectVideoActivity)
        val position = IntArray(2)
        btCloseImag!!.getLocationOnScreen(position)
        Log.e("Harrison", layoutStickerTool!!.height.toString() + "*" + "height*" + height + "width" + width)
        //开始的比例
        var ratio = 1.2f
        val step = 0.1f
        while (true) {
            if (exWidth * ratio > width - minLeft * 2 || exHeight * ratio > height - layoutStickerTool!!.height - minBottom * 2 - position[1]) {
                ratio -= step
                if (ratio <= 0) {
                    ratio = step
                    break
                }
            } else {
                break
            }
        }
        border[0] = (exWidth * ratio).toInt()
        border[1] = (exHeight * ratio).toInt()
        return border
    }


    private fun initData() {

        val bundle = intent.getBundleExtra(BUNDLE_VIDEO_PATH)
        videoPath = bundle.getString(KEY_VIDEO_PATH)
        //设置播放的视频路径
        mVideoRenderer!!.setVideoPaths(videoPath)
        // 绑定需要渲染的SurfaceView
        mVideoRenderer!!.setSurfaceView(mVideoPreviewView)

        EXECUTOR.execute {
            //获取第一帧图片并模糊
            ResourceHelper.initEffectFilterResource(this@EffectVideoActivity, mResourceData)

            val bitmap = BitmapUtils.createVideoThumbnail(videoPath)
            //                ImageBlur.blurBitmap(bitmap, 10);
            mHandler.post {
                mRootView!!.background = BitmapDrawable(resources, bitmap)
                mPreviewResourceAdapter!!.notifyDataSetChanged()
            }
        }


        //设置videoPlayer 状态监听
        mVideoRenderer!!.setVideoPlayerStatusChangeLisenter(object : VideoRenderThread.VideoPlayerStatusChangeLisenter {
            override fun videoStart(totalTime: Int) {
                mHandler.post {
                    mSeekBar!!.max = totalTime
                    mStickerSeekBar!!.max = totalTime
                    Log.e("Harrison", "totalTime$totalTime")
                    val time = StringUtils.generateTime(totalTime.toLong())
                    tvTotalTime!!.text = if (TextUtils.isEmpty(time)) "00:00" else time
                    mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS)
                }
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_START)

            }

            override fun videoStop() {
                mHandler.removeMessages(MSG_VIDEO_PLAY_PROGRESS)
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_STOP)
            }

            override fun videoRestart() {
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS)
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_START)

            }

            override fun videoCompleted() {
                Log.e("Harrison", "videoCompleted")
                //只有在按下的时候，去改变
                if (isStartClick)
                    isVideoPlayCompleted = true
            }


        })

    }

    //特效，贴纸，合成Mp4
    private fun combineFilterToVideoFile() {
        isCombine = true
        EXECUTOR.execute {
            val outputPath = externalCacheDir!!.absolutePath + File.separator + System.currentTimeMillis() + ".mp4"
            //获取视频文件的宽高
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val videoWidth =
                Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            val videoHeight =
                Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            retriever.release()
            val widthScreen = DensityUtils.getDisplayWidthPixels(this@EffectVideoActivity)
            val heightScreen = DensityUtils.getDisplayHeightPixels(this@EffectVideoActivity)
            val filterGroup = GlFilterGroup()

            //这个是颜色的滤镜
            if (mColorFilterFragment != null) {
                val colorData = mColorFilterFragment!!.currentColorFilter
                if (colorData != null) {
                    val colorFilter = GLColorFilter()
                    colorFilter.setFragmentShaderSource(OpenGLUtils.getShaderFromFile(colorData.fsPath))
                    Log.e("Harrison", OpenGLUtils.getShaderFromFile(colorData.fsPath))
                    filterGroup.addFilterItem(colorFilter)
                }
            }

            //这个是特效的滤镜切换
            mEffectFilter = GLEffectFilter()
            mEffectFilter!!.setFilters(mVideoEffects)

            //计算比例
            val screenScale = Math.min(videoWidth.toFloat() / widthScreen, videoHeight.toFloat() / heightScreen)
            val gifCanvas = Canvas()

            glStickerFilter = object : GLStickerFilter() {
                internal var bitmap: Bitmap? = null
                internal var matrix = Matrix()

                override fun drawCanvas(canvas: Canvas, mPaint: Paint) {
                    val stickers = mStickerView!!.stickers
                    Log.e("Harrison", "********" + mStickerView!!.stickerCount)
                    for (i in 0 until mStickerView!!.stickerCount) {
                        Log.e("Harrison", "********))))))))))))))$i")
                        val sticker = stickers[i]
                        if (sticker.startTime > glStickerFilter!!.currentTime || sticker.endTime < glStickerFilter!!.currentTime) {
                            continue
                        }
                        val stickerScale = sticker.currentScale
                        val drawable = sticker.drawable
                        val drawableWith = drawable.intrinsicWidth
                        val centerX = sticker.mappedCenterPoint.x / widthScreen * canvas.width
                        val centerY = sticker.mappedCenterPoint.y / heightScreen * canvas.height
                        matrix.reset()

                        if (sticker is GifSticker) {
                            //判断之前有没给drawable 设置
                            val gifDrawable = drawable as GifDrawable
                            val currentScale = stickerScale * screenScale
                            bitmap = Bitmap.createBitmap(
                                drawable.getMinimumWidth(),
                                drawable.getMinimumHeight(),
                                Bitmap.Config.ARGB_8888
                            )
                            gifCanvas.setBitmap(bitmap)

                            matrix.postTranslate(
                                centerX - bitmap!!.width * currentScale / 2,
                                centerY - bitmap!!.height * currentScale / 2
                            )
                            matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY)
                            matrix.postScale(currentScale, currentScale)
                            gifDrawable.draw(gifCanvas)
                            canvas.drawBitmap(bitmap!!, matrix, mPaint)
                        } else if (sticker is DrawableSticker) {
                            val bd = drawable as BitmapDrawable
                            bitmap = bd.bitmap
                            //计算比例,屏幕的比例等等
                            val bitmapScale = drawableWith.toFloat() / bitmap!!.width.toFloat()
                            val currentScale = stickerScale * screenScale * bitmapScale
                            //计算按照sticker的中心值，计算百分比宽高
                            matrix.postScale(currentScale, currentScale)
                            matrix.postTranslate(
                                centerX - bitmap!!.width * currentScale / 2,
                                centerY - bitmap!!.height * currentScale / 2
                            )
                            matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY)

                        }
                        if (bitmap != null) {
                            canvas.drawBitmap(bitmap!!, matrix, mPaint)
                        }
                    }
                }


            }
            //滤镜的先后有一定的影响
            //                 filterGroup.addFilterItem(new GlMonochromeFilter());
            filterGroup.addFilterItem(mEffectFilter)
            filterGroup.addFilterItem(glStickerFilter)


            Mp4Composer(videoPath, outputPath)
                .size(videoWidth, videoHeight)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(filterGroup)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double, time: Double) {
                        //time 是纳秒的，需要除以 1000 转化为毫秒 好计算
                        val times = time / 1000
                        glStickerFilter!!.currentTime = times
                        mEffectFilter!!.currentTime = times
                    }

                    override fun onCompleted() {
                        //                                Log.e(TAG, "onCompleted()");
                        isCombine = false
                    }

                    override fun onCanceled() {
                        //                                Log.e(TAG, "onCanceled");
                        isCombine = false
                    }

                    override fun onFailed(exception: Exception) {
                        //                                Log.e(TAG, "onFailed()", exception);
                        isCombine = false
                    }
                })
                .start()
        }
    }

    /**
     * 初始化页面
     *
     * @param
     */
    private fun initView() {
        mAspectLayout = findViewById(R.id.layout_aspect)
        mRecyclerView = findViewById(R.id.recyclerView)
        mThumbRecyclerView = findViewById(R.id.thumbRecyclerView)

        mRootView = findViewById(R.id.rootView)
        mSeekBar = findViewById(R.id.seekBar)
        mStickerSeekBar = findViewById(R.id.mStickerSeekBar)
        tvTotalTime = findViewById(R.id.totalTime)
        tvStartTime = findViewById(R.id.startTime)
        mVideoPlayStatus = findViewById(R.id.imgVideo)
        //        imgVideoSmall = findViewById(R.id.imgVideoSmall);
        //        imgVideoSmall.setOnClickListener(this);

        layoutStickerTool = findViewById(R.id.layoutStickerTool)
        val imgNext = findViewById<ImageView>(R.id.imgNext)
        imgNext.setOnClickListener(this)

        val sticker = findViewById<TextView>(R.id.btSticker)
        sticker.setOnClickListener(this)
        mVideoPlayStatus!!.visibility = View.GONE
        btSave = findViewById(R.id.btSave)
        btCloseImag = findViewById(R.id.btCloseImag)
        btSave!!.setOnClickListener(this)
        btCloseImag!!.setOnClickListener(this)

        mVideoPlayStatus!!.visibility = View.VISIBLE
        val btFilters = findViewById<View>(R.id.btFilters)
        val btEffect = findViewById<View>(R.id.btEffect)
        val btVoiceAdjust = findViewById<View>(R.id.btVoiceAdjust)
        btVoiceAdjust.setOnClickListener(this)
        btEffect.setOnClickListener(this)
        effectGroup = findViewById(R.id.effectGroup)
        mainGroup = findViewById(R.id.mainGroup)
        layoutStickerTool = findViewById(R.id.layoutStickerTool)
        effectGroup!!.visibility = View.GONE
        layoutStickerTool!!.visibility = View.GONE
        mainGroup!!.visibility = View.VISIBLE

        btFilters.setOnClickListener(this)
        mMaxWidth = DensityUtils.getDisplayWidthPixels(this)
        mThumbRecyclerView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mVideoEditAdapter = ThumbVideoAdapter(this, mMaxWidth / 11)
        mThumbRecyclerView!!.adapter = mVideoEditAdapter
        mRecyclerView!!.addOnScrollListener(mOnScrollListener)


        //贴纸得显示时间选择
        mRangeSeekBar = findViewById(R.id.rangeSeekBar)
        mStickerTimeTv = findViewById(R.id.stickerTime)
        mRangeSeekBar!!.selectedMinValue = 0L
        mRangeSeekBar!!.selectedMaxValue = MAX_CUT_DURATION
        mRangeSeekBar!!.setMin_cut_time(MIN_CUT_DURATION)//设置最小裁剪时间
        mRangeSeekBar!!.isNotifyWhileDragging = true
        mRangeSeekBar!!.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)

        mVideoPreviewView = VideoPreviewView(this)
        mAspectLayout!!.addView(mVideoPreviewView, 0)
        mAspectLayout!!.requestLayout()
        mVideoPreviewView!!.setOnClickListener {
            if (mainGroup!!.visibility == View.VISIBLE) {
                hideFilterView()
            } else {
                //假如不是贴图的布局显示，那么就可以点击暂停和播放
                if (layoutStickerTool!!.visibility != View.VISIBLE) {
                    if (mVideoRenderer!!.isVideoPlay) {
                        mVideoRenderer!!.stopPlayVideo()
                    } else {
                        mVideoRenderer!!.startPlayVideo()
                    }
                }

            }
        }
        mStickerSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //改变显示的时间
                mStickerView!!.setShowSticker(progress)
                if (fromUser) {
                    //改变视频的位置
                    mVideoRenderer!!.changeVideoProgress(progress)
                }

            }


            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //触摸Seekbar 停止播放
                mVideoRenderer!!.stopPlayVideo()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mVideoRenderer!!.startPlayVideo()
            }
        })

        mSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    //改变视频的位置
                    mVideoRenderer!!.changeVideoProgress(progress)
                }

                val size = mVideoEffects.size
                if (isStartClick) {
                    //这个条件就作为，再次经过起点
                    if (progress > newVideoEffect!!.getStartTime() && isVideoPlayCompleted) {
                        Log.e("Harrison", "经过了同一点")
                        isVideoPlayCompleted = false
                        newVideoEffect!!.isHasAll = true
                    }
                    newVideoEffect!!.setEndTime(progress)
                    //更新进度的颜色
                    mSeekBar!!.setPathList(mVideoEffects, seekBar.max)
                    return
                }
                if (size <= 0 || isStartClick) return

                for (i in size - 1 downTo 0) {

                    if (mVideoEffects[i].getStartTime() < mVideoEffects[i].getEndTime() &&
                        progress >= mVideoEffects[i].getStartTime() && progress < mVideoEffects[i].getEndTime() || mVideoEffects[i].getStartTime() > mVideoEffects[i].getEndTime() && (progress >= mVideoEffects[i].getStartTime() || progress < mVideoEffects[i].getEndTime())
                    ) {
                        //在添加
                        currentVideoEffectIndex = i
                        val videoEffect = mVideoEffects[i]
                        val indexFilterColor = videoEffect.getDynamicColorId()
                        val color = mDynamicColorFilter.get(indexFilterColor)
                        // Log.e("Harrison", "已改变特效" + currentVideoEffectIndex);
                        mVideoRenderer!!.changeDynamicColorFilter(color)
                        return
                    } else {
                        //判断之前有没使用过特效
                        if (currentVideoEffectIndex != -1) {
                            val videoEffectRemove = mVideoEffects[currentVideoEffectIndex]
                            val color = mDynamicColorFilter.get(videoEffectRemove.getDynamicColorId())
                            mVideoRenderer!!.removeDynamic(color)
                            currentVideoEffectIndex = -1
                            return

                        }
                    }
                }

                //  Log.e("Harrison", "不用修改特效");

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //触摸Seekbar 停止播放
                mVideoRenderer!!.stopPlayVideo()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.HORIZONTAL
        mRecyclerView!!.layoutManager = manager

        mPreviewResourceAdapter = EffectResourceAdapter(this, mResourceData)
        mRecyclerView!!.adapter = mPreviewResourceAdapter
        mRecyclerView!!.addItemDecoration(SpaceItemDecoration(40, 20))
        //长按对每种特效的记录
        mPreviewResourceAdapter!!.setOnLongClickLister(object : EffectResourceAdapter.OnLongClickLister {
            override fun onClickStart(position: Int) {
                //开始计算使用特效
                parseResource(mResourceData[position].type, mResourceData[position].unzipFolder, position)
                isStartClick = true
                val current = mSeekBar!!.progress
                currentEffectKey = current
                //如果有重叠的话
                newVideoEffect =
                    VideoEffect().setStartTime(currentEffectKey).setDynamicColorId(position).setResColorId(position)
                        .setDynamicColor(mDynamicColorFilter.get(position))
                mVideoEffects.add(newVideoEffect!!)

            }

            override fun onClickEnd(position: Int) {
                //结束使用该特效
                val color = mDynamicColorFilter.get(position)
                mVideoRenderer!!.removeDynamic(color)
                if (newVideoEffect!!.isHasAll) {
                    Log.e("Harrison", "设置全部")
                    newVideoEffect!!.setStartTime(0)
                    newVideoEffect!!.setEndTime(mSeekBar!!.max)
                } else {
                    newVideoEffect!!.setEndTime(mSeekBar!!.progress)
                }
                newVideoEffect!!.setEndTime(mSeekBar!!.progress)
                isStartClick = false
            }
        })

    }

    /**
     * 显示贴纸的Framelayout
     */
    private fun showStickerFragment() {

        val ft = supportFragmentManager.beginTransaction()
        hideFragment(ft)
        if (mStickerFragment == null) {
            mStickerFragment = StickersFragment.getInstance()
            ft.add(R.id.fragment_container, mStickerFragment!!)
            mStickerFragment!!.setOnStickerAddListener(object : StickersFragment.OnStickerPanlListener {
                override fun addSticker(item: ItemSticker) {

                    if (!TextUtils.isEmpty(item.path) && File(item.path).exists()) {
                        if (item.type == item.TYPE_GIF) {
                            //android P 以上版本
                            //                                drawable = ImageDecoder.decodeDrawable(
                            //                                        ImageDecoder.createSource(new File(item.getPath())));
                            //                                if (drawable instanceof AnimatedImageDrawable) {
                            //                                    ((AnimatedImageDrawable) drawable).start();
                            //                                }

                            var gifDrawable: GifDrawable? = null
                            try {
                                gifDrawable = GifDrawable(item.path!!)
                                gifDrawable.start()
                                mStickerView!!.addSticker(GifSticker(gifDrawable).setEndTime(mSeekBar!!.max.toFloat()))
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        } else {
                            val bitmap = BitmapFactory.decodeFile(item.path)
                            val drawable = BitmapDrawable(bitmap)
                            mStickerView!!.addSticker(DrawableSticker(drawable).setEndTime(mSeekBar!!.max.toFloat()))
                        }
                    }

                }

                override fun onClosePanl() {
                    val ft = supportFragmentManager.beginTransaction()
                    hideFragment(ft)
                    ft.commit()
                }
            })

        } else {
            ft.show(mStickerFragment!!)
        }
        ft.commit()
    }


    /**
     * 隐藏滤镜页面
     */
    private fun hideFilterView() {
        if (isShowingFilters) {
            isShowingFilters = false
            if (mColorFilterFragment != null) {
                val ft = supportFragmentManager.beginTransaction()
                ft.hide(mColorFilterFragment!!)
                ft.commit()
            }
        }
    }


    /**
     * 解码资源
     *
     * @param type        资源类型
     * @param unzipFolder 资源所在文件夹
     * @param position
     */
    private fun parseResource(type: ResourceType?, unzipFolder: String, position: Int) {
        if (type == null) {
            return
        }
        try {
            when (type) {
                // 单纯的滤镜
                ResourceType.FILTER -> {

                    if (mDynamicColorFilter.get(position) != null) {
                        val color = mDynamicColorFilter.get(position)
                        mVideoRenderer!!.changeDynamicColorFilter(color)
                    } else {
                        val folderPath =
                            ResourceHelper.getResourceDirectory(this@EffectVideoActivity) + File.separator + unzipFolder
                        val color = ResourceJsonCodec.decodeFilterData(folderPath)
                        color.colorType = ResourceType.FILTER.index
                        val colorData = color.filterList[0]
                        colorData.vsPath = folderPath + colorData.vertexShader
                        colorData.fsPath = folderPath + File.separator + colorData.fragmentShader
                        mVideoRenderer!!.changeDynamicColorFilter(color)
                        mDynamicColorFilter.put(position, color)
                    }
                }   // 贴纸
                ResourceType.STICKER -> {
                    val folderPath =
                        ResourceHelper.getResourceDirectory(this@EffectVideoActivity) + File.separator + unzipFolder
                    val sticker = ResourceJsonCodec.decodeStickerData(folderPath)
                    mVideoRenderer!!.changeDynamicResource(sticker)
                }
                // 所有数据均为空
                ResourceType.NONE -> {
                    if (mDynamicColorFilter.get(position) != null) {
                        val color = mDynamicColorFilter.get(position)
                        mVideoRenderer!!.changeDynamicColorFilter(color)
                        //    color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                    } else {
                        val color = DynamicColor().setColorType(ResourceType.FILTER.index)
                        mVideoRenderer!!.changeDynamicColorFilter(color)
                        //  color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                        val colorData = color.filterList[0]
                        colorData.fsPath = ""
                        mDynamicColorFilter.put(position, color)
                    }
                }
                else -> {
                }
            }
        } catch (e: Exception) {

        }

    }


    override fun onStop() {
        if (mVideoRenderer != null) {
            mVideoRenderer!!.stopPlayVideo()
        }
        super.onStop()


    }

    public override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        if (mVideoRenderer != null) {
            mVideoRenderer!!.startPlayVideo()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        if (mVideoRenderer != null) {
            mVideoRenderer!!.destroyRenderer()
        }
    }

    /**
     * 显示滤镜页面
     */
    private fun showFilterView() {
        isShowingFilters = true
        val ft = supportFragmentManager.beginTransaction()
        hideFragment(ft)
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(
                PreviewFiltersFragment.TYPE_COLOR_FILTER,
                PreviewFiltersFragment.TYPE_VIDEO_EIDTEXT
            )
            mColorFilterFragment!!.setVideoRenderer(mVideoRenderer)
            ft.add(R.id.fragment_container, mColorFilterFragment!!)
        } else {
            ft.show(mColorFilterFragment!!)
        }
        //隐藏其他的
        if (mVoiceAdjustFragment != null && mVoiceAdjustFragment!!.isAdded) {
            ft.hide(mVoiceAdjustFragment!!)
        }
        ft.commit()
        //  hideToolsLayout();
    }

    /**
     * 隐藏其他的Fragment
     *
     * @param ft
     */
    private fun hideFragment(ft: FragmentTransaction) {

        if (mColorFilterFragment != null && mColorFilterFragment!!.isAdded) {
            ft.hide(mColorFilterFragment!!)
        }
        if (mVoiceAdjustFragment != null && mVoiceAdjustFragment!!.isAdded) {
            ft.hide(mVoiceAdjustFragment!!)
        }
        if (mStickerFragment != null && mStickerFragment!!.isAdded) {
            ft.hide(mStickerFragment!!)
        }

    }

    //这个是特效的
    private fun setVideoPreviewSize() {


    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.btSave -> {
            }
            //            case R.id.imgVideoSmall:
            //                if (!mVideoRenderer.isVideoPlay()) {
            //                    //如果沒播放的話，好像回到全屏是有问题的
            //                    mVideoRenderer.startPlayVideo();
            //                }else{
            //                    mVideoRenderer.stopPlayVideo();
            //                }
            //                imgVideoSmall.setSelected(mVideoRenderer.isVideoPlay());
            //                break;
            R.id.btFilters -> showFilterView()
            R.id.btEffect -> {
                //                mStickerView.setLocked(true);
                val constraintSet = ConstraintSet()
                constraintSet.clone(mRootView!!)
                constraintSet.clear(R.id.layout_aspect)
                constraintSet.constrainWidth(R.id.layout_aspect, 0)
                constraintSet.constrainHeight(R.id.layout_aspect, 0)
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.TOP, R.id.btCloseImag, ConstraintSet.BOTTOM, 70)
                constraintSet.connect(
                    R.id.layout_aspect,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT,
                    150
                )
                constraintSet.connect(
                    R.id.layout_aspect,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT,
                    150
                )
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, R.id.startTime, ConstraintSet.TOP, 70)
                constraintSet.applyTo(mRootView!!)

                TransitionManager.beginDelayedTransition(mRootView, object : Transition() {
                    override fun captureStartValues(transitionValues: TransitionValues) {
                        mainGroup!!.visibility = View.GONE
                        layoutStickerTool!!.visibility = View.GONE
                        effectGroup!!.visibility = View.VISIBLE
                    }

                    override fun captureEndValues(transitionValues: TransitionValues) {

                    }
                })
            }

            R.id.btCloseImag -> if (mainGroup!!.visibility == View.GONE) {
                if (!mVideoRenderer!!.isVideoPlay) {
                    //如果沒播放的話，好像回到全屏是有问题的
                    mVideoRenderer!!.startPlayVideo()
                }
                if (mVideoPlayStatus!!.visibility == View.VISIBLE) {
                    mVideoPlayStatus!!.visibility = View.GONE
                }
                val set = ConstraintSet()
                set.clone(mRootView!!)
                set.clear(R.id.layout_aspect)
                set.connect(R.id.layout_aspect, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
                set.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
                set.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
                set.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

                set.applyTo(mRootView!!)
                TransitionManager.beginDelayedTransition(mRootView, object : Transition() {
                    override fun captureStartValues(transitionValues: TransitionValues) {
                        effectGroup!!.visibility = View.GONE
                        layoutStickerTool!!.visibility = View.GONE
                    }

                    override fun captureEndValues(transitionValues: TransitionValues) {
                        mainGroup!!.visibility = View.VISIBLE
                    }
                })

            } else {
                finish()
            }
            R.id.btVoiceAdjust -> showVoiceAdjust()
            R.id.imgNext -> {
                //再次渲染特效成mp4
                if (isCombine) return
                combineFilterToVideoFile()
            }
            R.id.btSticker -> showStickerFragment()
        }
    }


    /**
     * 显示声音的调节
     */
    private fun showVoiceAdjust() {
        val ft = supportFragmentManager.beginTransaction()
        if (mVoiceAdjustFragment == null) {
            mVoiceAdjustFragment = VoiceAdjustFragment.getInstance()
            ft.add(R.id.fragment_container, mVoiceAdjustFragment!!)
            mVoiceAdjustFragment!!.setOnVoiceSeekBarChangeListener(object :
                VoiceAdjustFragment.OnVoiceSeekBarChangeListener {

                override fun origiVoiceChange(progress: Float) {
                    Log.e("Harrison", "origiVoiceChange$progress")
                    mVideoRenderer!!.changeVideoVoice(progress)
                }

                override fun bgmVoiceChange(progres: Float) {
                    Log.e("Harrison", "bgmVoiceChange")
                }
            })
        } else {
            ft.show(mVoiceAdjustFragment!!)
        }
        //隐藏其他的
        if (mColorFilterFragment != null && mColorFilterFragment!!.isAdded) {
            ft.hide(mColorFilterFragment!!)
        }
        ft.commit()
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
            val intent = Intent(context, EffectVideoActivity::class.java)
            val bundle = Bundle()
            bundle.putString(KEY_VIDEO_PATH, path)
            intent.putExtra(BUNDLE_VIDEO_PATH, bundle)
            context.startActivity(intent)
        }
    }


}
