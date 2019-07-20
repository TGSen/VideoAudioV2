package com.owoh.video.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionValues;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.mp4compose.FillMode;
import com.cgfay.filterlibrary.mp4compose.composer.Mp4Composer;
import com.cgfay.filterlibrary.mp4compose.filter.GlFilterGroup;
import com.cgfay.filterlibrary.utils.BitmapUtils;
import com.cgfay.filterlibrary.utils.DensityUtils;
import com.cgfay.filterlibrary.utils.StringUtils;
import com.owoh.R;
import com.owoh.video.ItemSticker;
import com.owoh.video.adapter.EffectResourceAdapter;
import com.owoh.video.adapter.ThumbVideoAdapter;
import com.owoh.video.filter.GLColorFilter;
import com.owoh.video.filter.GLEffectFilter;
import com.owoh.video.filter.GLStickerFilter;
import com.owoh.video.fragment.PreviewFiltersFragment;
import com.owoh.video.fragment.StickersFragment;
import com.owoh.video.fragment.VoiceAdjustFragment;
import com.owoh.video.media.VideoRenderThread;
import com.owoh.video.media.VideoRenderer;
import com.owoh.video.media.bean.VideoEffect;
import com.owoh.video.media.bean.VideoEffectType;
import com.owoh.video.video.ExtractFrameWorkThread;
import com.owoh.video.video.VideoEditInfo;
import com.owoh.video.widget.DragSeekBar;
import com.owoh.video.widget.RangeSeekBar;
import com.owoh.video.widget.SpaceItemDecoration;
import com.owoh.video.widget.VideoEffectSeekBar;
import com.owoh.video.widget.VideoPreviewView;
import com.owoh.video.widget.sticker.BitmapStickerIcon;
import com.owoh.video.widget.sticker.DeleteIconEvent;
import com.owoh.video.widget.sticker.DrawableSticker;
import com.owoh.video.widget.sticker.GifSticker;
import com.owoh.video.widget.sticker.Sticker;
import com.owoh.video.widget.sticker.StickerIconEvent;
import com.owoh.video.widget.sticker.StickerView;
import com.owoh.video.widget.sticker.ZoomIconEvent;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;

import static com.owoh.video.media.bean.VideoEffectTypeKt.EFFECT_TYPE_SINGLE;
import static com.owoh.video.media.bean.VideoEffectTypeKt.RENDER_TYPE_AT_TIME;

/**
 * @author Harrison 唐广森
 * @description: 特效编辑
 * 带有时间的特效，重叠的时候，有添加和后加的重叠的区别
 * @date :2019/4/1 14:29
 */
public class EffectVideoActivityV extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Harrison";
    // 显示滤镜页面
    private boolean isShowingFilters = false;
    private static final String KEY_VIDEO_PATH = "videoPath";
    private static final String BUNDLE_VIDEO_PATH = "bundle";
    private SparseArray<DynamicColor> mDynamicColorFilter = new SparseArray<>();

    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    private String videoPath;
    private VideoPreviewView mVideoPreviewView;
    private VideoRenderer mVideoRenderer;
    private int mMaxWidth; //可裁剪区域的最大宽度

    // 设置video paths
    public static final int MSG_VIDEO_PLAY_PROGRESS = 0x001;
    public static final int MSG_VIDEO_PLAY_STATUS_STOP = 0x002;
    public static final int MSG_VIDEO_PLAY_STATUS_START = 0x003;
    private int currentEffectKey;
    private boolean isStartClick;

    private static final long MIN_CUT_DURATION = 0L;// 最小剪辑时间3s
    private static final long MAX_CUT_DURATION = 10 * 1000L;//视频最多剪切多长时间
    private static final int MAX_COUNT_RANGE = 10;//seekBar的区域内一共有多少张图片

    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    // 滤镜页面
    private StickersFragment mStickerFragment;
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
                    try{
                        int progress = mVideoRenderer.getVideoProgress();
                        String time = StringUtils.generateTime(progress);
                        tvStartTime.setText(TextUtils.isEmpty(time) ? "00:00" : time);
                        mSeekBar.setProgress(progress);
                        mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);

                        //改变贴纸的显示时间
                        mStickerSeekBar.setProgress(progress);
                    }catch (Exception e){
                        Log.e("Harrison","****"+e.getLocalizedMessage());
                    }


                    break;
                case MSG_VIDEO_PLAY_STATUS_STOP:
                    //贴纸的暂停不需要显示
                    if (layoutStickerTool.getVisibility() == View.GONE)
                        mVideoPlayStatus.setVisibility(View.VISIBLE);
                    break;
                case MSG_VIDEO_PLAY_STATUS_START:
                    mVideoPlayStatus.setVisibility(View.GONE);
                    break;
                case ExtractFrameWorkThread.MSG_SAVE_SUCCESS:
                    VideoEditInfo info = (VideoEditInfo) msg.obj;
                    Log.e("Harrison", "info:" + info.getPath());
                    mVideoEditAdapter.addItemVideoInfo(info);
                    mVideoEditAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    public void test(){
    }
    private ConstraintLayout mRootView;
    private RecyclerView mRecyclerView, mThumbRecyclerView;
    private TextView tvTotalTime, tvStartTime, mStickerTimeTv;
    private ImageView mVideoPlayStatus/*,imgVideoSmall*/;
    private VideoEffectSeekBar mSeekBar;
    private DragSeekBar mStickerSeekBar;
    private List<ResourceData> mResourceData = new ArrayList<>();
    private EffectResourceAdapter mPreviewResourceAdapter;
    private long mStartClickTime;
    private int currentVideoEffectIndex = -1;
    private VideoEffect newVideoEffect;
    private TextView btSave;
    private boolean isVideoPlayCompleted;
    private Group mainGroup;
    private Group effectGroup;
    private ConstraintLayout layoutStickerTool;
    private FrameLayout mAspectLayout;
    private ImageView btCloseImag;
    private StickerView mStickerView;
    private RangeSeekBar mRangeSeekBar;
    private int MARGIN = 46;
    private ExtractFrameWorkThread mExtractFrameWorkThread;
    private ThumbVideoAdapter mVideoEditAdapter;
    private float averageMsPx;
    private boolean isCombine;
    private GLStickerFilter glStickerFilter;
    private GLEffectFilter mEffectFilter;
    //该视频是否有该截图
    private boolean isVideoRange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext(), "c5db1d8f24", false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_effect_video);
        mVideoRenderer = new VideoRenderer();
        VideoEffectType mVideoEffectType = new VideoEffectType()
                .setCurrentEffectType(EFFECT_TYPE_SINGLE)
                .setCurrentRendererType(RENDER_TYPE_AT_TIME);

        mVideoRenderer.initRenderer(this.getApplicationContext(), mVideoEffectType);

        initView();
        initData();

        initStickerView();
    }

    /**
     * 初始化贴纸 的view
     */
    private void initStickerView() {
        mStickerView = findViewById(R.id.stickerView);

        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.mipmap.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.mipmap.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
                R.mipmap.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new StickerIconEvent() {
            @Override
            public void onActionDown(StickerView stickerView, MotionEvent event) {
                mStickerView.setCurrentSticker();
            }

            @Override
            public void onActionMove(StickerView stickerView, MotionEvent event) {

            }

            @Override
            public void onActionUp(final StickerView stickerView, MotionEvent event) {
                if (layoutStickerTool.getVisibility() == View.VISIBLE && stickerView.isCurrentSticker()) {
                    mStickerView.setBorder(false);
                    return;
                }
                mStickerTimeTv.setText(String.format(getResources().getString(R.string.sticker_choose_time), String.valueOf(new BigDecimal(stickerView.getRangeTime()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())));

                mainGroup.setVisibility(View.GONE);
                effectGroup.setVisibility(View.GONE);
                layoutStickerTool.setVisibility(View.VISIBLE);
                //关闭贴纸列表
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                hideFragment(transaction);
                transaction.commit();
                layoutStickerTool.post(new Runnable() {
                    @Override
                    public void run() {
                        int[] margin = calculation();
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(mRootView);
                        constraintSet.clear(R.id.layout_aspect);
                        constraintSet.constrainWidth(R.id.layout_aspect, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                        constraintSet.constrainHeight(R.id.layout_aspect, ConstraintLayout.LayoutParams.WRAP_CONTENT);

                        constraintSet.connect(R.id.layout_aspect, ConstraintSet.TOP, R.id.btCloseImag, ConstraintSet.BOTTOM);
                        constraintSet.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
                        constraintSet.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
                        constraintSet.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, R.id.layoutStickerTool, ConstraintSet.TOP);

                        constraintSet.applyTo(mRootView);
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mAspectLayout.getLayoutParams();

                        layoutParams.width = margin[0];
                        layoutParams.height = margin[1];
                        mAspectLayout.setLayoutParams(layoutParams);
                        //改变该触摸的sticker 范围
                        mRangeSeekBar.setNormalizedMaxValue(stickerView.getCurrentStickerMaxValue());
                        mRangeSeekBar.setNormalizedMinValue(stickerView.getCurrentStickerMinValue());


                        if (isVideoRange)
                            return;

                        EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                isVideoRange = true;
                                //如果存在的 话，那么就不用在截取了
                                String outPutFileDirPath = getExternalCacheDir().getAbsolutePath() + "/";
                                int extractW = mMaxWidth / MAX_COUNT_RANGE;
                                int extractH = DensityUtils.dp2px(EffectVideoActivityV.this, 62);
                                mExtractFrameWorkThread = new ExtractFrameWorkThread(extractW, extractH, mHandler, videoPath,
                                        outPutFileDirPath, 0, mSeekBar.getMax(), MAX_COUNT_RANGE);
                                thumbnailsCount = (int) (mSeekBar.getMax() * 1.0f / (MAX_CUT_DURATION * 1.0f) * MAX_COUNT_RANGE);
                                rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount;
                                averageMsPx = mSeekBar.getMax() * 1.0f / rangeWidth * 1.0f;
                                mExtractFrameWorkThread.start();
                            }
                        });
                    }
                });


            }
        });

        mStickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon));
        mStickerView.setBackgroundColor(Color.TRANSPARENT);
        mStickerView.setLocked(false);
        mStickerView.setConstrained(true);


        mStickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerAdded");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                //stickerView.removeAllSticker();

                Log.e(TAG, "onStickerClicked");
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerDeleted");
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerDragFinished");
            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerTouchedDown");
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerZoomFinished");
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
                Log.e(TAG, "onStickerFlipped");
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                Log.e(TAG, "onDoubleTapped: double tap will be with two click");
            }

            @Override
            public void onStickerTouchedOutSide() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                hideFragment(ft);
                ft.commit();
            }
        });


    }

    /**
     * //得计算比例，宽和高都是720*1280 的比例，得注意底部的高度
     *
     * @return
     */
    private int[] calculation() {


        int[] border = new int[2];

        int exWidth = 720;
        int exHeight = 1280;
        //左右两边最小的边距
        int minLeft = 40;
        int minBottom = 40;
        //计算一个我认为最佳的比例
        int width = DensityUtils.getDisplayWidthPixels(EffectVideoActivityV.this);
        int height = DensityUtils.getDisplayHeightPixels(EffectVideoActivityV.this);
        int[] position = new int[2];
        btCloseImag.getLocationOnScreen(position);
        Log.e("Harrison", layoutStickerTool.getHeight() + "*" + "height*" + height + "width" + width);
        //开始的比例
        float ratio = 1.2f;
        float step = 0.1f;
        while (true) {
            if ((exWidth * ratio > width - minLeft * 2) || (exHeight * ratio > height - layoutStickerTool.getHeight() - minBottom * 2 - position[1])) {
                ratio -= step;
                if (ratio <= 0) {
                    ratio = step;
                    break;
                }
            } else {
                break;
            }
        }
        border[0] = (int) (exWidth * ratio);
        border[1] = (int) (exHeight * ratio);
        return border;
    }


    private void initData() {

        Bundle bundle = getIntent().getBundleExtra(BUNDLE_VIDEO_PATH);
        videoPath = bundle.getString(KEY_VIDEO_PATH);
        //设置播放的视频路径
        mVideoRenderer.setVideoPaths(videoPath);
        // 绑定需要渲染的SurfaceView
        mVideoRenderer.setSurfaceView(mVideoPreviewView);

        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                //获取第一帧图片并模糊
                ResourceHelper.initEffectFilterResource(EffectVideoActivityV.this, mResourceData);

                final Bitmap bitmap = BitmapUtils.createVideoThumbnail(videoPath);
//                ImageBlur.blurBitmap(bitmap, 10);
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
                        mStickerSeekBar.setMax(totalTime);
                        Log.e("Harrison", "totalTime" + totalTime);
                        String time = StringUtils.generateTime(totalTime);
                        tvTotalTime.setText(TextUtils.isEmpty(time) ? "00:00" : time);
                        mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);

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

    //特效，贴纸，合成Mp4
    private void combineFilterToVideoFile() {
        isCombine = true;
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String outputPath = getExternalCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp4";
                //获取视频文件的宽高
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath);
                final int videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                final int videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                retriever.release();
                final int widthScreen = DensityUtils.getDisplayWidthPixels(EffectVideoActivityV.this);
                final int heightScreen = DensityUtils.getDisplayHeightPixels(EffectVideoActivityV.this);
                final GlFilterGroup filterGroup = new GlFilterGroup();

                //这个是颜色的滤镜
                if (mColorFilterFragment != null) {
                    DynamicColorData colorData = mColorFilterFragment.getCurrentColorFilter();
                    if (colorData != null) {
                        GLColorFilter colorFilter = new GLColorFilter();
                        colorFilter.setFragmentShaderSource(OpenGLUtils.getShaderFromFile(colorData.getFsPath()));
                        Log.e("Harrison", OpenGLUtils.getShaderFromFile(colorData.getFsPath()));
                        filterGroup.addFilterItem(colorFilter);
                    }
                }

                //这个是特效的滤镜切换
                mEffectFilter = new GLEffectFilter();
                mEffectFilter.setFilters(mVideoEffects);

                //计算比例
                final float screenScale = Math.min((float) videoWidth / widthScreen, (float) videoHeight / heightScreen);
                final Canvas gifCanvas = new Canvas();

                glStickerFilter = new GLStickerFilter() {
                    Bitmap bitmap = null;
                    Matrix matrix = new Matrix();

                    @Override
                    protected void drawCanvas(Canvas canvas, Paint mPaint) {
                        List<Sticker> stickers = mStickerView.getStickers();
                        Log.e("Harrison", "********" + mStickerView.getStickerCount());
                        for (int i = 0; i < mStickerView.getStickerCount(); i++) {
                            Log.e("Harrison", "********))))))))))))))" + i);
                            final Sticker sticker = stickers.get(i);
                            if (sticker.getStartTime() > glStickerFilter.getCurrentTime() || sticker.getEndTime() < glStickerFilter.getCurrentTime()) {
                                continue;
                            }
                            float stickerScale = sticker.getCurrentScale();
                            Drawable drawable = sticker.getDrawable();
                            int drawableWith = drawable.getIntrinsicWidth();
                            float centerX = sticker.getMappedCenterPoint().x / widthScreen * canvas.getWidth();
                            float centerY = sticker.getMappedCenterPoint().y / heightScreen * canvas.getHeight();
                            matrix.reset();

                            if (sticker instanceof GifSticker) {
                                //判断之前有没给drawable 设置
                                GifDrawable gifDrawable = (GifDrawable) drawable;
                                float currentScale = stickerScale * screenScale;
                                bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(), drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
                                gifCanvas.setBitmap(bitmap);
                                matrix.postScale(currentScale, currentScale);
                                matrix.postTranslate(centerX - bitmap.getWidth() * currentScale / 2, centerY - bitmap.getHeight() * currentScale / 2);
                                matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY);
                                gifDrawable.draw(gifCanvas);
                                canvas.drawBitmap(bitmap, matrix, mPaint);
                            } else if (sticker instanceof DrawableSticker) {
                                BitmapDrawable bd = (BitmapDrawable) drawable;
                                bitmap = bd.getBitmap();
                                //计算比例,屏幕的比例等等
                                float bitmapScale = (float) drawableWith / (float) bitmap.getWidth();
                                float currentScale = stickerScale * screenScale * bitmapScale;
                                //计算按照sticker的中心值，计算百分比宽高
                                matrix.postScale(currentScale, currentScale);
                                matrix.postTranslate(centerX - bitmap.getWidth() * currentScale / 2, centerY - bitmap.getHeight() * currentScale / 2);
                                matrix.postRotate(sticker.getCurrentAngle(), centerX, centerY);

                            }
                            if(bitmap!=null){
                                canvas.drawBitmap(bitmap, matrix, mPaint);
                            }
                        }
                    }


                };
                //滤镜的先后有一定的影响
//                 filterGroup.addFilterItem(new GlMonochromeFilter());
                filterGroup.addFilterItem(mEffectFilter);
                filterGroup.addFilterItem(glStickerFilter);


                new Mp4Composer(videoPath, outputPath)
                        .size(videoWidth, videoHeight)
                        .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                        .filter(filterGroup)
                        .listener(new Mp4Composer.Listener() {
                            @Override
                            public void onProgress(double progress, double time) {
                                //time 是纳秒的，需要除以 1000 转化为毫秒 好计算
                                double times = time / 1000;
                                glStickerFilter.setCurrentTime(times);
                                mEffectFilter.setCurrentTime(times);
                            }

                            @Override
                            public void onCompleted() {
//                                Log.e(TAG, "onCompleted()");
                                isCombine = false;
                            }

                            @Override
                            public void onCanceled() {
//                                Log.e(TAG, "onCanceled");
                                isCombine = false;
                            }

                            @Override
                            public void onFailed(Exception exception) {
//                                Log.e(TAG, "onFailed()", exception);
                                isCombine = false;
                            }
                        })
                        .start();
            }
        });
    }

    public static void gotoThis(Context context, String path) {
        Intent intent = new Intent(context, EffectVideoActivityV.class);
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
        mRecyclerView = findViewById(R.id.effectRecyclerView);
        mThumbRecyclerView = findViewById(R.id.thumbRecyclerView);

        mRootView = findViewById(R.id.rootView);
        mSeekBar = findViewById(R.id.videoEffectBar);
        mStickerSeekBar = findViewById(R.id.mStickerSeekBar);
        tvTotalTime = findViewById(R.id.totalTime);
        tvStartTime = findViewById(R.id.startTime);
        mVideoPlayStatus = findViewById(R.id.imgVideo);
//        imgVideoSmall = findViewById(R.id.imgVideoSmall);
//        imgVideoSmall.setOnClickListener(this);

        layoutStickerTool = findViewById(R.id.layoutStickerTool);
        ImageView imgNext = findViewById(R.id.imgNext);
        imgNext.setOnClickListener(this);

        TextView sticker = findViewById(R.id.btSticker);
        sticker.setOnClickListener(this);
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
        layoutStickerTool = findViewById(R.id.layoutStickerTool);
        effectGroup.setVisibility(View.GONE);
        layoutStickerTool.setVisibility(View.GONE);
        mainGroup.setVisibility(View.VISIBLE);

        btFilters.setOnClickListener(this);
        mMaxWidth = DensityUtils.getDisplayWidthPixels(this);
        mThumbRecyclerView
                .setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mVideoEditAdapter = new ThumbVideoAdapter(this, mMaxWidth / 11);
        mThumbRecyclerView.setAdapter(mVideoEditAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);


        //贴纸得显示时间选择
        mRangeSeekBar = findViewById(R.id.rangeSeekBar);
        mStickerTimeTv = findViewById(R.id.stickerTime);
        mRangeSeekBar.setSelectedMinValue(0L);
        mRangeSeekBar.setSelectedMaxValue(MAX_CUT_DURATION);
        mRangeSeekBar.setMin_cut_time(MIN_CUT_DURATION);//设置最小裁剪时间
        mRangeSeekBar.setNotifyWhileDragging(true);
        mRangeSeekBar.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);

        mVideoPreviewView = new VideoPreviewView(this);
        mAspectLayout.addView(mVideoPreviewView, 0);
        mAspectLayout.requestLayout();
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainGroup.getVisibility() == View.VISIBLE) {
                    hideFilterView();
                } else {
                    //假如不是贴图的布局显示，那么就可以点击暂停和播放
                    if (layoutStickerTool.getVisibility() != View.VISIBLE) {
                        if (mVideoRenderer.isVideoPlay()) {
                            mVideoRenderer.stopPlayVideo();
                        } else {
                            mVideoRenderer.startPlayVideo();
                        }
                    }

                }

            }
        });
        mStickerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //改变显示的时间
                mStickerView.setShowSticker(progress);
                if (fromUser) {
                    //改变视频的位置
                    mVideoRenderer.changeVideoProgress(progress);
                }

            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //触摸Seekbar 停止播放
                mVideoRenderer.stopPlayVideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoRenderer.startPlayVideo();
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
                        Log.e("Harrison", "经过了同一点");
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
                        // Log.e("Harrison", "已改变特效" + currentVideoEffectIndex);
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

                //  Log.e("Harrison", "不用修改特效");

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
                newVideoEffect = new VideoEffect().setStartTime(currentEffectKey).setDynamicColorId(position).setResColorId(position).setDynamicColor(mDynamicColorFilter.get(position));
                mVideoEffects.add(newVideoEffect);

            }

            @Override
            public void onClickEnd(int position) {
                //结束使用该特效
                DynamicColor color = mDynamicColorFilter.get(position);
                mVideoRenderer.removeDynamic(color);
                if (newVideoEffect.isHasAll()) {
                    Log.e("Harrison", "设置全部");
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

    private long leftProgress, rightProgress; //裁剪视频左边区域的时间位置, 右边时间位置
    private long scrollPos = 0;
    private int mScaledTouchSlop;
    private int lastScrollX;
    private boolean isSeeking;
    private boolean isOverScaledTouchSlop;
    private int thumbnailsCount;
    private int rangeWidth;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.e(TAG, "-------newState:>>>>>" + newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isSeeking = false;
//                videoStart();
            } else {
                isSeeking = true;
//                if (isOverScaledTouchSlop) {
//                   // videoPause();
//                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSeeking = false;
            int scrollX = getScrollXDistance();
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false;
                return;
            }
            isOverScaledTouchSlop = true;
            Log.e(TAG, "-------scrollX:>>>>>" + scrollX);
            //初始状态,why ? 因为默认的时候有56dp的空白！
            if (scrollX == -MARGIN) {
                scrollPos = 0;
            } else {
                // why 在这里处理一下,因为onScrollStateChanged早于onScrolled回调
                // videoPause();
                isSeeking = true;
                scrollPos = (long) (averageMsPx * (MARGIN + scrollX));
                Log.e(TAG, "-------scrollPos:>>>>>" + scrollPos);
                leftProgress = mRangeSeekBar.getSelectedMinValue() + scrollPos;
                rightProgress = mRangeSeekBar.getSelectedMaxValue() + scrollPos;
                Log.e(TAG, "-------leftProgress:>>>>>" + leftProgress);
                // mMediaPlayer.seekTo((int) leftProgress);
            }
            lastScrollX = scrollX;
        }
    };

    /**
     * 水平滑动了多少px
     *
     * @return int px
     */
    private int getScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }


    private final RangeSeekBar.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBar.OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar, long minValue, long maxValue,
                                                int action, boolean isMin, RangeSeekBar.Thumb pressedThumb) {

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    leftProgress = minValue + scrollPos;
                    rightProgress = maxValue + scrollPos;
//                    isSeeking = false;
//                    videoPause();
                    //暂停视频
                    break;
                case MotionEvent.ACTION_MOVE:
//                    isSeeking = true;
//                    mMediaPlayer.seekTo((int) (pressedThumb == RangeSeekBar.Thumb.MIN ?
//                            leftProgress : rightProgress));
                    break;
                case MotionEvent.ACTION_UP:
                    mStickerView.setStickerTime(minValue, maxValue);
//                    isSeeking = false;
//                    //从minValue开始播
//                    mMediaPlayer.seekTo((int) leftProgress);
////                    videoStart();
//                    mTvShootTip
//                            .setText(String.format("裁剪 %d s", (rightProgress - leftProgress) / 1000));

                    break;
                default:
                    break;
            }
            //设置已选择的时间
            mStickerTimeTv.setText(String.format(getResources().getString(R.string.sticker_choose_time), String.valueOf(new BigDecimal((maxValue - minValue) / 1000.0f).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue())));

        }
    };

    /**
     * 显示贴纸的Framelayout
     */
    private void showStickerFragment() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideFragment(ft);
        if (mStickerFragment == null) {
            mStickerFragment = StickersFragment.getInstance();
            ft.add(R.id.fragment_container, mStickerFragment);
            mStickerFragment.setOnStickerAddListener(new StickersFragment.OnStickerPanlListener() {
                @Override
                public void addSticker(ItemSticker item) {

                    if (!TextUtils.isEmpty(item.getPath()) && new File(item.getPath()).exists()) {
                        if (item.getType() == item.getTYPE_GIF()) {
                            //android P 以上版本
//                                drawable = ImageDecoder.decodeDrawable(
//                                        ImageDecoder.createSource(new File(item.getPath())));
//                                if (drawable instanceof AnimatedImageDrawable) {
//                                    ((AnimatedImageDrawable) drawable).start();
//                                }

                            GifDrawable gifDrawable = null;
                            try {
                                gifDrawable = new GifDrawable(item.getPath());
                                gifDrawable.start();
                                mStickerView.addSticker(new GifSticker(gifDrawable).setEndTime(mSeekBar.getMax()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Bitmap bitmap = BitmapFactory.decodeFile(item.getPath());
                            Drawable drawable = new BitmapDrawable(bitmap);
                            mStickerView.addSticker(new DrawableSticker(drawable).setEndTime(mSeekBar.getMax()));
                        }
                    }

                }


            });

        } else {
            ft.show(mStickerFragment);
        }
        ft.commit();
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

                    if (mDynamicColorFilter.get(position) != null) {
                        DynamicColor color = mDynamicColorFilter.get(position);
                        mVideoRenderer.changeDynamicColorFilter(color);
                    } else {
                        String folderPath = ResourceHelper.getResourceDirectory(EffectVideoActivityV.this) + File.separator + unzipFolder;
                        DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                        color.setColorType(ResourceType.FILTER.getIndex());
                        DynamicColorData colorData = color.filterList.get(0);
                        colorData.setVsPath(folderPath + colorData.vertexShader);
                        colorData.setFsPath(folderPath + File.separator + colorData.fragmentShader);
                        mVideoRenderer.changeDynamicColorFilter(color);
                        mDynamicColorFilter.put(position, color);
                    }


                    break;
                }   // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(EffectVideoActivityV.this) + File.separator + unzipFolder;
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
                        DynamicColorData colorData = color.filterList.get(0);
                        colorData.setFsPath("");
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
        if (mVideoRenderer != null) {
            mVideoRenderer.stopPlayVideo();
        }
        super.onStop();


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
        mHandler.removeCallbacksAndMessages(null);
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
        hideFragment(ft);
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

    /**
     * 隐藏其他的Fragment
     *
     * @param ft
     */
    private void hideFragment(FragmentTransaction ft) {

        if (mColorFilterFragment != null && mColorFilterFragment.isAdded()) {
            ft.hide(mColorFilterFragment);
        }
        if (mVoiceAdjustFragment != null && mVoiceAdjustFragment.isAdded()) {
            ft.hide(mVoiceAdjustFragment);
        }
        if (mStickerFragment != null && mStickerFragment.isAdded()) {
            ft.hide(mStickerFragment);
        }

    }

    //这个是特效的
    private void setVideoPreviewSize() {


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btSave:
                break;
//            case R.id.imgVideoSmall:
//                if (!mVideoRenderer.isVideoPlay()) {
//                    //如果沒播放的話，好像回到全屏是有问题的
//                    mVideoRenderer.startPlayVideo();
//                }else{
//                    mVideoRenderer.stopPlayVideo();
//                }
//                imgVideoSmall.setSelected(mVideoRenderer.isVideoPlay());
//                break;
            case R.id.btFilters:
                showFilterView();
                break;
            case R.id.btEffect:
//                mStickerView.setLocked(true);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mRootView);
                constraintSet.clear(R.id.layout_aspect);
                constraintSet.constrainWidth(R.id.layout_aspect, 0);
                constraintSet.constrainHeight(R.id.layout_aspect, 0);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.TOP, R.id.btCloseImag, ConstraintSet.BOTTOM, 70);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 150);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 150);
                constraintSet.connect(R.id.layout_aspect, ConstraintSet.BOTTOM, R.id.startTime, ConstraintSet.TOP, 70);
                constraintSet.applyTo(mRootView);

                TransitionManager.beginDelayedTransition(mRootView, new Transition() {
                    @Override
                    public void captureStartValues(TransitionValues transitionValues) {
                        mainGroup.setVisibility(View.GONE);
                        layoutStickerTool.setVisibility(View.GONE);
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
                            layoutStickerTool.setVisibility(View.GONE);
                        }

                        @Override
                        public void captureEndValues(TransitionValues transitionValues) {
                            mainGroup.setVisibility(View.VISIBLE);
                        }
                    });

                } else {
                    finish();
                }
                break;
            case R.id.btVoiceAdjust:
                showVoiceAdjust();
                break;
            case R.id.imgNext:
                //再次渲染特效成mp4
                if (isCombine) return;
                combineFilterToVideoFile();
                break;
            case R.id.btSticker:
                showStickerFragment();

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
