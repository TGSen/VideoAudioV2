package com.cgfay.filterlibrary.glfilter.color.bean;

/**
 * 颜色滤镜数据
 */
public class DynamicColorData extends DynamicColorBaseData {
    private String vsPath;
    private String fsPath;


    public String getVsPath() {
        return vsPath;
    }

    public DynamicColorData setVsPath(String vsPath) {
        this.vsPath = vsPath;
        return this;
    }

    public String getFsPath() {
        return fsPath;
    }

    public DynamicColorData setFsPath(String fsPath) {
        this.fsPath = fsPath;
        return this;

    }
}
