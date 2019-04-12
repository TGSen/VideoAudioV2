package com.cgfay.cameralibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.EffectResourceAdapter;
import com.cgfay.cameralibrary.adapter.PreviewResourceAdapter;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.cameralibrary.media.VideoRenderThread;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.media.bean.VideoEffectType;
import com.cgfay.cameralibrary.utils.ImageBlur;
import com.cgfay.cameralibrary.widget.SpaceItemDecoration;
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
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Harrison 唐广森
 * @description: 特效编辑
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
    private View mRootView;
    private RecyclerView mRecyclerView;
    private TextView tvTotalTime, tvStartTime;
    private ImageView mVideoPlayStatus;
    private SeekBar mSeekBar;
    private List<ResourceData> mResourceData = new ArrayList<>();
    private EffectResourceAdapter mPreviewResourceAdapter;
    private long mStartClickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_effect_video);
        mVideoRenderer = new VideoRenderer();
        VideoEffectType mVideoEffectType = new VideoEffectType()
                .setCurrentEffectType(VideoEffectType.EFFECT_TYPE_MULTI)
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
        FrameLayout mAspectLayout = findViewById(R.id.layout_aspect);
        mRecyclerView = findViewById(R.id.recyclerView);

        mRootView = findViewById(R.id.rootView);
        mSeekBar = findViewById(R.id.seekBar);
        tvTotalTime = findViewById(R.id.totalTime);
        tvStartTime = findViewById(R.id.startTime);
        mVideoPlayStatus = findViewById(R.id.imgVideo);
        mVideoPlayStatus.setVisibility(View.VISIBLE);
        //设置Seekbar 的颜色
        //mSeekBar.setProgressDrawable();
        //mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mVideoPreviewView = new VideoPreviewView(this);
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  hideFilterView();
            }
        });
        mAspectLayout.addView(mVideoPreviewView);
        mAspectLayout.requestLayout();
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoRenderer.isVideoPlay()) {
                    mVideoRenderer.stopPlayVideo();
                } else {
                    mVideoRenderer.startPlayVideo();
                }
            }
        });
//        mSeekBar.setEnabled(true);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);

        mPreviewResourceAdapter = new EffectResourceAdapter(this, mResourceData);
        mRecyclerView.setAdapter(mPreviewResourceAdapter);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(40, 20));


        mPreviewResourceAdapter.setOnLongClickLister(new EffectResourceAdapter.OnLongClickLister() {
            @Override
            public void onClickStart(int position) {
                //开始计算使用特效
                parseResource(mResourceData.get(position).type, mResourceData.get(position).unzipFolder, position);
                mStartClickTime = System.currentTimeMillis();
                Log.e("Harrison", String.format("position：%2d- 现在时间：%2s", position, mStartClickTime + ""));
            }

            @Override
            public void onClickEnd(int position) {
                //结束使用该特效
                mVideoRenderer.removeDynamic(mDynamicColorFilter.get(position));
                Log.e("Harrison", String.format("position：%2d- 结束用时：%2s", position, (System.currentTimeMillis() - mStartClickTime) + ""));
            }
        });

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
                        mVideoRenderer.changeDynamicColorFilter(mDynamicColorFilter.get(position));
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
                    mVideoRenderer.changeDynamicColorFilter(new DynamicColor().setColorType(ResourceType.FILTER.getIndex()));
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {


        }
    }
}
