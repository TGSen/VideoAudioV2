package com.owoh.video.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class RecordProgressBar extends ProgressBar {

    public RecordProgressBar(Context context) {
        this(context, null);
    }

    public RecordProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
