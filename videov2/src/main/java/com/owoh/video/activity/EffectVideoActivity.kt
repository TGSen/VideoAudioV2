package com.owoh.video.activity


import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
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
import android.widget.SeekBar
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
import com.owoh.video.event.EventTextStickerChange
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
import com.owoh.video.widget.RangeSeekBar
import com.owoh.video.widget.SpaceItemDecoration
import com.owoh.video.widget.VideoPreviewView
import com.owoh.video.widget.sticker.*
import kotlinx.android.synthetic.main.activity_editext_effect_video.*
import kotlinx.android.synthetic.main.layout_editext_video.*
import kotlinx.android.synthetic.main.layout_editext_video.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
    private var mVideoPreviewView: VideoPreviewView? = null

    /**
     * 记录 video 的特效时间
     */
    private var isEditextEffect: Boolean = false
    //这个是临时的，编辑状态下的
    private val mVideoEffects = ArrayList<VideoEffect>()
    //这个是真正运行的
    private val mVideoRuntimeEffects = ArrayList<VideoEffect>()

    private val mHandler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_VIDEO_PLAY_PROGRESS -> try {
                    val progress = mVideoRenderer?.videoProgress
                    val time = StringUtils.generateTime(progress!!.toLong())
                    binding.startTime.text = if (TextUtils.isEmpty(time)) "00:00" else time
                    binding.videoEffectBar?.progress = progress!!
                    this.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS)

                    //改变贴纸的显示时间
                    mStickerSeekBar?.progress = progress
                } catch (e: Exception) {
                }

                MSG_VIDEO_PLAY_STATUS_STOP ->
                    //贴纸的暂停不需要显示
                    if (layoutStickerTool?.visibility == View.GONE)
                        binding.imgVideo.visibility = View.VISIBLE
                MSG_VIDEO_PLAY_STATUS_START -> binding.imgVideo.visibility = View.GONE
                ExtractFrameWorkThread.MSG_SAVE_SUCCESS -> {
                    val info = msg.obj as VideoEditInfo

                    mVideoEditAdapter?.addItemVideoInfo(info)
                    mVideoEditAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private val mResourceData = ArrayList<ResourceData>()
    private var mPreviewResourceAdapter: EffectResourceAdapter? = null
    private val mStartClickTime: Long = 0
    private var currentVideoEffectIndex = -1
    private var newVideoEffect: VideoEffect? = null

    private var isVideoPlayCompleted: Boolean = false

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
            isSeeking = newState != SCROLL_STATE_IDLE
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
                leftProgress = binding.rangeSeekBar.selectedMinValue + scrollPos
                rightProgress = binding.rangeSeekBar.selectedMaxValue + scrollPos
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
            val layoutManager = binding.thumbRecyclerView.layoutManager as LinearLayoutManager?
            val position = layoutManager?.findFirstVisibleItemPosition()
            val firstVisibleChildView = layoutManager?.findViewByPosition(position!!)
            val itemWidth = firstVisibleChildView?.width
            return position!! * itemWidth!! - firstVisibleChildView?.left!!
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
                    MotionEvent.ACTION_UP -> binding.stickerView.setStickerTime(minValue, maxValue)
                    else -> {
                    }
                }
                //设置已选择的时间
                binding.stickerTime?.text = String.format(
                        resources.getString(R.string.sticker_choose_time),
                        BigDecimal(((maxValue - minValue) / 1000.0f).toDouble()).setScale(
                                2,
                                BigDecimal.ROUND_HALF_UP
                        ).toDouble().toString()
                )
            }

    private lateinit var binding: com.owoh.databinding.ActivityEditextEffectVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_editext_effect_video)
        mVideoRenderer = VideoRenderer()
        val mVideoEffectType = VideoEffectType()
                .setCurrentEffectType(EFFECT_TYPE_SINGLE)
                .setCurrentRendererType(RENDER_TYPE_AT_TIME)

        mVideoRenderer?.initRenderer(this.applicationContext, mVideoEffectType)
        EventBus.getDefault().register(this@EffectVideoActivity)
        initView()
        initData()

        initStickerView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTextStickerChange(event: EventTextStickerChange) {

        binding.stickerView.currentSticker?.let {
            if (binding.stickerView.currentSticker is TextSticker) {
                var textSticker = binding.stickerView.currentSticker as TextSticker
                textSticker.setTextColor(Color.parseColor(event.color))
                textSticker.change = event
                textSticker.text = event.text
                textSticker.resizeText()
                binding.stickerView.updateSticker()
            }
        }
    }


    /**
     * 初始化贴纸 的view
     */
    private fun initStickerView() {
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
                binding.stickerView.setCurrentSticker()
            }

            override fun onActionMove(stickerView: StickerView, event: MotionEvent) {

            }

            override fun onActionUp(stickerView: StickerView, event: MotionEvent) {
                if (binding.layoutStickerTool?.visibility == View.VISIBLE && stickerView.isCurrentSticker) {
                    binding.stickerView.setBorder(false)
                    return
                }
                binding.stickerTime?.text = String.format(
                        resources.getString(R.string.sticker_choose_time),
                        BigDecimal(stickerView.rangeTime).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble().toString()
                )

                binding.layoutEdixtVideo.mainGroup.visibility = View.GONE
                binding.effectGroup?.visibility = View.GONE
                binding.layoutStickerTool?.visibility = View.VISIBLE
                //关闭贴纸列表
                val transaction = supportFragmentManager.beginTransaction()
                hideFragment(transaction)
                transaction.commit()
                binding.layoutStickerTool?.post(Runnable {
                    val margin = calculation()
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(binding.rootView)
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

                    constraintSet.applyTo(binding.rootView)
                    val layoutParams = binding.layoutAspect.layoutParams as ConstraintLayout.LayoutParams

                    layoutParams.width = margin[0]
                    layoutParams.height = margin[1]
                    binding.layoutAspect.layoutParams = layoutParams
                    //改变该触摸的sticker 范围
                    binding.rangeSeekBar.setNormalizedMaxValue(stickerView.currentStickerMaxValue.toDouble())
                    binding.rangeSeekBar.setNormalizedMinValue(stickerView.currentStickerMinValue.toDouble())
                    if (isVideoRange)
                        return@Runnable
                    EXECUTOR.execute {
                        isVideoRange = true
                        //如果存在的 话，那么就不用在截取了
                        val outPutFileDirPath = externalCacheDir?.absolutePath + "/"
                        val extractW = mMaxWidth / MAX_COUNT_RANGE
                        val extractH = DensityUtils.dp2px(this@EffectVideoActivity, 62f)
                        mExtractFrameWorkThread = ExtractFrameWorkThread(
                                extractW, extractH, mHandler, videoPath,
                                outPutFileDirPath, 0, binding.videoEffectBar.max.toLong(), MAX_COUNT_RANGE
                        )
                        thumbnailsCount = (binding.videoEffectBar.max * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE).toInt()
                        rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount
                        averageMsPx = binding.videoEffectBar.max * 1.0f / rangeWidth * 1.0f
                        mExtractFrameWorkThread?.start()
                    }
                })


            }
        }
        binding.stickerView.icons = Arrays.asList(deleteIcon, zoomIcon, flipIcon)
        binding.stickerView.setBackgroundColor(Color.TRANSPARENT)
        binding.stickerView.isLocked = false
        binding.stickerView.isConstrained = true
        binding.stickerView.onStickerOperationListener = object : StickerView.OnStickerOperationListener {
            override fun onStickerAdded(sticker: Sticker) {
                Log.e(TAG, "onStickerAdded")
            }

            override fun onStickerClicked(sticker: Sticker) {
                //stickerView.removeAllSticker();

                var textSticker = binding.stickerView.currentSticker

                textSticker?.let {
                    if (textSticker is TextSticker && !TextUtils.isEmpty(textSticker.path)) {
                        AddTextStickerActivity.gotoThis(this@EffectVideoActivity, textSticker.path, textSticker.change)
                        val ft = supportFragmentManager.beginTransaction()
                        hideFragment(ft)
                        ft.commit()
                    }
                }

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
        binding.btCloseImag?.getLocationOnScreen(position)
//        Log.e("Harrison", layoutStickerTool?.height.toString() + "*" + "height*" + height + "width" + width)
        //开始的比例
        var ratio = 1.2f
        val step = 0.1f
        while (true) {
            if (exWidth * ratio > width - minLeft * 2 || exHeight * ratio > height - binding.layoutStickerTool.height - minBottom * 2 - position[1]) {
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
        mVideoRenderer?.setVideoPaths(videoPath)

        // 绑定需要渲染的SurfaceView
        mVideoRenderer?.setSurfaceView(mVideoPreviewView)

        EXECUTOR.execute {
            //获取第一帧图片并模糊
            ResourceHelper.initEffectFilterResource(this@EffectVideoActivity, mResourceData)

            val bitmap = BitmapUtils.createVideoThumbnail(videoPath)
            //                ImageBlur.blurBitmap(bitmap, 10);
            mHandler.post {
                binding.rootView.background = BitmapDrawable(resources, bitmap)
                mPreviewResourceAdapter?.notifyDataSetChanged()
            }
        }


        //设置videoPlayer 状态监听
        mVideoRenderer?.setVideoPlayerStatusChangeLisenter(object : VideoRenderThread.VideoPlayerStatusChangeLisenter {
            override fun videoStart(totalTime: Int) {
                mHandler.post {
                    binding.videoEffectBar.max = totalTime
                    binding.mStickerSeekBar.max = totalTime
                    Log.e("Harrison", "totalTime$totalTime")
                    val time = StringUtils.generateTime(totalTime.toLong())
                    binding.totalTime.text = if (TextUtils.isEmpty(time)) "00:00" else time
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

        // showProgressDialog(getString(R.string.combine_video_message), false)
        isCombine = true
        EXECUTOR.execute {
            val outputPath = externalCacheDir?.absolutePath + File.separator + System.currentTimeMillis() + ".mp4"
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

            mColorFilterFragment?.currentColorFilter?.fsPath?.let {

                val colorFilter = GLColorFilter()
                colorFilter.setFragmentShaderSource(OpenGLUtils.getShaderFromFile(it))
                filterGroup.addFilterItem(colorFilter)

            }

            //计算比例
            val screenScale = Math.min(videoWidth.toFloat() / widthScreen, videoHeight.toFloat() / heightScreen)
            val gifCanvas = Canvas()


            glStickerFilter = object : GLStickerFilter() {
                var bitmap: Bitmap? = null
                var matrix = Matrix()

                override fun drawCanvas(canvas: Canvas, mPaint: Paint) {
                    val stickers = binding.stickerView?.stickers

                    for (i in 0 until binding.stickerView?.stickerCount) {
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
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                            )
                            gifCanvas.setBitmap(bitmap)
                            matrix.postScale(currentScale, currentScale)

                            matrix.postTranslate(
                                    centerX - bitmap!!.width * currentScale / 2,
                                    centerY - bitmap!!.height * currentScale / 2
                            )
                            matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY)

                            gifDrawable.draw(gifCanvas)
                            canvas.drawBitmap(bitmap, matrix, mPaint)
                        } else if (sticker is DrawableSticker) {
                            val bd = drawable as BitmapDrawable
                            bitmap = bd.bitmap
                            bitmap?.let {
                                //计算比例,屏幕的比例等等
                                val bitmapScale = drawableWith.toFloat() / bitmap!!.width.toFloat()
                                val currentScale = stickerScale * screenScale * bitmapScale
                                //计算按照sticker的中心值，计算百分比宽高
                                matrix.postScale(currentScale, currentScale)
                                matrix.postTranslate(
                                        centerX - (bitmap!!.width) * currentScale / 2,
                                        centerY - bitmap!!.height * currentScale / 2
                                )
                                matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY)

                            }

                            canvas.drawBitmap(bitmap, matrix, mPaint)
                        } else if (sticker is TextSticker) {
                            val bd = drawable as BitmapDrawable
                            var btm = bd.bitmap
                            val bitmapScale = drawableWith.toFloat() / btm!!.width.toFloat()
                            val currentScale = stickerScale * screenScale * bitmapScale
                            bitmap = Bitmap.createBitmap(
                                    btm.width,
                                    btm.height,
                                    Bitmap.Config.ARGB_8888
                            )
                            gifCanvas.setBitmap(bitmap)
                            //   matrix.postScale(currentScale, currentScale)
                            matrix.postTranslate(
                                    centerX - btm!!.width * currentScale / 2,
                                    centerY - btm!!.height * currentScale / 2
                            )
                            matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY)
                            sticker.drawCanvas(gifCanvas)
                            canvas.drawBitmap(bitmap, matrix, mPaint)
                        }
                    }
                }
            }

            //滤镜的先后有一定的影响
            //这个是特效的滤镜切换
            if (mVideoRuntimeEffects.size > 0) {
                mEffectFilter = GLEffectFilter()
                mEffectFilter?.setFilters(mVideoRuntimeEffects)
                filterGroup.addFilterItem(mEffectFilter)
            }
            if (binding.stickerView.stickerCount > 0) {
                filterGroup.addFilterItem(glStickerFilter)
            }

            //最后添加个Logo.gif
            var logoGifSticker: GifSticker? = null
            try {
                var gifDrawable = GifDrawable.createFromResource(resources, R.drawable.watermark)
                gifDrawable?.start()
                logoGifSticker = GifSticker(gifDrawable).setEndTime(binding.videoEffectBar.max.toFloat()).setPreview(false) as GifSticker?
                binding.stickerView.addSticker(logoGifSticker!!, Sticker.Position.BOTTOM)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            logoGifSticker?.let {
                var logoStickerFilter = object : GLStickerFilter() {
                    var bitmap: Bitmap? = null
                    var matrix = Matrix()
                    override fun drawCanvas(canvas: Canvas, mPaint: Paint) {

                        val stickerScale = logoGifSticker.currentScale
                        val drawable = logoGifSticker.drawable
                        val drawableWith = drawable.intrinsicWidth
                        val centerX = logoGifSticker.mappedCenterPoint.x / widthScreen * canvas.width
                        val centerY = logoGifSticker.mappedCenterPoint.y / heightScreen * canvas.height
                        matrix.reset()
                        //判断之前有没给drawable 设置
                        val gifDrawable = drawable as GifDrawable
                        val currentScale = stickerScale * screenScale
                        bitmap = Bitmap.createBitmap(
                                drawable.intrinsicWidth,
                                drawable.intrinsicHeight,
                                Bitmap.Config.ARGB_8888
                        )
                        gifCanvas.setBitmap(bitmap)
                        matrix.postScale(currentScale, currentScale)

                        //如果不是显示的话，那就是logo
                        matrix.postTranslate(
                                100f,
                                ((canvas.height - 100).toFloat())
                        )

                        gifDrawable.draw(gifCanvas)
                        canvas.drawBitmap(bitmap, matrix, mPaint)

                    }
                }
//                filterGroup.addFilterItem(logoStickerFilter)

            }


            var composer = Mp4Composer(videoPath, outputPath)
                    .size(videoWidth, videoHeight)
                    .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                    .listener(object : Mp4Composer.Listener {
                        override fun onProgress(progress: Double, time: Double) {
                            //time 是纳秒的，需要除以 1000 转化为毫秒 好计算
                            val times = time / 1000
                            glStickerFilter?.currentTime = times
                            mEffectFilter?.currentTime = times
                        }

                        override fun onCompleted() {
                            //                                Log.e(TAG, "onCompleted()");
//                            dismissDialog()
                            mHandler.post(Runnable {
                                ChooseCoverVideoActivity.gotoThis(this@EffectVideoActivity, outputPath)
                            })
                            isCombine = false
                        }

                        override fun onCanceled() {
                            //                                Log.e(TAG, "onCanceled");
//                            dismissDialog()
                            isCombine = false
                        }

                        override fun onFailed(exception: Exception) {
                            //                                Log.e(TAG, "onFailed()", exception);
//                            dismissDialog()
                            isCombine = false
                        }
                    })
            if (filterGroup.groupSize > 0) {
                composer.filter(filterGroup)
            }
            composer.start()
        }
    }

    /**
     * 初始化页面
     *
     * @param
     */
    private fun initView() {
        binding.layoutEdixtVideo.apply {
            imgNext.setOnClickListener(this@EffectVideoActivity)
            btSticker.setOnClickListener(this@EffectVideoActivity)
            btVoiceAdjust.setOnClickListener(this@EffectVideoActivity)
            btEffect.setOnClickListener(this@EffectVideoActivity)
            btFilters.setOnClickListener(this@EffectVideoActivity)


        }
        binding.apply {
            //        imgVideoSmall = findViewById(R.id.imgVideoSmall);
            //        imgVideoSmall.setOnClickListener(this);

            binding.imgVideo?.visibility = View.GONE

            btSave.setOnClickListener(this@EffectVideoActivity)
            btCloseImag.setOnClickListener(this@EffectVideoActivity)
            btDeleteEffect.setOnClickListener(this@EffectVideoActivity)





            effectGroup.visibility = View.GONE
            layoutStickerTool.visibility = View.GONE
            mainGroup.visibility = View.VISIBLE


            mMaxWidth = DensityUtils.getDisplayWidthPixels(this@EffectVideoActivity)
            thumbRecyclerView?.layoutManager = LinearLayoutManager(this@EffectVideoActivity, LinearLayoutManager.HORIZONTAL, false)
            mVideoEditAdapter = ThumbVideoAdapter(this@EffectVideoActivity, mMaxWidth / 11)
            thumbRecyclerView?.adapter = mVideoEditAdapter
            thumbRecyclerView?.addOnScrollListener(mOnScrollListener)

            //贴纸得显示时间选择
            binding.rangeSeekBar.selectedMinValue = 0L
            binding.rangeSeekBar.selectedMaxValue = MAX_CUT_DURATION
            binding.rangeSeekBar.setMin_cut_time(MIN_CUT_DURATION)//设置最小裁剪时间
            binding.rangeSeekBar.isNotifyWhileDragging = true
            binding.rangeSeekBar.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener)
            mVideoPreviewView = VideoPreviewView(this@EffectVideoActivity)
            layout_aspect.addView(mVideoPreviewView, 0)
            layout_aspect.requestLayout()
            mVideoPreviewView?.setOnClickListener {
                if (mainGroup?.visibility == View.VISIBLE) {
                    hideFilterView()
                } else {
                    //假如不是贴图的布局显示，那么就可以点击暂停和播放
                    if (layoutStickerTool?.visibility != View.VISIBLE) {
                        mVideoRenderer?.isVideoPlay.let {
                            if (it!!) {
                                mVideoRenderer?.stopPlayVideo()
                            } else {
                                mVideoRenderer?.startPlayVideo()
                            }
                        }

                    }

                }
            }
            mStickerSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    //改变显示的时间
                    binding.stickerView.setShowSticker(progress)
                    if (fromUser) {
                        //改变视频的位置
                        mVideoRenderer?.changeVideoProgress(progress)
                    }

                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    //触摸Seekbar 停止播放
                    mVideoRenderer?.stopPlayVideo()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mVideoRenderer?.startPlayVideo()
                }
            })

            videoEffectBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        //改变视频的位置
                        mVideoRenderer?.changeVideoProgress(progress)
                    }

                    val size = mVideoEffects.size
                    if (isStartClick) {
                        //这个条件就作为，再次经过起点
                        if (progress > newVideoEffect!!.getStartTime() && isVideoPlayCompleted) {
                            Log.e("Harrison", "经过了同一点")
                            isVideoPlayCompleted = false
                            newVideoEffect?.isHasAll = true
                        }
                        newVideoEffect?.setEndTime(progress)
                        //更新进度的颜色
                        videoEffectBar.setPathList(mVideoEffects, seekBar.max)
                        return
                    }
                    if (isEditextEffect) {
                        changeEffect(progress, mVideoEffects)
                    } else {
                        changeEffect(progress, mVideoRuntimeEffects)
                    }

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    //触摸Seekbar 停止播放
                    mVideoRenderer?.stopPlayVideo()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })

            val manager = LinearLayoutManager(this@EffectVideoActivity)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            effectRecyclerView.layoutManager = manager

            mPreviewResourceAdapter = EffectResourceAdapter(this@EffectVideoActivity, mResourceData)
            effectRecyclerView.adapter = mPreviewResourceAdapter
            effectRecyclerView.addItemDecoration(SpaceItemDecoration(40, 20))
            //长按对每种特效的记录
            mPreviewResourceAdapter?.setOnLongClickLister(object : EffectResourceAdapter.OnLongClickLister {
                override fun onClickStart(position: Int) {
                    //开始计算使用特效
                    parseResource(mResourceData[position].type, mResourceData[position].unzipFolder, position)
                    isStartClick = true
                    val current = videoEffectBar.progress
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
                    mVideoRenderer?.removeDynamic(color)
                    if (newVideoEffect!!.isHasAll) {
                        Log.e("Harrison", "设置全部")
                        newVideoEffect?.setStartTime(0)
                        newVideoEffect?.setEndTime(videoEffectBar.max)
                    } else {
                        newVideoEffect?.setEndTime(videoEffectBar.progress)
                    }
                    newVideoEffect?.setEndTime(videoEffectBar.progress)
                    isStartClick = false
                }
            })

        }

    }

    private fun changeEffect(progress: Int, list: ArrayList<VideoEffect>) {
        if (list == null || list.size <= 0) {
            //判断之前有没使用过特效
            if (currentVideoEffectIndex != -1) {
//                val videoEffectRemove = list[currentVideoEffectIndex]
//                val color = mDynamicColorFilter.get(videoEffectRemove.getDynamicColorId())
                mVideoRenderer?.changeDynamicColorFilter(null)
                currentVideoEffectIndex = -1
            }
            return
        }
        var size = list.size
        for (i in size - 1 downTo 0) {

            if (list[i].getStartTime() < list[i].getEndTime()
                    && progress >= list[i].getStartTime()
                    && progress < list[i].getEndTime()
                    || (list[i].getStartTime() > list[i].getEndTime()
                            && (progress >= list[i].getStartTime()
                            || progress < list[i].getEndTime()))
            ) {
                //在添加
                currentVideoEffectIndex = i
                val videoEffect = list[i]
                val indexFilterColor = videoEffect.getDynamicColorId()
                val color = mDynamicColorFilter.get(indexFilterColor)
                // Log.e("Harrison", "已改变特效" + currentVideoEffectIndex);
                mVideoRenderer?.changeDynamicColorFilter(color)
                return
            } else {
                //判断之前有没使用过特效
                if (currentVideoEffectIndex != -1) {
                    val videoEffectRemove = list[currentVideoEffectIndex]
                    val color = mDynamicColorFilter.get(videoEffectRemove.getDynamicColorId())
                    mVideoRenderer?.removeDynamic(color)
                    currentVideoEffectIndex = -1
                    return

                }
            }
        }
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
            mStickerFragment?.setOnStickerAddListener(object : StickersFragment.OnStickerPanlListener {
                override fun addSticker(item: ItemSticker) {

                    if (!TextUtils.isEmpty(item.path) && File(item.path).exists()) {
                        when (item.type) {
                            item.TYPE_GIF -> {
                                var gifDrawable: GifDrawable? = null
                                try {
                                    Log.e("Harrison", item.path)
                                    gifDrawable = GifDrawable(item.path!!)
                                    gifDrawable.start()
                                    binding.stickerView.addSticker(GifSticker(gifDrawable).setEndTime(binding.videoEffectBar.max.toFloat()))
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }

                            item.TYPE_PNG -> {
                                val bitmap = BitmapFactory.decodeFile(item.path)
                                val drawable = BitmapDrawable(bitmap)
                                binding.stickerView.addSticker(DrawableSticker(drawable).setEndTime(binding.videoEffectBar.max.toFloat()))
                            }

                            item.TYPE_TEXT -> {
                                val bitmap = BitmapFactory.decodeFile(item.path)
                                val drawable = BitmapDrawable(bitmap)
                                var textSticker = TextSticker(this@EffectVideoActivity)
                                textSticker.path = item.path
                                textSticker.drawable = drawable
                                textSticker.text = " "
                                textSticker.resizeText()
                                textSticker.endTime = binding.videoEffectBar.max.toFloat()
                                binding.stickerView.addSticker(textSticker)
                            }

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

                    if (mDynamicColorFilter.size() > 0 && mDynamicColorFilter.get(position) != null) {
                        val color = mDynamicColorFilter.get(position)
                        mVideoRenderer?.changeDynamicColorFilter(color)
                        Log.e("Harrison", "***position" + position);
                    } else {
                        val folderPath =
                                ResourceHelper.getResourceDirectory(this@EffectVideoActivity) + File.separator + unzipFolder
                        val color = ResourceJsonCodec.decodeFilterData(folderPath)
                        color.colorType = ResourceType.FILTER.index
                        val colorData = color.filterList[0]
                        colorData.vsPath = folderPath + colorData.vertexShader
                        colorData.fsPath = folderPath + File.separator + colorData.fragmentShader
                        mVideoRenderer?.changeDynamicColorFilter(color)
                        Log.e("Harrison", "position" + position);
                        mDynamicColorFilter.put(position, color)
                    }
                }   // 贴纸
                ResourceType.STICKER -> {
                    val folderPath =
                            ResourceHelper.getResourceDirectory(this@EffectVideoActivity) + File.separator + unzipFolder
                    val sticker = ResourceJsonCodec.decodeStickerData(folderPath)
                    mVideoRenderer?.changeDynamicResource(sticker)
                }
                // 所有数据均为空
                ResourceType.NONE -> {
                    if (mDynamicColorFilter.size() > 0 && mDynamicColorFilter.get(position) != null) {
                        val color = mDynamicColorFilter.get(position)
                        mVideoRenderer?.changeDynamicColorFilter(color)
                        //    color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                    } else {
                        val color = DynamicColor().setColorType(ResourceType.FILTER.index)
                        mVideoRenderer?.changeDynamicColorFilter(color)
                        //  color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                        //val colorData = color.filterList[0]
                        // colorData.fsPath = ""
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
            mVideoRenderer?.stopPlayVideo()
        }
        super.onStop()


    }

    public override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
        if (mVideoRenderer != null) {
            mVideoRenderer?.startPlayVideo()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@EffectVideoActivity)
        mHandler.removeCallbacksAndMessages(null)
        if (mVideoRenderer != null) {
            mVideoRenderer?.destroyRenderer()
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
            mColorFilterFragment?.setVideoRenderer(mVideoRenderer)
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
        mColorFilterFragment?.let {
            if (it.isAdded)
                ft.hide(it)
        }

        mVoiceAdjustFragment?.let {
            if (it.isAdded)
                ft.hide(it)
        }
        mStickerFragment?.let {
            if (it.isAdded)
                ft.hide(it)
        }


    }

    //这个是特效的
    private fun setVideoPreviewSize() {


    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.btSave -> {
                isEditextEffect = false
                if (mVideoEffects.size > 0) {
                    Log.e("Harrison", "****mVideoEffects" + mVideoEffects.size + "****mVideoRuntimeEffects" + mVideoRuntimeEffects.size)
                    mVideoRuntimeEffects.addAll(mVideoEffects)
                    mVideoEffects.clear()
                    Log.e("Harrison", "****mVideoEffects" + mVideoEffects.size + "****mVideoRuntimeEffects" + mVideoRuntimeEffects.size)
                }
                resetVideoWindown()

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
                isEditextEffect = true
                if (mVideoRuntimeEffects.size > 0) {
                    Log.e("Harrison", "****mVideoEffects" + mVideoEffects.size + "****mVideoRuntimeEffects" + mVideoRuntimeEffects.size)
                    mVideoEffects.addAll(mVideoRuntimeEffects)
                    Log.e("Harrison", "****mVideoEffects" + mVideoEffects.size + "****mVideoRuntimeEffects" + mVideoRuntimeEffects.size)
                    binding.videoEffectBar.setPathList(mVideoEffects, binding.videoEffectBar.max)
                }
                //                mStickerView.setLocked(true);
                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.rootView)
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
                constraintSet.applyTo(binding.rootView)

                TransitionManager.beginDelayedTransition(binding.rootView, object : Transition() {
                    override fun captureStartValues(transitionValues: TransitionValues) {
                        mainGroup?.visibility = View.GONE
                        layoutStickerTool?.visibility = View.GONE
                        effectGroup?.visibility = View.VISIBLE
                    }

                    override fun captureEndValues(transitionValues: TransitionValues) {

                    }
                })
            }

            R.id.btCloseImag -> if (mainGroup?.visibility == View.GONE) {
                //重置临时特效
                isEditextEffect = false
                mVideoEffects.clear()
                binding.videoEffectBar.reset()

                resetVideoWindown()

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
            R.id.btDeleteEffect -> deletedEffect()
        }
    }

    //重置播放的窗口大小
    private fun resetVideoWindown() {
        if (!(mVideoRenderer?.isVideoPlay)!!) {
            //如果沒播放的話，好像回到全屏是有问题的
            mVideoRenderer?.startPlayVideo()
        }
        if (binding.imgVideo?.visibility == View.VISIBLE) {
            binding.imgVideo?.visibility = View.GONE
        }
        val set = ConstraintSet()
        set.clone(binding.rootView)
        set.clear(R.id.layout_aspect)
        set.connect(R.id.layout_aspect, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        set.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0)
        set.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0)
        set.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)

        set.applyTo(binding.rootView)
        TransitionManager.beginDelayedTransition(binding.rootView, object : Transition() {
            override fun captureStartValues(transitionValues: TransitionValues) {
                effectGroup?.visibility = View.GONE
                layoutStickerTool?.visibility = View.GONE
            }

            override fun captureEndValues(transitionValues: TransitionValues) {
                mainGroup?.visibility = View.VISIBLE
            }
        })
    }

    /**
     * 删除特效
     */
    private fun deletedEffect() {
        mVideoEffects?.let {
            if (mVideoEffects.size > 0) {
                mVideoEffects.removeAt(mVideoEffects.size - 1)
                binding.videoEffectBar.setPathList(mVideoEffects, binding.videoEffectBar.max)
            }
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
            mVoiceAdjustFragment?.setOnVoiceSeekBarChangeListener(object :
                    VoiceAdjustFragment.OnVoiceSeekBarChangeListener {

                override fun origiVoiceChange(progress: Float) {
                    Log.e("Harrison", "origiVoiceChange$progress")
                    mVideoRenderer?.changeVideoVoice(progress)
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
