package com.owoh.video.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
import com.cgfay.filterlibrary.utils.BitmapUtils;
import com.owoh.R;
import com.owoh.video.loader.MediaLoader;
import com.owoh.video.loader.impl.GlideMediaLoader;

import java.util.List;

/**
 * 贴纸资源适配器
 */
public class PreviewResourceAdapter extends RecyclerView.Adapter<PreviewResourceAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private List<ResourceData> mResourceList;
    private Context mContext;
    private int mSelected;

    private MediaLoader mMediaLoader;

    private OnResourceChangeListener mListener;

    public PreviewResourceAdapter(Context context, List<ResourceData> itemList) {
        mContext = context;
        mResourceList = itemList;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
        mMediaLoader = new GlideMediaLoader();
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_preview_resource_view,
                parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.imageSeleted = view.findViewById(R.id.imageSeleted);
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

        if (position == mSelected) {
            holder.imageSeleted.setVisibility(View.VISIBLE);
        } else {
            holder.imageSeleted.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected == position) {
                    return;
                }
                int last = mSelected;
                mSelected = position;
                notifyItemChanged(last);
                notifyItemChanged(position);
                if (mListener != null) {
                    mListener.onResourceChanged(mResourceList.get(position), position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mResourceList == null ? 0 : mResourceList.size();
    }

    public class ResourceHolder extends RecyclerView.ViewHolder {

        public LinearLayout resourceRoot;
        public ImageView resourceThumb;
        public ImageView imageSeleted;

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnResourceChangeListener {
        void onResourceChanged(ResourceData resourceData, int currentPosition);
    }

    public void setOnResourceChangeListener(OnResourceChangeListener listener) {
        mListener = listener;
    }

}
