package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.cameralibrary.R;

/**
 * @author Harrison 唐广森
 * @description: 贴纸
 * @date :2019/5/13 15:56
 */
public class StickerFragment extends Fragment {


    private View mContentView;

    public static StickerFragment getInstance() {
        Bundle bundle = new Bundle();
        StickerFragment filtersFragment = new StickerFragment();
        filtersFragment.setArguments(bundle);
        return filtersFragment;
    }

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



}
