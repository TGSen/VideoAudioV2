package com.owoh.video.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.owoh.R;

/**
 * @author Harrison 唐广森
 * @description:
 * @date :2019/4/19 14:29
 */
public class VoiceAdjustFragment extends Fragment {
    private Activity mActivity;
    private View mContentView;

    public static VoiceAdjustFragment getInstance() {
        return new VoiceAdjustFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_layout_voice_adjust, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(mContentView);
    }

    private void initView(View mContentView) {
        SeekBar mOrigiSeekBar = mContentView.findViewById(R.id.seekBarVoice);
        SeekBar seekBarBGM = mContentView.findViewById(R.id.seekBarBGM);
        mOrigiSeekBar.setMax(100);
        seekBarBGM.setMax(100);
        mOrigiSeekBar.setProgress(50);
        seekBarBGM.setProgress(50);
        seekBarBGM.setEnabled(false);


        mOrigiSeekBar.setOnSeekBarChangeListener(new MSeekBarchangeListener(MSeekBarchangeListener.SEEKBAR_ORIGI));
        seekBarBGM.setOnSeekBarChangeListener(new MSeekBarchangeListener(MSeekBarchangeListener.SEEKBAR_BGM));
    }

    private class MSeekBarchangeListener implements SeekBar.OnSeekBarChangeListener {
        public static final int SEEKBAR_ORIGI = 0x001;
        public static final int SEEKBAR_BGM = 0x002;
        private int currentSeekBar;

        public MSeekBarchangeListener(int seekBarType) {
            this.currentSeekBar = seekBarType;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mVoiceChangeListener == null) return;
            if (currentSeekBar == SEEKBAR_BGM) {
                mVoiceChangeListener.bgmVoiceChange((float) progress / (float) seekBar.getMax());
            } else if (currentSeekBar == SEEKBAR_ORIGI) {
                mVoiceChangeListener.origiVoiceChange((float) progress / (float) seekBar.getMax());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private OnVoiceSeekBarChangeListener mVoiceChangeListener;

    public void setOnVoiceSeekBarChangeListener(OnVoiceSeekBarChangeListener listener) {
        this.mVoiceChangeListener = listener;
    }

    /**
     * 声音的控制器改变
     */
    public interface OnVoiceSeekBarChangeListener {
        void origiVoiceChange(float progress);

        void bgmVoiceChange(float progres);
    }


}
