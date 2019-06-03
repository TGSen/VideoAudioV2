package com.cgfay.cameralibrary.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.loader.MediaLoader;
import com.cgfay.cameralibrary.loader.impl.GlideMediaLoader;
import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.utils.BitmapUtils;

import java.util.List;

/**
 * 贴纸资源适配器
 */
public class EffectResourceAdapter extends RecyclerView.Adapter<EffectResourceAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private List<ResourceData> mResourceList;
    private Context mContext;
    private int mSelected;

    private MediaLoader mMediaLoader;

    private OnResourceChangeListener mListener;
    private OnLongClickLister mOnLongClickLister;

    public EffectResourceAdapter(Context context, List<ResourceData> itemList) {
        mContext = context;
        mResourceList = itemList;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
        mMediaLoader = new GlideMediaLoader();
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_preview_resource_view_v2,
                parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.resourceRoot = view.findViewById(R.id.resource_root);
        holder.resourceThumb = (ImageView) view.findViewById(R.id.resource_thumb);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceHolder holder, final int position) {
        ResourceData resource = mResourceList.get(position);
        // 如果是asset下面的，则直接解码
        if (!TextUtils.isEmpty(resource.thumbPath) && resource.thumbPath.startsWith("assets://")) {
            holder.resourceThumb.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext,
                    resource.thumbPath.substring("assets://".length())));
        } else {
            if (mMediaLoader != null) {
                mMediaLoader.loadThumbnail(mContext, mPlaceHolder, holder.resourceThumb,
                        Uri.parse(resource.thumbPath));
            }
        }
        final int currentPosition = position;
//        holder.resourceRoot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mSelected == currentPosition) {
//                    return;
//                }
//                int last = mSelected;
//                mSelected = currentPosition;
//                notifyItemChanged(last);
//                notifyItemChanged(currentPosition);
//                if (mListener != null) {
//                    mListener.onResourceChanged(mResourceList.get(currentPosition));
//                }
//            }
//        });
        //设置ItemView 的开始点击和抬起的手势，提供给外部长按的时间统计
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mOnLongClickLister == null) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mOnLongClickLister.onClickStart(position);
                        break;
                    case MotionEvent.ACTION_UP:
                        mOnLongClickLister.onClickEnd(position);
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mResourceList == null ? 0 : mResourceList.size();
    }

    public class ResourceHolder extends RecyclerView.ViewHolder {

        public FrameLayout resourceRoot;
        public ImageView resourceThumb;

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnResourceChangeListener {
        void onResourceChanged(ResourceData resourceData);
    }

    public interface OnLongClickLister {
        void onClickStart(int position);

        void onClickEnd(int position);
    }

    public void setOnResourceChangeListener(OnResourceChangeListener listener) {
        mListener = listener;
    }

    public void setOnLongClickLister(OnLongClickLister listener) {
        mOnLongClickLister = listener;
    }

}
