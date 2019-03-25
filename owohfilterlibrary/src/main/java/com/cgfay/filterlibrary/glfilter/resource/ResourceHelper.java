package com.cgfay.filterlibrary.glfilter.resource;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.utilslibrary.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源数据助手
 */
public final class ResourceHelper extends ResourceBaseHelper {

    // 资源存储路径
    private static final String ResourceDirectory = "Resource";
    // 资源列表
    private static final List<ResourceData> mResourceList = new ArrayList<>();

    private ResourceHelper() {

    }

    /**
     * 获取资源列表
     * @return
     */
    public static List<ResourceData> getResourceList() {
        return mResourceList;
    }

    /**
     * 初始化Assets目录下的资源
     * @param context
     */
    public static void initAssetsResource(Context context) {
        FileUtils.createNoMediaFile(getResourceDirectory(context));
        // 清空之前的数据
        mResourceList.clear();

        mResourceList.add(new ResourceData("none", "assets://resource/none.zip", ResourceType.NONE, "none", "assets://thumbs/camera/camera_style_normal.png"));
        mResourceList.add(new ResourceData("camera_style_mirror", "assets://camera/camera_style_mirror.zip", ResourceType.FILTER, "camera_style_mirror", "assets://thumbs/camera/camera_style_mirror.png"));
        mResourceList.add(new ResourceData("camera_style_3", "assets://camera/camera_style_3.zip", ResourceType.FILTER, "camera_style_3", "assets://thumbs/camera/camera_style_3.png"));
        mResourceList.add(new ResourceData("camera_style_4", "assets://camera/camera_style_4.zip", ResourceType.FILTER, "camera_style_4", "assets://thumbs/camera/camera_style_4.png"));
        mResourceList.add(new ResourceData("camera_style_5", "assets://camera/camera_style_5.zip", ResourceType.FILTER, "camera_style_5", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("cartoon", "assets://filters/cartoon.zip", ResourceType.FILTER, "cartoon", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("mosaic", "assets://filters/mosaic.zip", ResourceType.FILTER, "mosaic", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("reversed_color", "assets://filters/reversed_color.zip", ResourceType.FILTER, "reversed_color", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("grayscale", "assets://filters/grayscale.zip", ResourceType.FILTER, "grayscale", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("cartoon_v2", "assets://filters/cartoon_v2.zip", ResourceType.FILTER, "cartoon_v2", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("cool_color", "assets://filters/cool_color.zip", ResourceType.FILTER, "cool_color", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("warm_color", "assets://filters/warm_color.zip", ResourceType.FILTER, "warm_color", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("old_effect", "assets://filters/old_effect.zip", ResourceType.FILTER, "old_effect", "assets://thumbs/camera/camera_style_5.png"));
        mResourceList.add(new ResourceData("flash_green", "assets://effects/flash_green.zip", ResourceType.FILTER, "flash_green", "assets://thumbs/camera/camera_style_5.png"));
        // 解压所有资源
        decompressResource(context, mResourceList);
    }

    /**
     * 解压所有资源
     * @param context
     * @param resourceList 资源列表
     */
    public static void decompressResource(Context context, List<ResourceData> resourceList) {
        // 检查路径是否存在
        boolean result = checkResourceDirectory(context);
        // 存放资源路径无法创建，则直接返回
        if (!result) {
            return;
        }
        String resourcePath = getResourceDirectory(context);
        // 解码列表中的所有资源
        for (ResourceData item : resourceList) {
            if (item.type.getIndex() >= 0) {
                if (item.zipPath.startsWith("assets://")) {
                    decompressAsset(context, item.zipPath.substring("assets://".length()), item.unzipFolder, resourcePath);
                } else if (item.zipPath.startsWith("file://")) {    // 绝对目录中的资源
                    decompressFile(item.zipPath.substring("file://".length()), item.unzipFolder, resourcePath);
                }
            }
        }
    }

    /**
     * 检查资源路径是否存在
     * @param context
     */
    private static boolean checkResourceDirectory(Context context) {
        String resourcePath = getResourceDirectory(context);
        File file = new File(resourcePath);
        if (file.exists()) {
            return file.isDirectory();
        }
        return file.mkdirs();
    }

    /**
     * 获取资源路径
     * @param context
     * @return
     */
    public static String getResourceDirectory(Context context) {
        String resourcePath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            resourcePath = context.getExternalFilesDir(ResourceDirectory).getAbsolutePath();
        } else { // 使用内部存储
            resourcePath = context.getFilesDir() + File.separator + ResourceDirectory;
        }
        return resourcePath;
    }



    /**
     * 删除某个资源
     * @param context
     * @param resource  资源对象
     * @return          删除操作结果
     */
    public static boolean deleteResource(Context context, ResourceData resource) {
        if (resource == null || TextUtils.isEmpty(resource.unzipFolder)) {
            return false;
        }
        boolean result = checkResourceDirectory(context);
        if (!result) {
            return false;
        }
        // 获取资源解压的文件夹路径
        String resourceFolder = getResourceDirectory(context) + File.separator + resource.unzipFolder;
        File file = new File(resourceFolder);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        return FileUtils.deleteDir(file);
    }
}
