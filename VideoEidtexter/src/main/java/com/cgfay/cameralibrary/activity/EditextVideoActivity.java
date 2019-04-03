package com.cgfay.cameralibrary.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.fragment.PreviewFiltersFragment;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.widget.VideoPreviewView;

/**
 * @author Harrison 唐广森
 * @description: 视频的编辑【循环播放+ic_effects+滤镜+贴纸】，使用GLSurfaceVeiw+MedieoPlayer
 * @date :2019/4/1 14:29
 */
public class EditextVideoActivity extends AppCompatActivity implements View.OnClickListener {
    // 显示滤镜页面
    private boolean isShowingFilters = false;
    // 滤镜页面
    private PreviewFiltersFragment mColorFilterFragment;
    private TextView btFilters;
    private TextView btEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_editext_video);
        VideoRenderer.getInstance().initRenderer(this.getApplicationContext());
        initView();
    }

    /**
     * 初始化页面
     *
     * @param
     */
    private void initView() {
        FrameLayout mAspectLayout = findViewById(R.id.layout_aspect);
        //mAspectLayout.setAspectRatio(mCameraParam.currentRatio);
        VideoPreviewView mVideoPreviewView = new VideoPreviewView(this);
        mVideoPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFilterView();
            }
        });
        mAspectLayout.addView(mVideoPreviewView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        VideoRenderer.getInstance().setSurfaceView(mVideoPreviewView);
        btFilters = findViewById(R.id.btFilters);
        btFilters.setOnClickListener(this);

        btEffect = findViewById(R.id.btEffect);
        btEffect.setOnClickListener(this);
//        List<String> paths = new ArrayList<>();
//        String path = "/storage/emulated/0/Android/data/com.cgfay.cameralibrary/cache/CainCamera_1554114635517.mp4";
//        paths.add(path);
//        VideoGLRenderer.getInstance().setVideoPaths(paths);

    }

    /**
     * 显示滤镜页面
     */
    private void showFilterView() {
        isShowingFilters = true;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mColorFilterFragment == null) {
            mColorFilterFragment = PreviewFiltersFragment.getInstance(PreviewFiltersFragment.TYPE_COLOR_FILTER, PreviewFiltersFragment.TYPE_VIDEO_EIDTEXT);
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
        VideoRenderer.getInstance().stopPlayVideo();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideFilterView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoRenderer.getInstance().startPlayVideo();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btEffect:
                break;
            case R.id.btFilters:
                showFilterView();
                break;
        }
    }
}
