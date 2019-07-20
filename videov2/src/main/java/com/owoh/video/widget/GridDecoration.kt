package com.owoh.video.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.blankj.utilcode.util.ConvertUtils

class GridDecoration(private val space: Int, private val column: Int) : RecyclerView.ItemDecoration() {
    private val mSpace: Int

    init {
        this.mSpace = ConvertUtils.dp2px(space.toFloat())
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        // 每个span分配的间隔大小
        val spanSpace = mSpace * (column + 1) / column
        // 列索引
        val colIndex = position % column
        // 列左、右间隙
        outRect.left = mSpace * (colIndex + 1) - spanSpace * colIndex
        outRect.right = spanSpace * (colIndex + 1) - mSpace * (colIndex + 1)
        // 行间距
        if (position >= column) {
            outRect.top = mSpace
        }
    }
}