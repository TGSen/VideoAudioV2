package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.MusicAdapter;
import com.cgfay.cameralibrary.adapter.PreviewResourceAdapter;
import com.cgfay.cameralibrary.engine.render.PreviewRenderer;
import com.cgfay.cameralibrary.media.VideoRenderer;
import com.cgfay.cameralibrary.media.bgmusic.ItemMusic;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MusicFragment extends Fragment {

    private static final String TAG = "PreviewFiltersFragment";
    private MusicAdapter musicAdapter;

    private List<ItemMusic> mResourceData = new ArrayList<>();

    private VideoRenderer mVideoRenderer;
    private OnMusicChangeListener onMusicChangeListener;

    public void setVideoRenderer(VideoRenderer mVideoRenderer) {
        this.mVideoRenderer = mVideoRenderer;
    }


    public static MusicFragment getInstance() {
        Bundle bundle = new Bundle();
        MusicFragment filtersFragment = new MusicFragment();
        filtersFragment.setArguments(bundle);
        return filtersFragment;
    }

    // 内容显示列表
    private View mContentView;

    // 贴纸列表
    private RecyclerView mResourceView;

    // 布局管理器
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_preview_resource, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {

        }

        initView(mContentView);
        initMusicData();
    }

    private void initMusicData() {
        ItemMusic noMusic = new ItemMusic();
        noMusic.setName("默认无");
        noMusic.setThumbPath("assets://thumbs/camera/camera_style_3.png");
        mResourceData.add(noMusic);

        ItemMusic itemMusic = new ItemMusic();
        itemMusic.setName("玛丽婚礼Music");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/output_audio.mp3";
        itemMusic.setThumbPath("assets://thumbs/camera/camera_style_3.png");
        itemMusic.setPath(path);
        mResourceData.add(itemMusic);

        ItemMusic itemMusic1 = new ItemMusic();
        itemMusic1.setName("企业音乐");
        itemMusic1.setThumbPath("assets://thumbs/camera/camera_style_3.png");

        String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/test.mp3";
        itemMusic1.setPath(path2);
        mResourceData.add(itemMusic1);

        ItemMusic itemMusic3 = new ItemMusic();
        itemMusic3.setName("AAC音乐");
        itemMusic3.setThumbPath("assets://thumbs/camera/camera_style_3.png");
        String path3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/testmp.mp3";
        itemMusic3.setPath(path3);
        mResourceData.add(itemMusic3);
        musicAdapter.notifyDataSetChanged();
    }


    private void initView(View view) {
        mResourceView = view.findViewById(R.id.preview_resource_list);

        GridLayoutManager manager = new GridLayoutManager(mActivity, 5);
        mResourceView.setLayoutManager(manager);
        musicAdapter = new MusicAdapter(mActivity, mResourceData);
        mResourceView.setAdapter(musicAdapter);
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                //切换音乐
                if (onMusicChangeListener != null) {
                    onMusicChangeListener.change(mResourceData.get(position).getPath());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setOnMusicChangeListener(OnMusicChangeListener listener) {
        this.onMusicChangeListener = listener;
    }

    /**
     * music 切换监听
     */
    public interface OnMusicChangeListener {
        void change(String url);
    }
}
