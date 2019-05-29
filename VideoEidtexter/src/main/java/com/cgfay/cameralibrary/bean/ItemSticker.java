package com.cgfay.cameralibrary.bean;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ItemSticker {
    private String name;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static List<ItemSticker> getStickerList() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        List<ItemSticker> list = new ArrayList<>();
        ItemSticker itemSticker = new ItemSticker();
        itemSticker.setName("sticker");
        itemSticker.setPath(path+"sticker_02.png");
        list.add(itemSticker);

        ItemSticker itemSticker1 = new ItemSticker();
        itemSticker1.setName("sticker1");
        itemSticker1.setPath(path+"sticker_05.png");
        list.add(itemSticker1);

        ItemSticker itemSticker2 = new ItemSticker();
        itemSticker2.setName("sticker2");
        itemSticker2.setPath(path+"sticker_13.png");
        list.add(itemSticker2);


        ItemSticker itemSticker3 = new ItemSticker();
        itemSticker3.setName("sticker3");
        itemSticker3.setPath(path+"sticker_19.png");
        list.add(itemSticker3);

        ItemSticker itemSticker4 = new ItemSticker();
        itemSticker4.setName("sticker4");
        itemSticker4.setPath(path+"sticker_21.png");
        list.add(itemSticker4);


        return list;
    }
}
