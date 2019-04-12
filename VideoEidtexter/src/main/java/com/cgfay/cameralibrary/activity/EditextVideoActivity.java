package com.cgfay.cameralibrary.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.fragment.PreviewFiltersFragment;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.media.bean.VideoEffectType;
import com.cgfay.cameralibrary.media.surface.EncodeDecodeSurface;
import com.cgfay.cameralibrary.media.surface.OffSVideoRenderManager;
import com.cgfay.cameralibrary.media.surface.OffScreenVideoRenderer;
import com.cgfay.cameralibrary.widget.VideoPreviewView;

/**
 * @author Harrison 唐广森
 * @description: 视频的编辑【循环播放+ic_effects+滤镜+贴纸】，使用GLSurfaceVeiw+MedieoPlayer
 * @date :2019/4/1 14:29
 */
public class EditextVideoActivity extends AppCompatActivity implements View.OnClickListener {
    // 显示滤镜页面
    private boolean isShowingFilters = false;
    private static final String KEY_VIDEO_PATH = "videoPath";
    private static final String BUNDLE_VIDEO_PATH = "bundle";
    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    private TextView btFilters;
    private TextView btEffect;
    private String videoPath;
    private VideoPreviewView mVideoPreviewView;
    private VideoRenderer mVideoRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_video);
        mVideoRenderer = new VideoRenderer();
        VideoEffectType mVideoEffectType = new VideoEffectType()
                .setCurrentEffectType(VideoEffectType.EFFECT_TYPE_SINGLE)
                .setCurrentRendererType(VideoEffectType.RENDER_TYPE_CONTINUED);
        mVideoRenderer.initRenderer(this.getApplicationContext(),mVideoEffectType);

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
      //  OffSVideoRenderManager.getInstance().init(this.getApplicationContext());
        try {
            test.testEncodeDecodeSurface();
        } catch (Throwable a) {
            a.printStackTrace();
        }
    }

    public static void gotoThis(Context context, String path) {
        Intent intent = new Intent(context, EditextVideoActivity.class);
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
        //mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        mVideoPreviewView = new VideoPreviewView(this);
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFilterView();
            }
        });
        mAspectLayout.addView(mVideoPreviewView);
        mAspectLayout.requestLayout();

        btFilters = findViewById(R.id.btFilters);
        btFilters.setOnClickListener(this);

        btEffect = findViewById(R.id.btEffect);
        btEffect.setOnClickListener(this);
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
        ft.commit();
        //  hideToolsLayout();
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

        hideFilterView();
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
            case R.id.btEffect:
                EffectVideoActivity.gotoThis(EditextVideoActivity.this, videoPath);
                break;
            case R.id.btFilters:
                showFilterView();
                break;
        }
    }
}
