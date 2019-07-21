package com.owoh.video.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.DrawableCompat

class DrawableUtil{
    companion object {
        //设置图片
        fun tintDrawable(drawable: Drawable, colors: ColorStateList): Drawable {
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTintList(wrappedDrawable, colors)
            return wrappedDrawable
        }

        //设置图片
        fun tintDrawable(drawable: Drawable, colors: String) {
            drawable.apply {
                val wrappedDrawable = DrawableCompat.wrap(drawable)
                var color = Color.parseColor(colors)
                DrawableCompat.setTint(wrappedDrawable, color)
            }

        }
    }
}