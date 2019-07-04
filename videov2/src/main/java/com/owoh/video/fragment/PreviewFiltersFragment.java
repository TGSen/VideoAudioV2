package com.owoh.video.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.owoh.R;
import com.owoh.video.adapter.PreviewResourceAdapter;
import com.owoh.video.engine.render.PreviewRenderer;
import com.owoh.video.media.VideoRenderer;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColorData;
import com.cgfay.filterlibrary.glfilter.resource.ResourceHelper;
import com.cgfay.filterlibrary.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceType;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 分镜页面和滤镜的页面
 */
public class PreviewFiltersFragment extends Fragment {

    private static final String TAG = "PreviewFiltersFragment";
    private static final String KEY_FILTER_TYPE = "filterType";
    private static final String KEY_VIDEO_MODE_TYPE = "videoModeType";
    private PreviewResourceAdapter mPreviewResourceAdapter;

    private List<ResourceData> mResourceData = new ArrayList<>();
    private int mCurrentPosition = -1;

    //数据模式， 分镜
    public static final int TYPE_CAMERA_FILTER = 0;
    //颜色方面的滤镜
    public static final int TYPE_COLOR_FILTER = 1;

    //拍摄
    public static final int TYPE_VIDEO_SHOT = 0;
    //编辑
    public static final int TYPE_VIDEO_EIDTEXT = 1;
    //滤镜分类
    private int mFilterType;
    //视频的模式，1拍摄模式 2.编辑模式
    private int mVideoModeType;
    private VideoRenderer mVideoRenderer;

    public void setVideoRenderer(VideoRenderer mVideoRenderer) {
        this.mVideoRenderer = mVideoRenderer;
    }

    //Retention 是元注解，简单地讲就是系统提供的，用于定义注解的“注解”
    @Retention(RetentionPolicy.SOURCE)
    //这里指定int的取值只能是以下范围
    @IntDef({TYPE_CAMERA_FILTER, TYPE_COLOR_FILTER})
    @interface FilterDef {
    }

    @Retention(RetentionPolicy.SOURCE)
    //这里指定int的取值只能是以下范围
    @IntDef({TYPE_VIDEO_SHOT, TYPE_VIDEO_EIDTEXT})
    @interface VideoModeDef {
    }


    public static PreviewFiltersFragment getInstance(@FilterDef int filterType, @VideoModeDef int videoModeType) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_FILTER_TYPE, filterType);
        bundle.putInt(KEY_VIDEO_MODE_TYPE, videoModeType);
        PreviewFiltersFragment filtersFragment = new PreviewFiltersFragment();
        filtersFragment.setArguments(bundle);
        return filtersFragment;
    }

    // 内容显示列表
    private View mContentView;

    // 贴纸列表
    private RecyclerView mResourceView;

    // 布局管理器
    private Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_preview_resource, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mFilterType = bundle.getInt(KEY_FILTER_TYPE);
            mVideoModeType = bundle.getInt(KEY_VIDEO_MODE_TYPE);
        }

        initView(mContentView);
        initFilterData();
    }

    //初始化分镜或者是滤镜资源
    private void initFilterData() {
        if (mFilterType == TYPE_CAMERA_FILTER) {
            ResourceHelper.initCameraFilterResource(mActivity, mResourceData);
        } else if (mFilterType == TYPE_COLOR_FILTER) {
            ResourceHelper.initColorFilterResource(mActivity, mResourceData);
        }
//        mResourceData.addAll( mFilterType == TYPE_COLOR_FILTER ? ResourceHelper.getColorFilter() : ResourceHelper.getCamerFilter());
        mPreviewResourceAdapter.notifyDataSetChanged();
    }


    private void initView(View view) {
        mResourceView = view.findViewById(R.id.preview_resource_list);

        GridLayoutManager manager = new GridLayoutManager(mActivity, 5);
        mResourceView.setLayoutManager(manager);
        mPreviewResourceAdapter = new PreviewResourceAdapter(mActivity, mResourceData);
        mResourceView.setAdapter(mPreviewResourceAdapter);
        mPreviewResourceAdapter.setOnResourceChangeListener(new PreviewResourceAdapter.OnResourceChangeListener() {
            @Override
            public void onResourceChanged(ResourceData resourceData, int currentPosition) {
                mCurrentPosition = currentPosition;
                parseResource(resourceData.type, resourceData.unzipFolder);
            }
        });
    }

    @Override
    public void onDestroyView() {
        mContentView = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    public DynamicColorData getCurrentColorFilter() {
        if (mResourceData != null && (mCurrentPosition >= 0 && mCurrentPosition < mResourceData.size())) {
            String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + mResourceData.get(mCurrentPosition).unzipFolder;
            try {
                DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                DynamicColorData colorData = color.filterList.get(0);
                colorData.setVsPath(folderPath + colorData.vertexShader);
                colorData.setFsPath(folderPath + File.separator + colorData.fragmentShader);

                return color.filterList.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 解码资源
     *
     * @param type        资源类型
     * @param unzipFolder 资源所在文件夹
     */
    private void parseResource(@Nullable ResourceType type, String unzipFolder) {
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                // 单纯的滤镜
                case FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    color.setColorType(ResourceType.FILTER.getIndex());
                    Log.e("Harrison", "FILTER");
                    //滤镜分拍摄模式还是编辑模式
                    if (mVideoModeType == TYPE_VIDEO_EIDTEXT) {
                        Log.e("Harrison", "TYPE_VIDEO_EIDTEXT");
                        if (mVideoRenderer != null)
                            mVideoRenderer.changeDynamicColorFilter(color);
                    } else if (mVideoModeType == TYPE_VIDEO_SHOT) {
                        Log.e("Harrison", "TYPE_VIDEO_SHOT");
                        PreviewRenderer.getInstance().changeDynamicColorFilter(color);
                    }

                    break;
                }// 摄像机分镜
                case CAMERA_FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    color.setColorType(ResourceType.CAMERA_FILTER.getIndex());
                    PreviewRenderer.getInstance().changeDynamicCameraFilter(color);
                    break;
                }

                // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    PreviewRenderer.getInstance().changeDynamicResource(sticker);
                    break;
                }

                // TODO 多种结果混合
                case MULTI: {
                    break;
                }

                // 所有数据均为空
                case NONE: {
                    //滤镜分拍摄模式还是编辑模式
                    if (mVideoModeType == TYPE_VIDEO_EIDTEXT) {
                        if (mVideoRenderer != null)
                            mVideoRenderer.changeDynamicColorFilter(new DynamicColor().setColorType(ResourceType.FILTER.getIndex()));
                    } else if (mVideoModeType == TYPE_VIDEO_SHOT) {
                        if (mFilterType == TYPE_COLOR_FILTER) {
                            PreviewRenderer.getInstance().removeDynamic(new DynamicColor().setColorType(ResourceType.FILTER.getIndex()));
                        } else {
                            //移除分镜的
                            PreviewRenderer.getInstance().removeDynamic(new DynamicColor().setColorType(ResourceType.CAMERA_FILTER.getIndex()));
                        }
                    }

                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
