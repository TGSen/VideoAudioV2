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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.media.VideoRenderThread;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.utils.ImageBlur;
import com.cgfay.cameralibrary.widget.VideoPreviewView;
import com.cgfay.utilslibrary.utils.BitmapUtils;

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

    public static Executor EXECUTOR = Executors.newCachedThreadPool();

    private String videoPath;
    private VideoPreviewView mVideoPreviewView;
    private VideoRenderer mVideoRenderer;
    // 设置video paths
    public static final int MSG_VIDEO_PLAY_PROGRESS = 0x001;

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_VIDEO_PLAY_PROGRESS:
                 int progress =  mVideoRenderer.getVideoProgress();
                 mSeekBar.setProgress(progress);
                 mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
                    break;
            }
        }
    };
    private View mRootView;
    private RecyclerView mRecyclerView;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_effect_video);
        mVideoRenderer = new VideoRenderer();
        mVideoRenderer.initRenderer(this.getApplicationContext());

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
                final Bitmap bitmap = BitmapUtils.createVideoThumbnail(videoPath);
                ImageBlur.blurBitmap(bitmap, 10);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.setBackground(new BitmapDrawable(getResources(), bitmap));
                    }
                });
            }
        });

        //设置videoPlayer 状态监听
        mVideoRenderer.setVideoPlayerStatusChangeLisenter(new VideoRenderThread.VideoPlayerStatusChangeLisenter() {
            @Override
            public void videoStart(int totalTime) {
                mSeekBar.setMax(totalTime);
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
            }

            @Override
            public void videoStop() {
                mHandler.removeMessages(MSG_VIDEO_PLAY_PROGRESS);
            }

            @Override
            public void videoRestart() {
                mHandler.sendEmptyMessage(MSG_VIDEO_PLAY_PROGRESS);
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
        mSeekBar.setEnabled(false);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

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
