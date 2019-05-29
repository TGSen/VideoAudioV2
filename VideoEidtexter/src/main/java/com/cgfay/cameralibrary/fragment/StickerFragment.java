package com.cgfay.cameralibrary.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.widget.sticker.BitmapStickerIcon;
import com.cgfay.cameralibrary.widget.sticker.DeleteIconEvent;
import com.cgfay.cameralibrary.widget.sticker.DrawableSticker;
import com.cgfay.cameralibrary.widget.sticker.FlipHorizontallyEvent;
import com.cgfay.cameralibrary.widget.sticker.Sticker;
import com.cgfay.cameralibrary.widget.sticker.StickerView;
import com.cgfay.cameralibrary.widget.sticker.TextSticker;
import com.cgfay.cameralibrary.widget.sticker.ZoomIconEvent;
import com.cgfay.cameralibrary.utils.FileUtil;

import java.io.File;
import java.util.Arrays;

/**
 * @author Harrison 唐广森
 * @description: 贴纸
 * @date :2019/5/13 15:56
 */
public class StickerFragment extends Fragment {


    private static final String TAG = "Harrison";
    private View mContentView;
    private StickerView stickerView;

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
        stickerView = view.findViewById(R.id.stickerView);

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

        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon));
        stickerView.setBackgroundColor(Color.TRANSPARENT);
        stickerView.setLocked(false);
        stickerView.setConstrained(true);


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

            @Override
            public void onStickerTouchedOutSide() {

            }
        });

        loadSticker();

    }


    private void loadSticker() {
        Drawable drawable =
                ContextCompat.getDrawable(mActivity, R.mipmap.sticker);

        stickerView.addSticker(new DrawableSticker(drawable));


//        Drawable bubble = ContextCompat.getDrawable(mActivity, R.mipmap.bubble);
//        stickerView.addSticker(
//                new TextSticker(mActivity.getApplicationContext())
//                        .setDrawable(bubble)
//                        .setText("Sticker\n")
//                        .setMaxTextSize(14)
//                        .resizeText()
//                , Sticker.Position.CENTER);

        stickerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                File file = FileUtil.getNewFile(mActivity, "Sticker");
                if (file != null) {
                    stickerView.save(file);
                    if (file.exists()) {
                        Log.e("Harrison", "file" + file.getAbsolutePath());
                    } else {
                        Log.e("Harrison", "file: null");
                    }
                    Toast.makeText(mActivity, "saved in " + file.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity, "the file is null", Toast.LENGTH_SHORT).show();
                }
            }
        }, 25000);

    }


}
