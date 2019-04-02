package com.cgfay.cameralibrary.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.widget.VideoPreviewView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harrison 唐广森
 * @description: 视频的编辑【循环播放+ic_effects+滤镜+贴纸】，使用GLSurfaceVeiw+MedieoPlayer
 * @date :2019/4/1 14:29
 */
public class EditextVideoActivity extends AppCompatActivity {
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

        mAspectLayout.addView(mVideoPreviewView);
        mAspectLayout.requestLayout();
        // 绑定需要渲染的SurfaceView
        VideoRenderer.getInstance().setSurfaceView(mVideoPreviewView);
        VideoRenderer.getInstance().requestRender();
//        List<String> paths = new ArrayList<>();
//        String path = "/storage/emulated/0/Android/data/com.cgfay.cameralibrary/cache/CainCamera_1554114635517.mp4";
//        paths.add(path);
//        VideoRenderer.getInstance().setVideoPaths(paths);

    }


}
