package com.owoh.video

import android.os.Environment
import java.util.*

class ItemSticker {
    val TYPE_GIF = 1
    val TYPE_PNG = 0
    var name: String? = null
    var path: String? = null
    var type: Int = TYPE_PNG;

    companion object {

        val stickerList: List<ItemSticker>
            get() {
                val path = Environment.getExternalStorageDirectory().absolutePath + "/Download/"
                val list = ArrayList<ItemSticker>()
                val itemSticker = ItemSticker()
                itemSticker.name = "sticker"
                itemSticker.path = path + "test1.jpg"
                list.add(itemSticker)

                val itemSticker1 = ItemSticker()
                itemSticker1.name = "sticker1"
                itemSticker1.path = path + "test1.jpg"
                list.add(itemSticker1)

                val itemSticker2 = ItemSticker()
                itemSticker2.name = "sticker2"
                itemSticker2.path = path + "test1.jpg"
                list.add(itemSticker2)

                val itemSticker3 = ItemSticker()
                itemSticker3.name = "sticker3"
                itemSticker3.path = path + "test2.gif"
                itemSticker3.type = itemSticker3.TYPE_GIF
                list.add(itemSticker3)

                val itemSticker4 = ItemSticker()
                itemSticker4.name = "sticker4"
                itemSticker4.path = path + "test3.gif"
                itemSticker4.type = itemSticker4.TYPE_GIF
                list.add(itemSticker4)
                return list
            }
    }
}
