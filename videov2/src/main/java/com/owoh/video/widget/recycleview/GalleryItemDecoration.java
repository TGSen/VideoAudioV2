package com.owoh.video.widget.recycleview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class GalleryItemDecoration extends RecyclerView.ItemDecoration {
    /**
     * 自定义默认的Item的边距
     */
    private int mPageMargin = 10;
    /**
     * 第一张图片的左边距
     */
    private int mLeftPageVisibleWidth;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        //计算一下第一中图片距离屏幕左边的距离：(屏幕的宽度-item的宽度)/2。其中item的宽度=实际ImagView的宽度+margin。
        //我这里设置的ImageView的宽度为100+margin=110
        if (mLeftPageVisibleWidth == 0) {
            //计算一次就好了
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
            view.measure(widthMeasureSpec, heightMeasureSpec);
            mLeftPageVisibleWidth = (int) (px2dip(view.getContext(), getScreenWidth(view.getContext()) - view.getMeasuredWidth()) / 2);

        }

        //获取当前Item的position
        int position = parent.getChildAdapterPosition(view);
        //获得Item的数量
        int itemCount = parent.getAdapter().getItemCount();
        int leftMagin;
        if (position == 0) {
            leftMagin = dpToPx(mLeftPageVisibleWidth);
        } else {
            leftMagin = dpToPx(mPageMargin);
        }
        int rightMagin;
        if (position == itemCount - 1) {
            rightMagin = dpToPx(mLeftPageVisibleWidth);
        } else {
            rightMagin = dpToPx(mPageMargin);
        }
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

        //10,10分别是item到上下的margin
        layoutParams.setMargins(leftMagin, 10, rightMagin, 10);
        view.setLayoutParams(layoutParams);

        super.getItemOffsets(outRect, view, parent, state);


    }

    /**
     * d p转换成px
     *
     * @param dp：
     */
    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);

    }

    /**
     * px转dp
     *
     * @param px      px
     * @param context 上下文
     * @return
     */
    public float px2dip(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        float dp = px / density;
        return dp;
    }


    /**
     * 获取屏幕的宽度
     *
     * @param context:
     * @return :
     */
    public static int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

}

