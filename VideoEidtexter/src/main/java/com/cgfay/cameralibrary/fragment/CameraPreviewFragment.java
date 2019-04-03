package com.cgfay.cameralibrary.fragment;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.activity.EditextVideoActivity;
import com.cgfay.cameralibrary.engine.camera.CameraEngine;
import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.listener.OnCameraCallback;
import com.cgfay.cameralibrary.engine.listener.OnCaptureListener;
import com.cgfay.cameralibrary.engine.listener.OnRecordListener;
import com.cgfay.cameralibrary.engine.model.AspectRatio;
import com.cgfay.cameralibrary.engine.model.GalleryType;
import com.cgfay.cameralibrary.engine.recorder.PreviewRecorder;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.cameralibrary.listener.OnPageOperationListener;
import com.cgfay.cameralibrary.utils.PathConstraints;
import com.cgfay.cameralibrary.widget.AspectFrameLayout;
import com.cgfay.cameralibrary.widget.CainSurfaceView;
import com.cgfay.cameralibrary.widget.HorizontalIndicatorView;
import com.cgfay.cameralibrary.widget.PopupSettingView;
import com.cgfay.cameralibrary.widget.RatioImageView;
import com.cgfay.cameralibrary.widget.ShutterButton;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.multimedia.VideoCombiner;
import com.cgfay.utilslibrary.fragment.PermissionConfirmDialogFragment;
import com.cgfay.utilslibrary.fragment.PermissionErrorDialogFragment;
import com.cgfay.utilslibrary.utils.BitmapUtils;
import com.cgfay.utilslibrary.utils.BrightnessUtils;
import com.cgfay.utilslibrary.utils.PermissionUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener,
        HorizontalIndicatorView.OnIndicatorListener {

    private static final String TAG = "CameraPreviewFragment1";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    // 对焦大小
    private static final int FocusSize = 100;

    // 相机权限使能标志
    private boolean mCameraEnable = false;
    // 存储权限使能标志
    private boolean mStorageWriteEnable = false;
    // 是否需要等待录制完成再跳转
    private boolean mNeedToWaitStop = false;
    // 显示贴纸页面
    private boolean isShowingStickers = false;
    // 显示滤镜页面
    private boolean isShowingFilters = false;


    // 处于延时拍照状态
    private boolean mDelayTaking = false;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private AspectFrameLayout mAspectLayout;
    private CainSurfaceView mCameraSurfaceView;


    // 顶部Button
    private ImageView mBtClose;
    private TextView tvTitle;
    private TextView mBtnSwitch;


    // 道具按钮
    private TextView mBtnTools;
    private TextView btFlash;
    // 快门按钮
    private ShutterButton mBtnShutter;
    // 滤镜按钮
    private TextView mBtnEffect;
    // 视频删除按钮
    private Button mBtnRecordDelete;
    // 视频预览按钮
    private Button mBtnRecordPreview;
    // 相机类型指示器
    private HorizontalIndicatorView mBottomIndicator;
    // 相机类型指示文字
    private List<String> mIndicatorText = new ArrayList<String>();
    // 合并对话框
    private CombineVideoDialogFragment mCombineDialog;
    // 主线程Handler
    private Handler mMainHandler;
    // 持有该Fragment的Activity，onAttach/onDetach中绑定/解绑，主要用于解决getActivity() = null的情况
    private Activity mActivity;
    // 页面跳转监听器
    private OnPageOperationListener mPageListener;
    // 分镜页面
    private PreviewFiltersFragment mCameraFilterFragment;
    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    private Group mGroupViewTop;
    private Group mGroupViewBottom;

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
        mMainHandler = new Handler(context.getMainLooper());
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
                .setCaptureFrameCallback(mCaptureCallback)
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
        mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mCameraSurfaceView = new CainSurfaceView(mActivity);
        mCameraSurfaceView.addOnTouchScroller(mTouchScroller);
        mCameraSurfaceView.addMultiClickListener(mMultiClickListener);
        mAspectLayout.addView(mCameraSurfaceView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        PreviewRenderer.getInstance().setSurfaceView(mCameraSurfaceView);

        mBtClose = view.findViewById(R.id.btCloseImag);
        mBtClose.setOnClickListener(this);

        mBtnSwitch = view.findViewById(R.id.btSwitchCamera);
        mBtnSwitch.setOnClickListener(this);


        mBtnTools = view.findViewById(R.id.btnTools);
        btFlash = view.findViewById(R.id.btFlash);
        btFlash.setOnClickListener(this);
        mBtnTools.setOnClickListener(this);
        mBtnEffect = view.findViewById(R.id.btFilters);
        mBtnEffect.setOnClickListener(this);
        mBottomIndicator = (HorizontalIndicatorView) view.findViewById(R.id.bottom_indicator);
        String[] galleryIndicator = getResources().getStringArray(R.array.gallery_indicator);
        mIndicatorText.addAll(Arrays.asList(galleryIndicator));
        mBottomIndicator.setIndicators(mIndicatorText);
        mBottomIndicator.addIndicatorListener(this);

        mBtnShutter = (ShutterButton) view.findViewById(R.id.btShutter);
        mBtnShutter.setOnShutterListener(mShutterListener);
        mBtnShutter.setOnClickListener(this);

        mBtnRecordDelete = (Button) view.findViewById(R.id.btn_record_delete);
        mBtnRecordDelete.setOnClickListener(this);
        mBtnRecordPreview = (Button) view.findViewById(R.id.btn_record_preview);
        mBtnRecordPreview.setOnClickListener(this);

        mGroupViewTop = view.findViewById(R.id.mGroupViewTop);
        mGroupViewBottom = view.findViewById(R.id.mGroupViewBottom);

    }


    @Override
    public void onResume() {
        super.onResume();
        registerHomeReceiver();
        enhancementBrightness();
        mBtnShutter.setEnableOpened(false);
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
        mBtnShutter.setEnableOpened(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }

    @Override
    public void onDestroy() {
        mPageListener = null;

        // 关掉渲染引擎
        PreviewRenderer.getInstance().destroyRenderer();

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
        if (isShowingFilters) {
            hideEffectView();
            return true;
        } else if (isShowingStickers) {
            hideStickerView();
            return true;
        }
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
                Intent intent = new Intent(mActivity, EditextVideoActivity.class);
                startActivity(intent);
                break;
            case R.id.btFilters:
                showEffectView();
                break;
            case R.id.btShutter:
                takePicture();
                break;
            case R.id.btn_record_delete:
                deleteRecordedVideo(false);
                break;
            case R.id.btn_record_preview:
                stopRecordOrPreviewVideo();
                break;
        }
    }

    @Override
    public void onIndicatorChanged(int currentIndex) {
        if (currentIndex == 0) {
            mCameraParam.mGalleryType = GalleryType.VIDEO;
            // 录制视频状态
            mBtnShutter.setIsRecorder(true);
            // 请求录音权限
            if (!mCameraParam.audioPermitted) {
                requestRecordSoundPermission();
            }

        } else if (currentIndex == 1) {
            mCameraParam.mGalleryType = GalleryType.PICTURE;
            // 拍照状态
            mBtnShutter.setIsRecorder(false);
            if (!mStorageWriteEnable) {
                requestStoragePermission();
            }
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
        isShowingStickers = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mCameraFilterFragment == null) {
            mCameraFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_CAMERA_FILTER,PreviewFiltersFragment.TYPE_VIDEO_SHOT);
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
        isShowingFilters = true;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_COLOR_FILTER,PreviewFiltersFragment.TYPE_VIDEO_SHOT);
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
        if (isShowingStickers) {
            isShowingStickers = false;
            if (mCameraFilterFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mCameraFilterFragment);
                ft.commit();
            }
        }
        showToolsLayout();
    }

    /**
     * 隐藏滤镜页面
     */
    private void hideEffectView() {
        if (isShowingFilters) {
            isShowingFilters = false;
            if (mColorFilterFragment != null) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                ft.hide(mColorFilterFragment);
                ft.commit();
            }
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
     * 拍照
     */
    private void takePicture() {
        if (mStorageWriteEnable) {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay && !mDelayTaking) {
                    mDelayTaking = true;
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDelayTaking = false;
                            PreviewRenderer.getInstance().takePicture();
                        }
                    }, 3000);
                } else {
                    PreviewRenderer.getInstance().takePicture();
                }
            }
        } else {
            requestStoragePermission();
        }
    }


    // ------------------------------- SurfaceView 滑动、点击回调 ----------------------------------
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
                    hideStickerView();
                    hideEffectView();
                }
            });

            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
            if (mCameraParam.touchTake) {
                takePicture();
                return;
            }

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


    // ----------------------------------- 顶部状态栏点击回调 ------------------------------------
    private PopupSettingView.StateChangedListener mStateChangedListener = new PopupSettingView.StateChangedListener() {

        @Override
        public void flashStateChanged(boolean flashOn) {
            CameraEngine.getInstance().setFlashLight(flashOn);
        }

        @Override
        public void onOpenCameraSetting() {
            if (mPageListener != null) {
                mPageListener.onOpenCameraSettingPage();
            }
        }

        @Override
        public void delayTakenChanged(boolean enable) {
            mCameraParam.takeDelay = enable;
        }

        @Override
        public void luminousCompensationChanged(boolean enable) {
            mCameraParam.luminousEnhancement = enable;
            enhancementBrightness();
        }

        @Override
        public void touchTakenChanged(boolean touchTake) {
            mCameraParam.touchTake = touchTake;
        }

        @Override
        public void changeEdgeBlur(boolean enable) {

        }
    };

    // ------------------------------------- 长宽比改变回调 --------------------------------------
    private RatioImageView.OnRatioChangedListener mRatioChangedListener = new RatioImageView.OnRatioChangedListener() {
        @Override
        public void onRatioChanged(AspectRatio type) {
            mCameraParam.setAspectRatio(type);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
                    PreviewRenderer.getInstance().reopenCamera();

                    PreviewRenderer.getInstance().surfaceSizeChanged(mCameraSurfaceView.getWidth(),
                            mCameraSurfaceView.getHeight());
                }
            });
        }
    };


    // ------------------------------------ 拍照回调 ---------------------------------------------
    private OnCaptureListener mCaptureCallback = new OnCaptureListener() {
        @Override
        public void onCapture(final ByteBuffer buffer, final int width, final int height) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String filePath = PathConstraints.getImageCachePath(mActivity);
                    BitmapUtils.saveBitmap(filePath, buffer, width, height);
                    if (mPageListener != null) {
                        mPageListener.onOpenImageEditPage(filePath);
                    }
                }
            });
        }
    };

    // ------------------------------------ 预览回调 ---------------------------------------------
    private OnCameraCallback mCameraCallback = new OnCameraCallback() {

        @Override
        public void onCameraOpened() {
            requestRender();
        }

        @Override
        public void onPreviewCallback(byte[] data) {
            if (mBtnShutter != null && !mBtnShutter.isEnableOpened()) {
                mBtnShutter.setEnableOpened(true);
            }

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
    private ShutterButton.OnShutterListener mShutterListener = new ShutterButton.OnShutterListener() {

        @Override
        public void onStartRecord() {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                return;
            }

            // 隐藏删除按钮
            if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                mBtnRecordPreview.setVisibility(View.GONE);
                mBtnRecordDelete.setVisibility(View.GONE);
            }
            mBtnShutter.setProgressMax((int) PreviewRecorder.getInstance().getMaxMilliSeconds());
            // 添加分割线
            mBtnShutter.addSplitView();

            // 是否允许录制音频
            boolean enableAudio = mCameraParam.audioPermitted && mCameraParam.recordAudio
                    && mCameraParam.mGalleryType == GalleryType.VIDEO;

            // 计算输入纹理的大小
            int width = mCameraParam.previewWidth;
            int height = mCameraParam.previewHeight;
            if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
                width = mCameraParam.previewHeight;
                height = mCameraParam.previewWidth;
            }
            // 开始录制
            PreviewRecorder.getInstance()
                    .setRecordType(mCameraParam.mGalleryType == GalleryType.VIDEO ? PreviewRecorder.RecordType.Video : PreviewRecorder.RecordType.Gif)
                    .setOutputPath(PathConstraints.getVideoCachePath(mActivity))
                    .enableAudio(enableAudio)
                    .setRecordSize(width, height)
                    .setOnRecordListener(mRecordListener)
                    .startRecord();
        }

        @Override
        public void onStopRecord() {
            PreviewRecorder.getInstance().stopRecord();
        }

        @Override
        public void onProgressOver() {
            // 如果最后一秒内点击停止录制，则仅仅关闭录制按钮，因为前面已经停止过了，不做跳转
            // 如果最后一秒内没有停止录制，否则停止录制并跳转至预览页面
            if (PreviewRecorder.getInstance().isLastSecondStop()) {
                // 关闭录制按钮
                mBtnShutter.closeButton();
            } else {
                stopRecordOrPreviewVideo();
            }
        }
    };

    /**
     * 录制监听器
     */
    private OnRecordListener mRecordListener = new OnRecordListener() {

        @Override
        public void onRecordStarted() {
            // 编码器已经进入录制状态，则快门按钮可用
            mBtnShutter.setEnableEncoder(true);
        }

        @Override
        public void onRecordProgressChanged(final long duration) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 设置进度
                    mBtnShutter.setProgress(duration);
                    // 设置时间
                }
            });
        }

        @Override
        public void onRecordFinish() {
            // 编码器已经完全释放，则快门按钮可用
            mBtnShutter.setEnableEncoder(true);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 处于录制状态点击了预览按钮，则需要等待完成再跳转， 或者是处于录制GIF状态
                    if (mNeedToWaitStop || mCameraParam.mGalleryType == GalleryType.GIF) {
                        // 开始预览
                        stopRecordOrPreviewVideo();
                    }
                    // 显示删除按钮
                    if (mCameraParam.mGalleryType == GalleryType.VIDEO) {
                        mBtnRecordPreview.setVisibility(View.VISIBLE);
                        mBtnRecordDelete.setVisibility(View.VISIBLE);
                    }
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
        // 处于删除模式，则删除文件
        if (mBtnShutter.isDeleteMode()) {
            // 删除视频，判断是否清除所有
            if (clearAll) {
                // 清除所有分割线
                mBtnShutter.cleanSplitView();
                PreviewRecorder.getInstance().removeAllSubVideo();
            } else {
                // 删除分割线
                mBtnShutter.deleteSplitView();
                PreviewRecorder.getInstance().removeLastSubVideo();
            }

            // 删除一段已记录的时间
            PreviewRecorder.getInstance().deleteRecordDuration();

            // 更新进度
            mBtnShutter.setProgress(PreviewRecorder.getInstance().getVisibleDuration());
            // 更新时间
            // 如果此时没有了视频，则恢复初始状态
            if (PreviewRecorder.getInstance().getNumberOfSubVideo() <= 0) {
                mBtnRecordDelete.setVisibility(View.GONE);
                mBtnRecordPreview.setVisibility(View.GONE);
                mNeedToWaitStop = false;
            }
        } else { // 没有进入删除模式则进入删除模式
            mBtnShutter.setDeleteMode(true);
        }
    }

    /**
     * 停止录制或者预览视频
     */
    private void stopRecordOrPreviewVideo() {
        if (PreviewRecorder.getInstance().isRecording()) {
            mNeedToWaitStop = true;
            PreviewRecorder.getInstance().stopRecord(false);
        } else {
            mNeedToWaitStop = false;
            // 销毁录制线程
            PreviewRecorder.getInstance().destroyRecorder();
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
        public void onCombineFinished(final boolean success) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCombineDialog != null) {
                        mCombineDialog.dismiss();
                        mCombineDialog = null;
                    }
                }
            });
            if (mPageListener != null) {
                mPageListener.onOpenVideoEditPage(combinePath);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                        // 取消录制
                        PreviewRecorder.getInstance().cancelRecording();
                        // 重置进入条
                        mBtnShutter.setProgress((int) PreviewRecorder.getInstance().getVisibleDuration());
                        // 删除分割线
                        mBtnShutter.deleteSplitView();
                        // 关闭按钮
                        mBtnShutter.closeButton();
                    }
                }
            }
        }
    };

    /**
     * 设置页面监听器
     *
     * @param listener
     */
    public void setOnPageOperationListener(OnPageOperationListener listener) {
        mPageListener = listener;
    }
}