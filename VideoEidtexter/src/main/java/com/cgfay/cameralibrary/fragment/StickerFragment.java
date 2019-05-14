package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.sticker.BitmapStickerIcon;
import com.cgfay.cameralibrary.sticker.DeleteIconEvent;
import com.cgfay.cameralibrary.sticker.FlipHorizontallyEvent;
import com.cgfay.cameralibrary.sticker.Sticker;
import com.cgfay.cameralibrary.sticker.StickerView;
import com.cgfay.cameralibrary.sticker.TextSticker;
import com.cgfay.cameralibrary.sticker.ZoomIconEvent;

import java.util.Arrays;

/**
 * @author Harrison 唐广森
 * @description: 贴纸
 * @date :2019/5/13 15:56
 */
public class StickerFragment extends Fragment {


    private static final String TAG = "Harrison";
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
        mContentView = inflater.inflate(R.layout.fragment_sticker_layout, container, false);
        return mContentView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {

        }

        initView(mContentView);
        initData();
    }

    private void initData() {

    }

    private void initView(View view) {
        final StickerView stickerView = view.findViewById(R.id.stickerView);

        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(mActivity,
                R.mipmap.sticker_ic_close_white_18dp),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());

        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(mActivity,
                R.mipmap.sticker_ic_scale_white_18dp),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());

        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(mActivity,
                R.mipmap.sticker_ic_flip_white_18dp),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(ContextCompat.getDrawable(mActivity, R.mipmap.sticker),
                        BitmapStickerIcon.LEFT_BOTTOM);
//        heartIcon.setIconEvent(new HelloIconEvent());

        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon, heartIcon));

        //default icon layout
        //stickerView.configDefaultIcons();

        stickerView.setBackgroundColor(Color.WHITE);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);

//        sticker = new TextSticker(this);
//
//        sticker.setDrawable(ContextCompat.getDrawable(getApplicationContext(),
//                R.drawable.sticker_transparent_background));
//        sticker.setText("Hello, world!");
//        sticker.setTextColor(Color.BLACK);
//        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
//        sticker.resizeText();

        stickerView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            @Override
            public void onStickerAdded(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerAdded");
            }

            @Override
            public void onStickerClicked(@NonNull Sticker sticker) {
                //stickerView.removeAllSticker();
                if (sticker instanceof TextSticker) {
                    ((TextSticker) sticker).setTextColor(Color.RED);
                    stickerView.replace(sticker);
                    stickerView.invalidate();
                }
                Log.d(TAG, "onStickerClicked");
            }

            @Override
            public void onStickerDeleted(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerDeleted");
            }

            @Override
            public void onStickerDragFinished(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerDragFinished");
            }

            @Override
            public void onStickerTouchedDown(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerTouchedDown");
            }

            @Override
            public void onStickerZoomFinished(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerZoomFinished");
            }

            @Override
            public void onStickerFlipped(@NonNull Sticker sticker) {
                Log.d(TAG, "onStickerFlipped");
            }

            @Override
            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                Log.d(TAG, "onDoubleTapped: double tap will be with two click");
            }
        });

    }
    
    


}
