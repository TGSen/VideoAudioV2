package com.owoh.video.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cgfay.filterlibrary.fragment.PermissionConfirmDialogFragment;
import com.cgfay.filterlibrary.fragment.PermissionErrorDialogFragment;
import com.cgfay.filterlibrary.multimedia.VideoCombiner;
import com.cgfay.filterlibrary.utils.BrightnessUtils;
import com.cgfay.filterlibrary.utils.PermissionUtils;
import com.owoh.R;
import com.owoh.video.activity.EffectVideoActivity;
import com.owoh.video.engine.GalleryType;
import com.owoh.video.engine.OnCameraCallback;
import com.owoh.video.engine.OnRecordListener;
import com.owoh.video.engine.camera.CameraEngine;
import com.owoh.video.engine.camera.CameraParam;
import com.owoh.video.engine.camera.SensorControler;
import com.owoh.video.engine.recorder.PreviewRecorder;
import com.owoh.video.engine.render.PreviewRenderer;
import com.owoh.video.media.bgmusic.MusicManager;
import com.owoh.video.media.bgmusic.MusicService;
import com.owoh.video.media.combine.VideoAudioCombine;
import com.owoh.video.utils.PathConstraints;
import com.owoh.video.widget.AspectFrameLayout;
import com.owoh.video.widget.CainSurfaceView;
import com.owoh.video.widget.ShutterView;
import com.owoh.video.widget.recycleview.CenterLayoutManager;
import com.owoh.video.widget.recycleview.GalleryItemDecoration;
import com.owoh.video.widget.recycleview.RvAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CameraPreviewFragment1";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    // 对焦大小
    private static final int FocusSize = 80;
    private static final int MSG_SHUTTER_PROGRESS = 0;

    // 相机权限使能标志
    private boolean mCameraEnable = false;
    // 存储权限使能标志
    private boolean mStorageWriteEnable = false;
    // 是否需要等待录制完成再跳转
    private boolean mNeedToWaitStop = false;


    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private AspectFrameLayout mAspectLayout;
    private CainSurfaceView mCameraSurfaceView;


    // 顶部Button
    private ImageView mBtClose;
    private TextView tvSeletedBgm;
    private TextView mBtnSwitch;


    // 道具按钮
    private TextView mBtnTools;
    private TextView mBtUpload;
    private TextView btFlash;
    // 快门按钮
    private ShutterView mBtnShutter;
    // 滤镜按钮
    private TextView mBtnEffect;
    // 视频删除按钮
    private Button mBtnRecordDelete;
    // 视频预览按钮
    private Button mBtnRecordPreview;
    // 相机类型指示器
    private RecyclerView mBottomIndicator;
    // 相机类型指示文字
    private List<String> mIndicatorText = new ArrayList<String>();
    // 合并对话框
    private CombineVideoDialogFragment mCombineDialog;
    // 主线程Handler
    private Handler mMainHandler;
    // 持有该Fragment的Activity，onAttach/onDetach中绑定/解绑，主要用于解决getActivity() = null的情况
    private Activity mActivity;

    // 分镜页面
    private PreviewFiltersFragment mCameraFilterFragment;


    // 背景音乐页面
    private MusicFragment musicFragment;
    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    private Group mGroupViewTop;
    private Group mGroupViewBottom;
    private SensorControler mSensorControler;
    private View layoutPreViewTop;


    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        int currentMode = BrightnessUtils.getSystemBrightnessMode(mActivity);
        if (currentMode == 1) {
            mCameraParam.brightness = -1;
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
        }
        mMainHandler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SHUTTER_PROGRESS:
                        mBtnShutter.setProgress((long) msg.obj);

                        break;
                }
            }
        };
        mCameraEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mCameraParam.audioPermitted = PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化相机渲染引擎
        PreviewRenderer.getInstance()
                .setCameraCallback(mCameraCallback)
                .initRenderer(mActivity);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCameraEnable) {
            initView(mContentView);
        } else {
            requestCameraPermission();
        }

    }

    /**
     * 初始化页面
     *
     * @param view
     */
    private void initView(View view) {
        mAspectLayout = view.findViewById(R.id.layout_aspect);
        layoutPreViewTop = view.findViewById(R.id.layoutPreViewTop);
        //   mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mCameraSurfaceView = new CainSurfaceView(mActivity);
        mCameraSurfaceView.addOnTouchScroller(mTouchScroller);
        mCameraSurfaceView.addMultiClickListener(mMultiClickListener);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        PreviewRenderer.getInstance().setSurfaceView(mCameraSurfaceView);
        mBtClose = view.findViewById(R.id.btCloseImag);
        tvSeletedBgm = view.findViewById(R.id.tvSeletedBgm);
        tvSeletedBgm.setOnClickListener(this);
        mBtClose.setOnClickListener(this);

        mBtnSwitch = view.findViewById(R.id.btSwitchCamera);
        mBtnSwitch.setOnClickListener(this);


        mBtnTools = view.findViewById(R.id.btnTools);
        mBtUpload = view.findViewById(R.id.btUpload);
        btFlash = view.findViewById(R.id.btFlash);
        btFlash.setOnClickListener(this);
        mBtnTools.setOnClickListener(this);
        mBtnEffect = view.findViewById(R.id.btFilters);
        mBtnEffect.setOnClickListener(this);
        mBottomIndicator = view.findViewById(R.id.bottom_indicator);
        //底部指示器的
        String[] galleryIndicator = getResources().getStringArray(R.array.gallery_indicator);
        mIndicatorText.addAll(Arrays.asList(galleryIndicator));
        mBottomIndicator.addItemDecoration(new GalleryItemDecoration());
        CenterLayoutManager mCenterLayoutManager = new CenterLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        mBottomIndicator.setLayoutManager(mCenterLayoutManager);
        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                // TODO 找到对应的Index
                int mFindTargetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                setRecordMode(mFindTargetSnapPosition);
                return mFindTargetSnapPosition;
            }
        };

        pagerSnapHelper.attachToRecyclerView(mBottomIndicator);
        RvAdapter adapter = new RvAdapter(mActivity, mIndicatorText);
        mBottomIndicator.setAdapter(adapter);
        adapter.setOnItemClickLisitenter(new RvAdapter.onItemClickLisitenter() {
            @Override
            public void onItemClick(View v, int position) {


                mBottomIndicator.smoothScrollToPosition(position);
                setRecordMode(position);
            }
        });


        mBtnShutter = view.findViewById(R.id.btShutter);
        mBtnShutter.setOnShutterListener(mShutterListener);
        mBtnShutter.setOnClickListener(this);

        mBtnRecordDelete = (Button) view.findViewById(R.id.btn_record_delete);
        mBtnRecordDelete.setOnClickListener(this);
        mBtnRecordPreview = (Button) view.findViewById(R.id.btn_record_preview);
        mBtnRecordPreview.setOnClickListener(this);

        mGroupViewTop = view.findViewById(R.id.mGroupViewTop);
        mGroupViewBottom = view.findViewById(R.id.mGroupViewBottom);

    }

    public void setRecordMode(int position) {
        mCameraParam.mGalleryType = GalleryType.VIDEO;
        if (position == 0) {
            //长按模式还是点击模式
            // 录制视频状态
            mBtnShutter.setCurrentMode(ShutterView.MODE_CLICK_SINGLE);
        } else if (position == 1) {
            mBtnShutter.setCurrentMode(ShutterView.MODE_CLICK_LONG);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        registerHomeReceiver();
        enhancementBrightness();
//        mBtnShutter.setEnableOpened(false);
    }

    /**
     * 增强光照
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterHomeReceiver();
        hideStickerView();
        hideEffectView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }

    @Override
    public void onDestroy() {

        // 关掉渲染引擎
        PreviewRenderer.getInstance().destroyRenderer();
        MusicManager.getInstance().release(mActivity);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    /**
     * 处理返回事件
     *
     * @return
     */
    public boolean onBackPressed() {

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btSwitchCamera:
                switchCamera();
                break;
            case R.id.btCloseImag:

                break;
            case R.id.btnTools:
                //打开道具
                showCameraStyleTools();
                break;
            //测试视频编辑页面
            case R.id.btFlash:

                break;
            case R.id.btFilters:
                showEffectView();
                break;
            case R.id.btShutter:

                break;
            case R.id.btn_record_delete:
                deleteRecordedVideo(false);
                break;
            case R.id.btn_record_preview:
                stopRecordOrPreviewVideo();
                break;

            case R.id.tvSeletedBgm:
                showMusicFragment();
                break;
        }
    }

    /**
     * 显示音乐列表
     */
    private void showMusicFragment() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        hideFragment(ft);
        if (musicFragment == null) {
            musicFragment = MusicFragment.getInstance();
            MusicManager.getInstance().startService(mActivity, bgMusicMediaPlayerLinstener);
            VideoAudioCombine.getInstance().setVideoAudioCombineStateListener(mVideoAudioCombineStateListener);
            musicFragment.setOnMusicChangeListener(new MusicFragment.OnMusicChangeListener() {
                @Override
                public void change(String url) {
                    Log.e("Harrison", url + "***");
                    ///这里规定，如果url 为null，就启动麦克风，否则就是背景音乐
                    if (MusicManager.getInstance().changeAudioPlay(url)) {
                        VideoAudioCombine.getInstance().setBgMusicEnable(true).setAudioPath(url);
                        PreviewRecorder.getInstance().enableAudio(false);
                    } else {
                        PreviewRecorder.getInstance().enableAudio(true);
                        VideoAudioCombine.getInstance().setBgMusicEnable(false);
                    }
                }
            });
            ft.add(R.id.fragment_container, musicFragment);
        } else {
            ft.show(musicFragment);
        }
        ft.commit();
        hideToolsLayout();
    }

    /**
     * 隐藏其他的Fragment
     *
     * @param ft
     */
    private void hideFragment(FragmentTransaction ft) {

        if (mColorFilterFragment != null && mColorFilterFragment.isAdded()) {
            ft.hide(mColorFilterFragment);
        }
        if (mCameraFilterFragment != null && mCameraFilterFragment.isAdded()) {
            ft.hide(mCameraFilterFragment);
        }
        if (musicFragment != null && musicFragment.isAdded()) {
            ft.hide(musicFragment);
            //隐藏它时候需要关闭音乐
            MusicManager.getInstance().stop();
        }

    }


    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!mCameraEnable) {
            requestCameraPermission();
            return;
        }
        PreviewRenderer.getInstance().switchCamera();
    }


    /**
     * 显示分镜工具页面
     */
    private void showCameraStyleTools() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        hideFragment(ft);
        if (mCameraFilterFragment == null) {
            mCameraFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_CAMERA_FILTER, PreviewFiltersFragment.TYPE_VIDEO_SHOT);
            ft.add(R.id.fragment_container, mCameraFilterFragment);
        } else {
            ft.show(mCameraFilterFragment);
        }
        ft.commit();
        hideToolsLayout();
    }

    /**
     * 显示滤镜页面
     */
    private void showEffectView() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        hideFragment(ft);
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_COLOR_FILTER, PreviewFiltersFragment.TYPE_VIDEO_SHOT);
            ft.add(R.id.fragment_container, mColorFilterFragment);
        } else {
            ft.show(mColorFilterFragment);
        }
        ft.commit();
        hideToolsLayout();
    }

    /**
     * 隐藏动态贴纸页面
     */
    private void hideStickerView() {
        if (mCameraFilterFragment != null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.hide(mCameraFilterFragment);
            ft.commit();
        }
        showToolsLayout();
    }

    /**
     * 隐藏滤镜页面
     */
    private void hideEffectView() {
        if (mColorFilterFragment != null) {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.hide(mColorFilterFragment);
            ft.commit();
        }
        showToolsLayout();
    }

    /**
     * 当点击道具或者滤镜时，滤镜和道具，拍摄按钮，上传，选择音乐得隐藏，
     */
    private void hideToolsLayout() {
        mGroupViewTop.setVisibility(View.GONE);
        mGroupViewBottom.setVisibility(View.GONE);
        mBtnEffect.setVisibility(View.INVISIBLE);
    }

    /**
     * 恢复显示布局
     */
    private void showToolsLayout() {
        mGroupViewTop.setVisibility(View.VISIBLE);
        mGroupViewBottom.setVisibility(View.VISIBLE);
        mBtnEffect.setVisibility(View.VISIBLE);

    }

    /**
     * *****************************回调函数
     */

    private void gotoEffectVideo(final String combimePath) {
        //视频合并后就开始合并音频
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCombineDialog != null) {
                    mCombineDialog.dismiss();
                    mCombineDialog = null;

                    EffectVideoActivity.gotoThis(mActivity, combimePath);

                }
            }
        });
    }

    private void combineAudioVideoFail() {
        //视频合并后就开始合并音频
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCombineDialog != null) {
                    mCombineDialog.dismiss();
                    mCombineDialog = null;
                }
            }
        });
    }

    private VideoAudioCombine.VideoAudioCombineStateListener mVideoAudioCombineStateListener = new VideoAudioCombine.VideoAudioCombineStateListener() {
        @Override
        public void success(final String combimePath) {
            Log.e("Harrison", "combimePath" + combimePath);
            gotoEffectVideo(combimePath);
        }

        @Override
        public void fail() {
            Log.e("Harrison", "combine avdio onFail**");
        }

        @Override
        public void start() {
            Log.e("Harrison", "combine avdio start**");
        }
    };

    /**
     * 背景 音乐的Service 回调
     */
    private MusicService.MediaPlayerLinstener bgMusicMediaPlayerLinstener = new MusicService.MediaPlayerLinstener() {
        @Override
        public void onStart(String url) {
            Log.e("Harrison", "onStart**" + url);
        }

        @Override
        public void onFail(String url) {
            Log.e("Harrison", "onFail**" + url);
        }

        @Override
        public void onPrepareFinish() {
            Log.e("Harrison", "onPrepareFinish**");
        }

        @Override
        public void onCompletion() {
            Log.e("Harrison", "onCompletion**");
        }
    };


    /**
     * SurfaceView 滑动、点击回调
     */

    private CainSurfaceView.OnTouchScroller mTouchScroller = new CainSurfaceView.OnTouchScroller() {

        @Override
        public void swipeBack() {

        }

        @Override
        public void swipeFrontal() {

        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

    };


    /**
     * 单双击回调监听
     */
    private CainSurfaceView.OnMultiClickListener mMultiClickListener = new CainSurfaceView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 单击隐藏贴纸和滤镜页面
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    hideFragment(transaction);
                    transaction.commit();
                    showToolsLayout();
                }
            });

            // 判断是否支持对焦模式
            if (CameraEngine.getInstance().getCamera() != null) {
                List<String> focusModes = CameraEngine.getInstance().getCamera()
                        .getParameters().getSupportedFocusModes();
                if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    CameraEngine.getInstance().setFocusArea(CameraEngine.getFocusArea((int) x, (int) y,
                            mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), FocusSize));
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCameraSurfaceView.showFocusAnimation();
                        }
                    });
                }
            }
        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
        }

    };


    // ------------------------------------ 预览回调 ---------------------------------------------
    private OnCameraCallback mCameraCallback = new OnCameraCallback() {

        @Override
        public void onCameraOpened() {
            mBtnShutter.setEnableEncoder(true);
            requestRender();
        }

        @Override
        public void onPreviewCallback(byte[] data) {
            // 请求刷新
            requestRender();
        }


    };

    /**
     * 请求渲染
     */
    private void requestRender() {
        PreviewRenderer.getInstance().requestRender();
    }


    // ------------------------------------ 录制回调 -------------------------------------------
    private ShutterView.OnShutterListener mShutterListener = new ShutterView.OnShutterListener() {

        @Override
        public void onStartRecord() {
            Log.e("Harrison", "***onStartRecord");
            showAllToolView(false);
            // 隐藏删除按钮
            if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                mBtnRecordPreview.setVisibility(View.GONE);
                mBtnRecordDelete.setVisibility(View.GONE);
            }

            // 是否允许录制音频
            boolean enableAudio = mCameraParam.audioPermitted && mCameraParam.recordAudio
                    && mCameraParam.mGalleryType == GalleryType.VIDEO && !VideoAudioCombine.getInstance().isBgMusicEnable();

            // 计算输入纹理的大小
            int width = mCameraParam.recordWidth;
            int height = mCameraParam.recordHeight;
            if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
                width = mCameraParam.recordHeight;
                height = mCameraParam.recordWidth;
            }
            //同时判断是否开启背景音乐
            if (VideoAudioCombine.getInstance().isBgMusicEnable()) {
                MusicManager.getInstance().reStart();
            }
            // 开始录制
            PreviewRecorder.getInstance()
                    .setRecordType(PreviewRecorder.RecordType.Video)
                    .setOutputPath(PathConstraints.getVideoCachePath(mActivity))
                    .enableAudio(enableAudio)
                    .setRecordSize(width, height)
                    .setOnRecordListener(mRecordListener)
                    .setMilliSeconds(PreviewRecorder.CountDownType.TenSecond)
                    .startRecord();
        }

        @Override
        public void onStopRecord() {
            Log.e("Harrison", "***onStopRecord");
            //同时判断是否开启背景音乐
            if (VideoAudioCombine.getInstance().isBgMusicEnable()) {
                MusicManager.getInstance().stop();
            }
            PreviewRecorder.getInstance().stopRecord();
        }

        @Override
        public void onShortRecord() {
            PreviewRecorder.getInstance().stopRecord();
            Toast.makeText(mActivity, "录制的时间太短了", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndRecord() {
            Log.e("Harrison", "***onEndRecord" + PreviewRecorder.getInstance().getNumberOfSubVideo());
            combinePath = PathConstraints.getVideoCachePath(mActivity);
            PreviewRecorder.getInstance().combineVideo(combinePath, mCombineListener);
        }


    };

    private void showAllToolView(boolean isShow) {
        if (isShow) {
            layoutPreViewTop.setVisibility(View.VISIBLE);
            mBottomIndicator.setVisibility(View.VISIBLE);
            mBtnTools.setVisibility(View.VISIBLE);
            mBtUpload.setVisibility(View.VISIBLE);
        } else {
            layoutPreViewTop.setVisibility(View.INVISIBLE);
            mBottomIndicator.setVisibility(View.INVISIBLE);
            mBtnTools.setVisibility(View.INVISIBLE);
            mBtUpload.setVisibility(View.INVISIBLE);
        }

    }


    /**
     * 录制监听器
     */
    private OnRecordListener mRecordListener = new OnRecordListener() {

        @Override
        public void onRecordStarted() {
            // 请求录音权限
            if (!mCameraParam.audioPermitted) {
                requestRecordSoundPermission();
            }
        }

        @Override
        public void onRecordProgressChanged(final long duration) {
            Log.e("Harrison", "*******" + duration);
            Message msg = mMainHandler.obtainMessage();
            msg.what = MSG_SHUTTER_PROGRESS;
            msg.obj = duration;
            msg.sendToTarget();
        }

        @Override
        public void onRecordFinish() {
            Log.e("Harrison", "*****onRecordFinish");
            mBtnShutter.setEnableEncoder(true);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 编码器已经完全释放，则快门按钮可用
                    showAllToolView(true);
                    //判断是否最后录制完毕
                    mBtnShutter.isOnRecordFinish();
                    // 显示删除按钮
//                    if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
//                        mBtnRecordPreview.setVisibility(View.VISIBLE);
//                        mBtnRecordDelete.setVisibility(View.VISIBLE);
//                    }
                }
            });
        }
    };

    /**
     * 删除已录制的视频
     *
     * @param clearAll
     */
    private void deleteRecordedVideo(boolean clearAll) {
//        // 处于删除模式，则删除文件
//        if (mBtnShutter.isDeleteMode()) {
//            // 删除视频，判断是否清除所有
//            if (clearAll) {
//                // 清除所有分割线
//                mBtnShutter.cleanSplitView();
//                PreviewRecorder.getInstance().removeAllSubVideo();
//            } else {
//                // 删除分割线
//                mBtnShutter.deleteSplitView();
//                PreviewRecorder.getInstance().removeLastSubVideo();
//            }
//
//            // 删除一段已记录的时间
//            PreviewRecorder.getInstance().deleteRecordDuration();
//
//            // 更新进度
//            mBtnShutter.setProgress(PreviewRecorder.getInstance().getVisibleDuration());
//            // 更新时间
//            // 如果此时没有了视频，则恢复初始状态
//            if (PreviewRecorder.getInstance().getNumberOfSubVideo() <= 0) {
//                mBtnRecordDelete.setVisibility(View.GONE);
//                mBtnRecordPreview.setVisibility(View.GONE);
//                mNeedToWaitStop = false;
//            }
//        } else { // 没有进入删除模式则进入删除模式
//            mBtnShutter.setDeleteMode(true);
//        }
    }

    /**
     * 停止录制或者预览视频
     */
    private void stopRecordOrPreviewVideo() {
        if (PreviewRecorder.getInstance().isRecording()) {
            Log.e("Harrison", "stopRecordOrPreviewVideo");
            mNeedToWaitStop = true;
            PreviewRecorder.getInstance().stopRecord(false);
        } else {
            Log.e("Harrison", "stopRecordOrPreviewVideo0");
            mNeedToWaitStop = false;
            // 销毁录制线程
            PreviewRecorder.getInstance().stopRecord(false);
            //  PreviewRecorder.getInstance().destroyRecorder();
            combinePath = PathConstraints.getVideoCachePath(mActivity);
            PreviewRecorder.getInstance().combineVideo(combinePath, mCombineListener);
        }
    }

    // -------------------------------------- 短视频合成监听器 ---------------------------------
// 合成输出路径
    private String combinePath;
    // 合成监听器
    private VideoCombiner.CombineListener mCombineListener = new VideoCombiner.CombineListener() {
        @Override
        public void onCombineStart() {
            if (VERBOSE) {
                Log.d(TAG, "开始合并");
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null) {
                        mCombineDialog.dismiss();
                        mCombineDialog = null;
                    }
                    mCombineDialog = CombineVideoDialogFragment.newInstance(mActivity.getString(R.string.combine_video_message));
                    mCombineDialog.show(getChildFragmentManager(), FRAGMENT_DIALOG);
                }
            });
        }

        @Override
        public void onCombineProcessing(final int current, final int sum) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null && mCombineDialog.getShowsDialog()) {
                        mCombineDialog.setProgressMessage(mActivity.getString(R.string.combine_video_message));
                    }
                }
            });
        }

        @Override
        public void onCombineFinished(final boolean success, final String path) {
            //视频合并后，根据当前是不是有bgMusic ,如果有那就合并
            if (success) {
                if (VideoAudioCombine.getInstance().isBgMusicEnable()) {
                    //去合并背景音乐
                    String avdio = new File(path).getParentFile().getAbsolutePath() + "/Combine.mp4";
                    VideoAudioCombine.getInstance().setVideoPath(path).setCombinePath(avdio).prepare().startCombine();
                } else {
                    gotoEffectVideo(path);
                }
            } else {
                combineAudioVideoFail();
            }
        }
    };


    /**
     * 请求相机权限
     */
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    PermissionUtils.REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * 请求录音权限
     */
    private void requestRecordSoundPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                    PermissionUtils.REQUEST_SOUND_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraEnable = true;
                initView(mContentView);
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mStorageWriteEnable = true;
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCameraParam.audioPermitted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 注册服务
     */
    private void registerHomeReceiver() {
        if (mActivity != null) {
            IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            mActivity.registerReceiver(mHomePressReceiver, homeFilter);
        }
    }

    /**
     * 注销服务
     */
    private void unRegisterHomeReceiver() {
        if (mActivity != null) {
            mActivity.unregisterReceiver(mHomePressReceiver);
        }
    }

    /**
     * Home按键监听服务
     */
    private BroadcastReceiver mHomePressReceiver = new BroadcastReceiver() {
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.isEmpty(reason)) {
                    return;
                }
                // 当点击了home键时需要停止预览，防止后台一直持有相机
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    // 停止录制
                    if (PreviewRecorder.getInstance().isRecording()) {
                        // 暂停录制
                        PreviewRecorder.getInstance().stopRecord();
                        // 停止录制
                        mBtnShutter.onRecordStop();

                    }
                }
            }
        }
    };


}
