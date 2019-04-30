package com.cgfay.cameralibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.EffectResourceAdapter;
import com.cgfay.cameralibrary.fragment.PreviewFiltersFragment;
import com.cgfay.cameralibrary.fragment.VoiceAdjustFragment;
import com.cgfay.cameralibrary.media.VideoRenderThread;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.media.bean.VideoEffect;
import com.cgfay.cameralibrary.media.bean.VideoEffectType;
import com.cgfay.cameralibrary.media.surface.EncodeDecodeSurface;
import com.cgfay.cameralibrary.media.surface.OffSVideoRenderManager;
import com.cgfay.cameralibrary.utils.ImageBlur;
import com.cgfay.cameralibrary.widget.SpaceItemDecoration;
import com.cgfay.cameralibrary.widget.VideoEffectSeekBar;
import com.cgfay.cameralibrary.widget.VideoPreviewView;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.utilslibrary.utils.BitmapUtils;
import com.cgfay.utilslibrary.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Harrison 唐广森
 * @description: 特效编辑
 * 带有时间的特效，重叠的时候，有添加和后加的重叠的区别
 * @date :2019/4/1 14:29
 */
public class EffectVideoActivity extends AppCompatActivity implements View.OnClickListener {
    // 显示滤镜页面
    private boolean isShowingFilters = false;
    private static final String KEY_VIDEO_PATH = "videoPath";
    private static final String BUNDLE_VIDEO_PATH = "bundle";
    private SparseArray<DynamicColor> mDynamicColorFilter = new SparseArray<>();

    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    private String videoPath;
    private VideoPreviewView mVideoPreviewView;
    private VideoRenderer mVideoRenderer;

    // 设置video paths
    public static final int MSG_VIDEO_PLAY_PROGRESS = 0x001;
    public static final int MSG_VIDEO_PLAY_STATUS_STOP = 0x002;
    public static final int MSG_VIDEO_PLAY_STATUS_START = 0x003;
    private int currentEffectKey;
    private boolean isStartClick;

    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    //
    private VoiceAdjustFragment mVoiceAdjustFragment;
    /**
     * 记录 video 的特效时间
     */
    private List<VideoEffect> mVideoEffects = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_VIDEO_PLAY_PROGRESS:
                    int progress = mVideoRenderer.getVideoProgress();
                    String time = StringUtils.generateTime(progress);
                    tvStartTime.setText(TextUtils.isEmpty(time) ? "00:00" : time);
                    mSeekBar.setProgress(progress);
                    mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
                    break;
                case MSG_VIDEO_PLAY_STATUS_STOP:
                    mVideoPlayStatus.setVisibility(View.VISIBLE);
                    break;
                case MSG_VIDEO_PLAY_STATUS_START:
                    mVideoPlayStatus.setVisibility(View.GONE);
                    break;

            }
        }
    };
    private ConstraintLayout mRootView;
    private RecyclerView mRecyclerView;
    private TextView tvTotalTime, tvStartTime;
    private ImageView mVideoPlayStatus;
    private VideoEffectSeekBar mSeekBar;
    private List<ResourceData> mResourceData = new ArrayList<>();
    private EffectResourceAdapter mPreviewResourceAdapter;
    private long mStartClickTime;
    private int currentVideoEffectIndex = -1;
    private VideoEffect newVideoEffect;
    private TextView btSave;
    private boolean isVideoPlayCompleted;
    private Group mainGroup;
    private Group effectGroup;
    private FrameLayout mAspectLayout;
    private ImageView btCloseImag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_effect_video);
        mVideoRenderer = new VideoRenderer();
        VideoEffectType mVideoEffectType = new VideoEffectType()
                .setCurrentEffectType(VideoEffectType.EFFECT_TYPE_SINGLE)
                .setCurrentRendererType(VideoEffectType.RENDER_TYPE_AT_TIME);

        mVideoRenderer.initRenderer(this.getApplicationContext(), mVideoEffectType);

        initView();
        initData();
    }

    private void initData() {
        Bundle bundle = getIntent().getBundleExtra(BUNDLE_VIDEO_PATH);
        videoPath = bundle.getString(KEY_VIDEO_PATH);
        //设置播放的视频路径
        mVideoRenderer.setVideoPaths(videoPath);
        // 绑定需要渲染的SurfaceView
        mVideoRenderer.setSurfaceView(mVideoPreviewView);

        EncodeDecodeSurface test = new EncodeDecodeSurface();
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/out.mp4";
        test.setVideoPath(videoPath, outputPath);
        //初始化渲染的管理
        OffSVideoRenderManager.getInstance().init(this.getApplicationContext());
        try {
            test.testEncodeDecodeSurface();
        } catch (Throwable a) {
            a.printStackTrace();
            Log.e("Harrison", a.getLocalizedMessage());
        }

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                //获取第一帧图片并模糊
                ResourceHelper.initEffectFilterResource(EffectVideoActivity.this, mResourceData);

                final Bitmap bitmap = BitmapUtils.createVideoThumbnail(videoPath);
                ImageBlur.blurBitmap(bitmap, 10);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.setBackground(new BitmapDrawable(getResources(), bitmap));
                        mPreviewResourceAdapter.notifyDataSetChanged();
                    }
                });
            }
        });


        //设置videoPlayer 状态监听
        mVideoRenderer.setVideoPlayerStatusChangeLisenter(new VideoRenderThread.VideoPlayerStatusChangeLisenter() {
            @Override
            public void videoStart(final int totalTime) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setMax(totalTime);
                        Log.e("Harrison", "totalTime" + totalTime);
                        String time = StringUtils.generateTime(totalTime);
                        tvTotalTime.setText(TextUtils.isEmpty(time) ? "00:00" : time);
                        mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
                        //设置Seekbar 的颜色

                    }
                });
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_START);

            }

            @Override
            public void videoStop() {
                mHandler.removeMessages(MSG_VIDEO_PLAY_PROGRESS);
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_STOP);
            }

            @Override
            public void videoRestart() {
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_STATUS_START);

            }

            @Override
            public void videoCompleted() {
                Log.e("Harrison", "videoCompleted");
                //只有在按下的时候，去改变
                if (isStartClick)
                    isVideoPlayCompleted = true;
            }


        });

    }

    public static void gotoThis(Context context, String path) {
        Intent intent = new Intent(context, EffectVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(KEY_VIDEO_PATH, path);
        intent.putExtra(BUNDLE_VIDEO_PATH, bundle);
        context.startActivity(intent);
    }

    /**
     * 初始化页面
     *
     * @param
     */
    private void initView() {
        mAspectLayout = findViewById(R.id.layout_aspect);
        mRecyclerView = findViewById(R.id.recyclerView);

        mRootView = findViewById(R.id.rootView);
        mSeekBar = findViewById(R.id.seekBar);
        tvTotalTime = findViewById(R.id.totalTime);
        tvStartTime = findViewById(R.id.startTime);
        mVideoPlayStatus = findViewById(R.id.imgVideo);
        ImageView imgNext = findViewById(R.id.imgNext);
        imgNext.setOnClickListener(this);
        mVideoPlayStatus.setVisibility(View.GONE);
        btSave = findViewById(R.id.btSave);
        btCloseImag = findViewById(R.id.btCloseImag);
        btSave.setOnClickListener(this);
        btCloseImag.setOnClickListener(this);

        mVideoPlayStatus.setVisibility(View.VISIBLE);
        View btFilters = findViewById(R.id.btFilters);
        View btEffect = findViewById(R.id.btEffect);
        View btVoiceAdjust = findViewById(R.id.btVoiceAdjust);
        btVoiceAdjust.setOnClickListener(this);
        btEffect.setOnClickListener(this);
        effectGroup = findViewById(R.id.effectGroup);
        mainGroup = findViewById(R.id.mainGroup);
        effectGroup.setVisibility(View.GONE);
        mainGroup.setVisibility(View.VISIBLE);

        btFilters.setOnClickListener(this);


        //mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mVideoPreviewView = new VideoPreviewView(this);

        mAspectLayout.addView(mVideoPreviewView);
        mAspectLayout.requestLayout();
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainGroup.getVisibility() == View.VISIBLE) {
                    hideFilterView();
                } else {
                    if (mVideoRenderer.isVideoPlay()) {
                        mVideoRenderer.stopPlayVideo();
                    } else {
                        mVideoRenderer.startPlayVideo();
                    }
                }

            }
        });


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //改变视频的位置
                    mVideoRenderer.changeVideoProgress(progress);
                }

                int size = mVideoEffects.size();
                if (isStartClick) {
                    //这个条件就作为，再次经过起点
                    if (progress > newVideoEffect.getStartTime() && isVideoPlayCompleted) {
                        isVideoPlayCompleted = false;
                        newVideoEffect.setHasAll(true);
                    }
                    newVideoEffect.setEndTime(progress);
                    //更新进度的颜色
                    mSeekBar.setPathList(mVideoEffects, seekBar.getMax());
                    return;
                }
                if (size <= 0 || isStartClick) return;

                for (int i = size - 1; i >= 0; i--) {

                    if ((mVideoEffects.get(i).getStartTime() < mVideoEffects.get(i).getEndTime() &&
                            progress >= mVideoEffects.get(i).getStartTime() && progress < mVideoEffects.get(i).getEndTime()) ||
                            (mVideoEffects.get(i).getStartTime() > mVideoEffects.get(i).getEndTime() &&
                                    (progress >= mVideoEffects.get(i).getStartTime() || progress < mVideoEffects.get(i).getEndTime()))) {
                        //在添加
                        currentVideoEffectIndex = i;
                        VideoEffect videoEffect = mVideoEffects.get(i);
                        int indexFilterColor = videoEffect.getDynamicColorId();
                        DynamicColor color = mDynamicColorFilter.get(indexFilterColor);
                        Log.e("Harrison", "已改变特效" + currentVideoEffectIndex);
                        mVideoRenderer.changeDynamicColorFilter(color);
                        return;
                    } else {
                        //判断之前有没使用过特效
                        if (currentVideoEffectIndex != -1) {
                            VideoEffect videoEffectRemove = mVideoEffects.get(currentVideoEffectIndex);
                            DynamicColor color = mDynamicColorFilter.get(videoEffectRemove.getDynamicColorId());
                            mVideoRenderer.removeDynamic(color);
                            currentVideoEffectIndex = -1;
                            return;

                        }
                    }
                }

                Log.e("Harrison", "不用修改特效");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //触摸Seekbar 停止播放
                mVideoRenderer.stopPlayVideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);

        mPreviewResourceAdapter = new EffectResourceAdapter(this, mResourceData);
        mRecyclerView.setAdapter(mPreviewResourceAdapter);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40, 20));
        //长按对每种特效的记录
        mPreviewResourceAdapter.setOnLongClickLister(new EffectResourceAdapter.OnLongClickLister() {
            @Override
            public void onClickStart(int position) {
                //开始计算使用特效
                parseResource(mResourceData.get(position).type, mResourceData.get(position).unzipFolder, position);
                isStartClick = true;
                int current = mSeekBar.getProgress();
                currentEffectKey = current;
                //如果有重叠的话
                newVideoEffect = new VideoEffect().setStartTime(currentEffectKey).setDynamicColorId(position).setResColorId(position);
                mVideoEffects.add(newVideoEffect);

            }

            @Override
            public void onClickEnd(int position) {
                //结束使用该特效
                DynamicColor color = mDynamicColorFilter.get(position);
                mVideoRenderer.removeDynamic(color);
                if (newVideoEffect.isHasAll()) {
                    newVideoEffect.setStartTime(0);
                    newVideoEffect.setEndTime(mSeekBar.getMax());
                } else {
                    newVideoEffect.setEndTime(mSeekBar.getProgress());
                }
                newVideoEffect.setEndTime(mSeekBar.getProgress());
                isStartClick = false;
            }
        });

    }

    /**
     * 隐藏滤镜页面
     */
    private void hideFilterView() {
        if (isShowingFilters) {
            isShowingFilters = false;
            if (mColorFilterFragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.hide(mColorFilterFragment);
                ft.commit();
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
    private void parseResource(@Nullable ResourceType type, String unzipFolder, int position) {
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                // 单纯的滤镜
                case FILTER: {
                    //
                    if (mDynamicColorFilter.get(position) != null) {
                        DynamicColor color = mDynamicColorFilter.get(position);
                        mVideoRenderer.changeDynamicColorFilter(color);
                    } else {
                        String folderPath = ResourceHelper.getResourceDirectory(EffectVideoActivity.this) + File.separator + unzipFolder;
                        DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                        color.setColorType(ResourceType.FILTER.getIndex());
                        mVideoRenderer.changeDynamicColorFilter(color);
                        mDynamicColorFilter.put(position, color);
                    }


                    break;
                }   // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(EffectVideoActivity.this) + File.separator + unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    mVideoRenderer.changeDynamicResource(sticker);
                    break;
                }
                // 所有数据均为空
                case NONE: {
                    if (mDynamicColorFilter.get(position) != null) {
                        DynamicColor color = mDynamicColorFilter.get(position);
                        mVideoRenderer.changeDynamicColorFilter(color);
                        //    color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                    } else {
                        DynamicColor color = new DynamicColor().setColorType(ResourceType.FILTER.getIndex());
                        mVideoRenderer.changeDynamicColorFilter(color);
                        //  color.addItemTimes(new DynamicColor.ItemTime().setStartTime(mVideoRenderer.getVideoProgress()));
                        mDynamicColorFilter.put(position, color);
                    }
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoRenderer != null) {
            mVideoRenderer.stopPlayVideo();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoRenderer != null) {
            mVideoRenderer.startPlayVideo();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoRenderer != null) {
            mVideoRenderer.destroyRenderer();
        }
    }

    /**
     * 显示滤镜页面
     */
    private void showFilterView() {
        isShowingFilters = true;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_COLOR_FILTER, PreviewFiltersFragment.TYPE_VIDEO_EIDTEXT);
            mColorFilterFragment.setVideoRenderer(mVideoRenderer);
            ft.add(R.id.fragment_container, mColorFilterFragment);
        } else {
            ft.show(mColorFilterFragment);
        }
        //隐藏其他的
        if (mVoiceAdjustFragment != null && mVoiceAdjustFragment.isAdded()) {
            ft.hide(mVoiceAdjustFragment);
        }
        ft.commit();
        //  hideToolsLayout();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btSave:
                int size = mVideoEffects.size();
                for (int i = size - 1; i >= 0; i--) {
                    Log.e("Harrison", "posistion:" + mVideoEffects.get(i).getDynamicColorId() +
                            "*start:" + mVideoEffects.get(i).getStartTime() + "*end:" + mVideoEffects.get(i).getEndTime());
                }
                break;
            case R.id.btFilters:
                showFilterView();
                break;
            case R.id.btEffect:
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mRootView);
                constraintSet.clear(R.id.layout_aspect);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.TOP, R.id.btCloseImag, ConstraintSet.BOTTOM, 70);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 150);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 150);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, R.id.startTime, ConstraintSet.TOP, 70);
                constraintSet.applyTo(mRootView);
                TransitionManager.beginDelayedTransition(mRootView, new Transition() {
                    @Override
                    public void captureStartValues(TransitionValues transitionValues) {
                        mainGroup.setVisibility(View.GONE);
                        effectGroup.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void captureEndValues(TransitionValues transitionValues) {

                    }
                });

                break;

            case R.id.btCloseImag:
                if (mainGroup.getVisibility() == View.GONE) {
                    if (!mVideoRenderer.isVideoPlay()) {
                        //如果沒播放的話，好像回到全屏是有问题的
                        mVideoRenderer.startPlayVideo();
                    }
                    if (mVideoPlayStatus.getVisibility() == View.VISIBLE) {
                        mVideoPlayStatus.setVisibility(View.GONE);
                    }
                    ConstraintSet set = new ConstraintSet();
                    set.clone(mRootView);
                    set.clear(R.id.layout_aspect);
                    set.connect(R.id.layout_aspect, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
                    set.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
                    set.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
                    set.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);

                    set.applyTo(mRootView);
                    TransitionManager.beginDelayedTransition(mRootView, new Transition() {
                        @Override
                        public void captureStartValues(TransitionValues transitionValues) {
                            effectGroup.setVisibility(View.GONE);
                        }

                        @Override
                        public void captureEndValues(TransitionValues transitionValues) {
                            mainGroup.setVisibility(View.VISIBLE);
                        }
                    });

                }
                break;
            case R.id.btVoiceAdjust:
                showVoiceAdjust();
                break;
            case R.id.imgNext:
                //再次渲染特效成mp4

                break;


        }
    }

    /**
     * 显示声音的调节
     */
    private void showVoiceAdjust() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mVoiceAdjustFragment == null) {
            mVoiceAdjustFragment = VoiceAdjustFragment.getInstance();
            ft.add(R.id.fragment_container, mVoiceAdjustFragment);
            mVoiceAdjustFragment.setOnVoiceSeekBarChangeListener(new VoiceAdjustFragment.OnVoiceSeekBarChangeListener() {

                @Override
                public void origiVoiceChange(float progress) {
                    Log.e("Harrison", "origiVoiceChange" + progress);
                    mVideoRenderer.changeVideoVoice(progress);
                }

                @Override
                public void bgmVoiceChange(float progres) {
                    Log.e("Harrison", "bgmVoiceChange");
                }
            });
        } else {
            ft.show(mVoiceAdjustFragment);
        }
        //隐藏其他的
        if (mColorFilterFragment != null && mColorFilterFragment.isAdded()) {
            ft.hide(mColorFilterFragment);
        }
        ft.commit();
    }


}
