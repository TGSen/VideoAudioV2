package com.owoh.video.adapter

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.ColorItem
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.owoh.R
import com.owoh.video.widget.ColorSeletedView

/**
 * Created by Harrison
 */
class ColorAdapter(
        private val context: Context,
        val colorData: ArrayList<ColorItem>?
) : RecyclerView.Adapter<ColorAdapter.Vh>() {

    /* 取消内存缓存和硬盘缓存 TODO 需要整合到框架中 */
    val mRequestOptions = RequestOptions
            .centerInsideTransform()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

    private var currentSeleted = -1
    private var listener: OnColorSeletedLinstener? = null

    interface OnColorSeletedLinstener {
        fun onSeleted(color: String,positon:Int);
    }

    fun setOnColorSeletedLinstener(listener: OnColorSeletedLinstener) {
        this.listener = listener;
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Vh {
        return Vh(LayoutInflater.from(context).inflate(R.layout.item_color, viewGroup, false))
    }

    override fun onBindViewHolder(vh: Vh, i: Int) {
        vh.bindData(i)
    }

    override fun getItemCount(): Int {
        colorData?.let {
            return colorData.size
        }
        return 0;
    }

    fun getSeletedColor(): String? {
        if(currentSeleted>=0 ){
            return colorData?.get(currentSeleted)?.color
        }
        return null
    }

    inner class Vh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView = itemView.findViewById<ColorSeletedView>(R.id.colorView)!!

        fun bindData(position: Int) {
            var item = colorData?.get(position)
            item?.let {
                //设置背景颜色还是背景图片
                if (it.bgImageEnable) {
                    colorView.background = ContextCompat.getDrawable(context, it.bgImage)
                } else {
                    colorView.setColor(it.color)
                }
                if(item.isSeleted) currentSeleted = position
                colorView.setSeleted(item.isSeleted)
                itemView.setOnClickListener {
                    if (currentSeleted != position && currentSeleted >= 0 && currentSeleted < colorData!!.size) {
                        colorData[currentSeleted].isSeleted = false
                        notifyItemChanged(currentSeleted)
                    }
                    colorData?.get(position)?.isSeleted = !(colorData?.get(position)?.isSeleted)!!
                    notifyItemChanged(position)
                    currentSeleted = position
                    listener?.let {
                        listener?.onSeleted(item.color,position)
                    }
                }
            }

        }
    }
}
