package com.cgfay.filterlibrary.glfilter.color.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜数据
 */
public class DynamicColor {

    // 滤镜解压的文件夹路径
    public String unzipPath;
    private int colorType;


    public int getColorType() {
        return colorType;
    }

    public DynamicColor setColorType(int colorType) {
        this.colorType = colorType;
        return this;
    }

//    private List<ItemTime> itemTimes = new ArrayList<>();
//
//    /**
//     * 判断是否已经设置时间的特效
//     *
//     * @return
//     */
//    public boolean getIsAddTimeEffect() {
//        if (itemTimes != null && itemTimes.size() > 0) {
//            return true;
//        }
//        return false;
//    }
//
//    public void addItemTimes(ItemTime itemTime) {
//        this.itemTimes.add(itemTime);
//    }
//
//    public void setEndTime(int videoProgress) {
//        //设置结束时间
//        itemTimes.get(itemTimes.size() - 1).setEndTime(videoProgress);
//    }

    //分段时间有特效
    public static class ItemTime {
        private int startTime;
        private int endTime;

        public int getStartTime() {
            return startTime;
        }

        public ItemTime setStartTime(int startTime) {
            this.startTime = startTime;
            return this;
        }

        public int getEndTime() {
            return endTime;
        }

        public ItemTime setEndTime(int endTime) {
            this.endTime = endTime;
            return this;
        }
    }

    // 滤镜列表
    public List<DynamicColorData> filterList;

    public DynamicColor() {
        filterList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("unzipPath: ");
        builder.append(unzipPath);
        builder.append("\n");

        builder.append("data: [");
        for (int i = 0; i < filterList.size(); i++) {
            builder.append(filterList.get(i).toString());
            if (i < filterList.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        return builder.toString();
    }

}
