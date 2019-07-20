package com.owoh.video.fragment

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.hardware.Camera
import android.media.MediaFormat
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.cgfay.filterlibrary.fragment.PermissionConfirmDialogFragment
import com.cgfay.filterlibrary.fragment.PermissionErrorDialogFragment
import com.cgfay.filterlibrary.multimedia.VideoCombiner
import com.cgfay.filterlibrary.utils.BrightnessUtils
import com.cgfay.filterlibrary.utils.PermissionUtils
import com.owoh.R
import com.owoh.databinding.FragmentCameraPreviewBinding
import com.owoh.video.activity.EffectVideoActivity
import com.owoh.video.engine.GalleryType
import com.owoh.video.engine.OnCameraCallback
import com.owoh.video.engine.OnRecordListener
import com.owoh.video.engine.camera.CameraEngine
import com.owoh.video.engine.camera.CameraParam
import com.owoh.video.engine.recorder.PreviewRecorder
import com.owoh.video.engine.render.PreviewRenderer
import com.owoh.video.media.bgmusic.MusicManager
import com.owoh.video.media.bgmusic.MusicService
import com.owoh.video.media.change.AudioCodec
import com.owoh.video.media.combine.VideoAudioCombine
import com.owoh.video.utils.PathConstraints
import com.owoh.video.widget.CainSurfaceView
import com.owoh.video.widget.MarqueeTextView
import com.owoh.video.widget.ShutterView
import com.owoh.video.widget.recycleview.CenterLayoutManager
import com.owoh.video.widget.recycleview.GalleryItemDecoration
import com.owoh.video.widget.recycleview.RvAdapter
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import kotlinx.android.synthetic.main.view_preview_bottom.*
import kotlinx.android.synthetic.main.view_preview_bottom.view.*
import kotlinx.android.synthetic.main.view_preview_top.view.*
import java.io.File
import java.util.*

/**
 * 相机预览页面
 */
class CameraPreviewFragment : Fragment(), View.OnClickListener {

    // 相机权限使能标志
    private var mCameraEnable = false
    // 存储权限使能标志
    private var mStorageWriteEnable = false


    // 预览参数
    private var mCameraParam: CameraParam = CameraParam.getInstance()


    private var mCameraSurfaceView: CainSurfaceView? = null


    // 相机类型指示文字
    private val mIndicatorText = ArrayList<String>()

    // 主线程Handler
    private var mMainHandler: Handler? = null
    // 持有该Fragment的Activity，onAttach/onDetach中绑定/解绑，主要用于解决getActivity() = null的情况
    private var mActivity: Activity? = null

    // 分镜页面
    private var mCameraFilterFragment: PreviewFiltersFragment? = null


    // 背景音乐页面
    private var musicFragment: MusicFragment? = null
    // 滤镜页面
    private var mColorFilterFragment: PreviewFiltersFragment? = null


    private val mVideoAudioCombineStateListener = object : VideoAudioCombine.VideoAudioCombineStateListener {
        override fun success(combimePath: String) {
            Log.e("Harrison", "combimePath$combimePath")
            gotoEffectVideo(combimePath)
        }

        override fun fail() {
            Log.e("Harrison", "combine avdio onFail**")
        }

        override fun start() {
            Log.e("Harrison", "combine avdio start**")
        }
    }

    /**
     * 背景 音乐的Service 回调
     */
    private val bgMusicMediaPlayerLinstener = object : MusicService.MediaPlayerLinstener {
        override fun onStart(url: String) {
            Log.e("Harrison", "onStart**$url")
        }

        override fun onFail(url: String) {
            Log.e("Harrison", "onFail**$url")
        }

        override fun onPrepareFinish() {
            Log.e("Harrison", "onPrepareFinish**")
        }

        override fun onCompletion() {
            Log.e("Harrison", "onCompletion**")
        }
    }


    /**
     * 单双击回调监听
     */
    private val mMultiClickListener = object : CainSurfaceView.OnMultiClickListener {

        override fun onSurfaceSingleClick(x: Float, y: Float) {
            // 单击隐藏贴纸和滤镜页面
            mMainHandler!!.post {
                val transaction = childFragmentManager.beginTransaction()
                hideFragment(transaction)
                transaction.commit()
                showToolsLayout()
            }

            // 判断是否支持对焦模式
            if (CameraEngine.getInstance().camera != null) {
                val focusModes = CameraEngine.getInstance().camera
                        .parameters.supportedFocusModes
                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    CameraEngine.getInstance().setFocusArea(
                            CameraEngine.getFocusArea(
                                    x.toInt().toFloat(), y.toInt().toFloat(),
                                    mCameraSurfaceView!!.width, mCameraSurfaceView!!.height, FocusSize
                            )
                    )
                    mMainHandler!!.post { mCameraSurfaceView!!.showFocusAnimation() }
                }
            }
        }

        override fun onSurfaceDoubleClick(x: Float, y: Float) {}

    }


    // ------------------------------------ 预览回调 ---------------------------------------------
    private val mCameraCallback = object : OnCameraCallback {

        override fun onCameraOpened() {
            binding.layoutBottom.btShutter.setEnableEncoder(true)
            requestRender()
        }

        override fun onPreviewCallback(data: ByteArray) {
            // 请求刷新
            requestRender()
        }


    }


    // ------------------------------------ 录制回调 -------------------------------------------
    private val mShutterListener = object : ShutterView.OnShutterListener {

        override fun onStartRecord() {
            Log.e("Harrison", "***onStartRecord")
            layoutGroupDeleted!!.visibility = View.GONE
            mRecordProgBar!!.visibility = View.VISIBLE
            showAllToolView(false)
            // 是否允许录制音频
            val enableAudio = (mCameraParam.audioPermitted && mCameraParam.recordAudio
                    && mCameraParam.mGalleryType === GalleryType.VIDEO && !VideoAudioCombine.getInstance().isBgMusicEnable)

            // 计算输入纹理的大小
            var width = mCameraParam.recordWidth
            var height = mCameraParam.recordHeight
            if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
                width = mCameraParam.recordHeight
                height = mCameraParam.recordWidth
            }
            //同时判断是否开启背景音乐
            if (VideoAudioCombine.getInstance().isBgMusicEnable) {
                MusicManager.getInstance().reStart()
            }
            // 开始录制
            PreviewRecorder.getInstance()
                    .setRecordType(PreviewRecorder.RecordType.Video)
                    .setOutputPath(PathConstraints.getVideoCachePath(mActivity))
                    .enableAudio(enableAudio)
                    .setRecordSize(width, height)
                    .setOnRecordListener(mRecordListener)
                    .setMilliSeconds(PreviewRecorder.CountDownType.TenSecond)
                    .startRecord()
            binding.mRecordProgBar.max = PreviewRecorder.getInstance().maxMilliSeconds.toInt()
            binding.layoutBottom.btShutter?.setMax(PreviewRecorder.getInstance().maxMilliSeconds.toInt())
        }

        override fun onStopRecord() {
            Log.e("Harrison", "***onStopRecord")
            showAllToolView(true)
            layoutGroupDeleted!!.visibility = View.VISIBLE

            binding.layoutBottom.btUpload!!.visibility = View.INVISIBLE
            //同时判断是否开启背景音乐
            if (VideoAudioCombine.getInstance().isBgMusicEnable) {
                MusicManager.getInstance().stop()
            }

            PreviewRecorder.getInstance().stopRecord()


        }

        override fun onShortRecord() {
            PreviewRecorder.getInstance().stopRecord()
            Toast.makeText(mActivity, "录制的时间太短了", Toast.LENGTH_SHORT).show()
        }

        override fun onEndRecord() {
            gotoCombinePath()
        }


    }


    /**
     * 录制监听器
     */
    private val mRecordListener = object : OnRecordListener {

        override fun onRecordStarted() {
            // 请求录音权限
            if (!mCameraParam.audioPermitted) {
                requestRecordSoundPermission()
            }
        }

        override fun onRecordProgressChanged(duration: Long) {
            Log.e("Harrison", "*******$duration")
            val msg = mMainHandler!!.obtainMessage()
            msg.what = MSG_SHUTTER_PROGRESS
            msg.obj = duration
            msg.sendToTarget()
        }

        override fun onRecordFinish() {
            Log.e("Harrison", "*****onRecordFinish")
            binding.layoutBottom.btShutter?.setEnableEncoder(true)
            //判断是否最后录制完毕
            binding.layoutBottom.btShutter?.isOnRecordFinish()

        }
    }


    // -------------------------------------- 短视频合成监听器 ---------------------------------
    // 合成输出路径
    private var combinePath: String? = null
    // 合成监听器
    private val mCombineListener = object : VideoCombiner.CombineListener {
        override fun onCombineStart() {
            if (VERBOSE) {
                Log.d(TAG, "开始合并")
            }
            mMainHandler!!.post {
                // showProgressDialog(getString(R.string.combine_video_message), false)
            }
        }

        override fun onCombineProcessing(current: Int, sum: Int) {

        }

        override fun onCombineFinished(success: Boolean, path: String) {
            //视频合并后，根据当前是不是有bgMusic ,如果有那就合并
            if (success) {
                if (VideoAudioCombine.getInstance().isBgMusicEnable) {
                    //去合并背景音乐
                    val avdio = File(path).parentFile.absolutePath + "/Combine.mp4"
                    VideoAudioCombine.getInstance().setVideoPath(path).setCombinePath(avdio).prepare().startCombine()
                } else {
                    gotoEffectVideo(path)
                }
            } else {
                combineAudioVideoFail()
            }
        }
    }

    /**
     * Home按键监听服务
     */
    private val mHomePressReceiver = object : BroadcastReceiver() {
        private val SYSTEM_DIALOG_REASON_KEY = "reason"
        private val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                if (TextUtils.isEmpty(reason)) {
                    return
                }
                // 当点击了home键时需要停止预览，防止后台一直持有相机
                if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                    // 停止录制
                    if (PreviewRecorder.getInstance().isRecording) {
                        // 暂停录制
                        PreviewRecorder.getInstance().stopRecord()
                        // 停止录制
                        binding.layoutBottom.btShutter?.onRecordStop()

                    }
                }
            }
        }
    }

    init {
        mCameraParam = CameraParam.getInstance()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = activity
        val currentMode = BrightnessUtils.getSystemBrightnessMode(mActivity)
        if (currentMode == 1) {
            mCameraParam.brightness = -1
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity)
        }
        mMainHandler = object : Handler(context!!.mainLooper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_SHUTTER_PROGRESS -> {
                        binding.layoutBottom.btShutter?.setProgress((msg.obj as Long).toFloat())
                        binding.mRecordProgBar?.progress = (msg.obj as Long).toInt()
                    }
                }
            }
        }
        mCameraEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA)
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        mCameraParam.audioPermitted = PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化相机渲染引擎
        PreviewRenderer.getInstance()
                .setCameraCallback(mCameraCallback)
                .initRenderer(mActivity)

    }

    private lateinit var binding: FragmentCameraPreviewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera_preview, container, false)
        if (mCameraEnable) {
            initView()
        } else {
            requestCameraPermission()
        }
        return binding?.root
    }


    /**
     * 初始化页面
     *
     */
    private fun initView() {
        binding.apply {
            //   mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
            mCameraSurfaceView = CainSurfaceView(mActivity)
            mCameraSurfaceView!!.addMultiClickListener(mMultiClickListener)
            layoutAspect?.addView(mCameraSurfaceView)
            layoutAspect?.requestLayout()
            // 绑定需要渲染的SurfaceView
            PreviewRenderer.getInstance().setSurfaceView(mCameraSurfaceView)


        }
        binding.layoutTop.apply {
            btCloseImag.setOnClickListener(this@CameraPreviewFragment)
            btSwitchCamera.setOnClickListener(this@CameraPreviewFragment)
            tvSeletedBgm.setOnClickListener(this@CameraPreviewFragment)
            btFlash.setOnClickListener(this@CameraPreviewFragment)
            btFilters.setOnClickListener(this@CameraPreviewFragment)

            tvSeletedBgm.text = "选择音乐";
            // 初始化
            tvSeletedBgm.init()
            // 设置滚动方向
            tvSeletedBgm.setScrollDirection(MarqueeTextView.RIGHT_TO_LEFT);
            // 设置滚动速度
            tvSeletedBgm.setScrollMode(MarqueeTextView.SCROLL_SLOW);

        }

        binding.layoutBottom.apply {
            pointView.visibility = View.VISIBLE
            btnTools.setOnClickListener(this@CameraPreviewFragment)
            btUpload.setOnClickListener(this@CameraPreviewFragment)
            //底部指示器的
            val galleryIndicator = resources.getStringArray(R.array.gallery_indicator)
            mIndicatorText.addAll(Arrays.asList(*galleryIndicator))
            bottom_indicator.addItemDecoration(GalleryItemDecoration())
            val mCenterLayoutManager = CenterLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
            bottom_indicator.layoutManager = mCenterLayoutManager
            val pagerSnapHelper = object : PagerSnapHelper() {
                override fun findTargetSnapPosition(
                        layoutManager: RecyclerView.LayoutManager,
                        velocityX: Int,
                        velocityY: Int
                ): Int {
                    // TODO 找到对应的Index
                    val mFindTargetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
                    setRecordMode(mFindTargetSnapPosition)
                    return mFindTargetSnapPosition
                }
            }

            pagerSnapHelper.attachToRecyclerView(bottom_indicator)
            val adapter = RvAdapter(mActivity, mIndicatorText)
            bottom_indicator.adapter = adapter
            adapter.setOnItemClickLisitenter { v, position ->
                bottom_indicator.smoothScrollToPosition(position)
                setRecordMode(position)
            }


            btShutter.setOnShutterListener(mShutterListener)
            btShutter.setOnClickListener(this@CameraPreviewFragment)
            layoutGroupDeleted.visibility = View.GONE
            btn_record_preview.setOnClickListener(this@CameraPreviewFragment)
            btn_record_delete.setOnClickListener(this@CameraPreviewFragment)
        }
    }

    fun setRecordMode(position: Int) {
        mCameraParam.mGalleryType = GalleryType.VIDEO
        if (position == 0) {
            //长按模式还是点击模式
            // 录制视频状态
            binding.layoutBottom.btShutter.setCurrentMode(ShutterView.MODE_CLICK_SINGLE)
        } else if (position == 1) {
            binding.layoutBottom.btShutter.setCurrentMode(ShutterView.MODE_CLICK_LONG)
        }

    }

    override fun onResume() {
        super.onResume()
        registerHomeReceiver()

    }

    override fun onStop() {
        super.onStop()
        MusicManager.getInstance().stop()
    }

    override fun onPause() {
        super.onPause()
        unRegisterHomeReceiver()
        hideStickerView()
        hideEffectView()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onDestroy() {

        // 关掉渲染引擎
        PreviewRenderer.getInstance().destroyRenderer()
        MusicManager.getInstance().release(mActivity)
        super.onDestroy()
    }

    override fun onDetach() {
        mActivity = null
        super.onDetach()
    }

    /**
     * 处理返回事件
     *
     * @return
     */
    fun onBackPressed(): Boolean {

        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btSwitchCamera -> switchCamera()
            R.id.btCloseImag -> {
                if (PreviewRecorder.getInstance().numberOfSubVideo > 0) {
                    QMUIDialog.MessageDialogBuilder(activity)
                            .setMessage(getString(R.string.dilog_record))
                            .addAction(getString(R.string.dilog_cancel)) { dialog, _ -> dialog.dismiss() }
                            .addAction(0, getString(R.string.dilog_exist)) { dialog, _ ->
                                dialog.dismiss()
                                activity?.finish()
                            }
                            .create(R.style.QMUI_Dialog).show()
                } else {
                    activity?.finish()
                }
            }
            R.id.btnTools ->
                //打开道具
                showCameraStyleTools()
            //测试视频编辑页面
            R.id.btFlash -> {
            }
            R.id.btFilters -> showEffectView()
            R.id.btShutter -> {
            }
            R.id.btn_record_delete -> deleteRecordedVideo()
            R.id.btn_record_preview -> gotoCombinePath()

            R.id.tvSeletedBgm -> showMusicFragment()
            R.id.btUpload -> {
                val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_VIDEO_CODE)
            }
        }
    }

    /**
     * 显示音乐列表
     */
    private fun showMusicFragment() {
        val ft = childFragmentManager.beginTransaction()
        hideFragment(ft)
        if (musicFragment == null) {
            musicFragment = MusicFragment.instance
            MusicManager.getInstance().startService(mActivity, bgMusicMediaPlayerLinstener)
            VideoAudioCombine.getInstance().setVideoAudioCombineStateListener(mVideoAudioCombineStateListener)
            musicFragment!!.setOnMusicChangeListener(object : MusicFragment.OnMusicChangeListener {
                override fun change(url: String?) {
                    Log.e("Harrison", "$url***")
                    ///这里规定，如果url 为null，就启动麦克风，否则就是背景音乐
                    if (MusicManager.getInstance().changeAudioPlay(url)) {
                        VideoAudioCombine.getInstance().setBgMusicEnable(true).audioPath = url
                        PreviewRecorder.getInstance().enableAudio(false)
                    } else {
                        PreviewRecorder.getInstance().enableAudio(true)
                        VideoAudioCombine.getInstance().isBgMusicEnable = false
                    }
                }

            })
            ft.add(R.id.fragment_container, musicFragment!!)
        } else {
            ft.show(musicFragment!!)
        }
        ft.commit()
        hideToolsLayout()
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
        if (mCameraFilterFragment != null && mCameraFilterFragment!!.isAdded) {
            ft.hide(mCameraFilterFragment!!)
        }
        if (musicFragment != null && musicFragment!!.isAdded) {
            ft.hide(musicFragment!!)
            //隐藏它时候需要关闭音乐
            MusicManager.getInstance().stop()
        }

    }


    /**
     * 切换相机
     */
    private fun switchCamera() {
        if (!mCameraEnable) {
            requestCameraPermission()
            return
        }
        PreviewRenderer.getInstance().switchCamera()
    }


    /**
     * 显示分镜工具页面
     */
    private fun showCameraStyleTools() {
        val ft = childFragmentManager.beginTransaction()
        hideFragment(ft)
        if (mCameraFilterFragment == null) {
            mCameraFilterFragment = PreviewFiltersFragment.getInstance(
                    PreviewFiltersFragment.TYPE_CAMERA_FILTER,
                    PreviewFiltersFragment.TYPE_VIDEO_SHOT
            )
            ft.add(R.id.fragment_container, mCameraFilterFragment!!)
        } else {
            ft.show(mCameraFilterFragment!!)
        }
        ft.commit()
        hideToolsLayout()
    }

    /**
     * 显示滤镜页面
     */
    private fun showEffectView() {
        val ft = childFragmentManager.beginTransaction()
        hideFragment(ft)
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(
                    PreviewFiltersFragment.TYPE_COLOR_FILTER,
                    PreviewFiltersFragment.TYPE_VIDEO_SHOT
            )
            ft.add(R.id.fragment_container, mColorFilterFragment!!)
        } else {
            ft.show(mColorFilterFragment!!)
        }
        ft.commit()
        hideToolsLayout()
    }

    /**
     * 隐藏动态贴纸页面
     */
    private fun hideStickerView() {
        if (mCameraFilterFragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.hide(mCameraFilterFragment!!)
            ft.commit()
        }
        showToolsLayout()
    }

    /**
     * 隐藏滤镜页面
     */
    private fun hideEffectView() {
        if (mColorFilterFragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.hide(mColorFilterFragment!!)
            ft.commit()
        }
        showToolsLayout()
    }

    /**
     * 当点击道具或者滤镜时，滤镜和道具，拍摄按钮，上传，选择音乐得隐藏，
     */
    private fun hideToolsLayout() {
        binding.layoutBottom.visibility = View.GONE
        binding.layoutBottom.visibility = View.GONE
        binding.layoutTop.btFilters.visibility = View.INVISIBLE
    }

    /**
     * 恢复显示布局
     */
    private fun showToolsLayout() {
        binding.layoutBottom.visibility = View.VISIBLE
        binding.layoutBottom.visibility = View.VISIBLE
        binding.layoutTop.btFilters.visibility = View.VISIBLE

    }

    /**
     * *****************************回调函数
     */

    private fun gotoEffectVideo(combimePath: String) {
        //视频合并后就开始合并音频
        mMainHandler!!.post {
            // dismissDialog()
            EffectVideoActivity.gotoThis(mActivity!!, combimePath)
        }
    }

    private fun combineAudioVideoFail() {
        //视频合并后就开始合并音频
        mMainHandler!!.post {
            // dismissDialog()
        }
    }

    /**
     * 请求渲染
     */
    private fun requestRender() {
        PreviewRenderer.getInstance().requestRender()
    }

    private fun showAllToolView(isShow: Boolean) {
        binding.layoutBottom.apply {
            if (isShow) {
                binding.layoutTop.visibility = View.VISIBLE
                bottom_indicator.visibility = View.VISIBLE
                binding.layoutBottom.btnTools.visibility = View.VISIBLE
                pointView.visibility = View.VISIBLE
            } else {
                binding.layoutTop!!.visibility = View.INVISIBLE
                bottom_indicator!!.visibility = View.INVISIBLE
                btnTools!!.visibility = View.INVISIBLE
                btUpload!!.visibility = View.INVISIBLE
                pointView!!.visibility = View.GONE
            }
        }


    }

    private fun gotoCombinePath() {
        if (!PreviewRecorder.getInstance().isCanPreview) {
            ToastUtils.showShort(getString(R.string.video_short_tip))
            return
        }
        //首先检查音频是否是AAC,
        if (VideoAudioCombine.getInstance().isBgMusicEnable && !TextUtils.isEmpty(VideoAudioCombine.getInstance().audioPath)) {
            var audioCodec = AudioCodec.newInstance()
            //编码AAC
            var srcFile = File(VideoAudioCombine.getInstance().audioPath)
            var dstPath = srcFile.parentFile.absolutePath + "/aac/"
            audioCodec.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC)
            audioCodec.setIOPath(VideoAudioCombine.getInstance().audioPath, dstPath);
            audioCodec.prepare();
            audioCodec.startAsync();
            audioCodec.setOnCompleteListener {
                audioCodec.release();
                VideoAudioCombine.getInstance().audioPath = it
                combineVideoAudio()

            }
        } else {
            combineVideoAudio()
        }

    }

    private fun combineVideoAudio() {
        combinePath = PathConstraints.getVideoCachePath(mActivity)
        binding.layoutBottom.btShutter.reset()
        PreviewRecorder.getInstance().combineVideo(combinePath, mCombineListener)
    }

    /**
     * 删除已录制的视频
     *
     * @param
     */
    private fun deleteRecordedVideo() {
        PreviewRecorder.getInstance().removeLastSubVideo()
        mRecordProgBar!!.progress = PreviewRecorder.getInstance().visibleDuration.toInt()
        if (PreviewRecorder.getInstance().numberOfSubVideo <= 0) {
            layoutGroupDeleted!!.visibility = View.GONE
            binding.layoutBottom.btUpload.visibility = View.VISIBLE

            binding.layoutBottom.btShutter!!.reset()

        }

    }


    /**
     * 请求相机权限
     */
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(
                    getString(R.string.request_camera_permission),
                    PermissionUtils.REQUEST_CAMERA_PERMISSION,
                    true
            )
                    .show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PermissionUtils.REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CODE) {
            if (resultCode == RESULT_OK) {
                var uri = data?.data;
                var cr = activity?.contentResolver;
                var cursor = cr?.query(uri, null, null, null, null)
                cursor?.moveToFirst().let {
                    var videoPath = cursor?.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    cursor?.close()
                    videoPath?.let {
                        gotoEffectVideo(it)
                    }

                }

            }
        }
    }


    /**
     * 请求存储权限
     */
    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(
                    getString(R.string.request_storage_permission),
                    PermissionUtils.REQUEST_STORAGE_PERMISSION
            )
                    .show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PermissionUtils.REQUEST_STORAGE_PERMISSION
            )
        }
    }

    /**
     * 请求录音权限
     */
    private fun requestRecordSoundPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(
                    getString(R.string.request_sound_permission),
                    PermissionUtils.REQUEST_SOUND_PERMISSION
            )
                    .show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PermissionUtils.REQUEST_SOUND_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(
                        getString(R.string.request_camera_permission),
                        PermissionUtils.REQUEST_CAMERA_PERMISSION,
                        true
                )
                        .show(childFragmentManager, FRAGMENT_DIALOG)
            } else {
                mCameraEnable = true
                initView()
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(
                        getString(R.string.request_storage_permission),
                        PermissionUtils.REQUEST_STORAGE_PERMISSION
                )
                        .show(childFragmentManager, FRAGMENT_DIALOG)
            } else {
                mStorageWriteEnable = true
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(
                        getString(R.string.request_sound_permission),
                        PermissionUtils.REQUEST_SOUND_PERMISSION
                )
                        .show(childFragmentManager, FRAGMENT_DIALOG)
            } else {
                mCameraParam.audioPermitted = true
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * 注册服务
     */
    private fun registerHomeReceiver() {
        if (mActivity != null) {
            val homeFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            mActivity!!.registerReceiver(mHomePressReceiver, homeFilter)
        }
    }

    /**
     * 注销服务
     */
    private fun unRegisterHomeReceiver() {
        if (mActivity != null) {
            mActivity!!.unregisterReceiver(mHomePressReceiver)
        }
    }

    companion object {

        private val TAG = "CameraPreviewFragment1"
        private val VERBOSE = true

        private val FRAGMENT_DIALOG = "dialog"

        // 对焦大小
        private val FocusSize = 80
        private const val MSG_SHUTTER_PROGRESS = 0
        private const val REQUEST_VIDEO_CODE = 0
    }


}
