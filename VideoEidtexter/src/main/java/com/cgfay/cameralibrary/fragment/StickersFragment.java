package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;


import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.adapter.StickersAdapter;
import com.cgfay.cameralibrary.bean.ItemSticker;
import com.cgfay.cameralibrary.media.VideoRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StickersFragment extends Fragment {

    private static final String TAG = "PreviewFiltersFragment";
    private StickersAdapter stickersAdapter;

    private List<ItemSticker> mResourceData = new ArrayList<>();

    private VideoRenderer mVideoRenderer;
    private OnStickerPanlListener onStickerAddListener;

    public void setVideoRenderer(VideoRenderer mVideoRenderer) {
        this.mVideoRenderer = mVideoRenderer;
    }


    public static StickersFragment getInstance() {
        Bundle bundle = new Bundle();
        StickersFragment filtersFragment = new StickersFragment();
        filtersFragment.setArguments(bundle);
        return filtersFragment;
    }

    // 内容显示列表
    private View mContentView;

    // 贴纸列表
    private RecyclerView mResourceView;

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
        initStickerData();
    }

    private void initStickerData() {
        stickersAdapter.notifyDataSetChanged();
    }


    private void initView(View view) {
        mResourceView = view.findViewById(R.id.preview_resource_list);
        ImageView imageView = view.findViewById(R.id.imageClose);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onStickerAddListener != null) {
                    onStickerAddListener.onClosePanl();
                }
            }
        });
        GridLayoutManager manager = new GridLayoutManager(mActivity, 5);
        mResourceView.setLayoutManager(manager);
        mResourceData.addAll(ItemSticker.getStickerList());
        stickersAdapter = new StickersAdapter(mActivity, mResourceData);
        mResourceView.setAdapter(stickersAdapter);
        stickersAdapter.setOnItemClickListener(new StickersAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                if (onStickerAddListener != null) {
                    onStickerAddListener.addSticker(mResourceData.get(position));
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

    public void setOnStickerAddListener(OnStickerPanlListener listener) {
        this.onStickerAddListener = listener;
    }

    /**
     * music 切换监听
     */
    public interface OnStickerPanlListener {
        void addSticker(ItemSticker url);

        void onClosePanl();
    }
}
