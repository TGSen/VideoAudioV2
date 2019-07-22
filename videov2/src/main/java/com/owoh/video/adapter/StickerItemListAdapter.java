package com.owoh.video.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.owoh.R;
import com.owoh.video.ItemSticker;
import com.owoh.video.Stickers;
import com.owoh.video.loader.MediaLoader;
import com.owoh.video.loader.impl.GlideMediaLoader;

import java.util.List;

/**
 * 音乐
 */
public class StickerItemListAdapter extends RecyclerView.Adapter<StickerItemListAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private List<ItemSticker.StickersBean> mResourceList;
    private Context mContext;
    private int mSelected;

    private MediaLoader mMediaLoader;

    private OnItemClickListener mListener;

    public StickerItemListAdapter(Context context, List<ItemSticker.StickersBean> itemList) {
        mContext = context;
        mResourceList = itemList;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
        mMediaLoader = new GlideMediaLoader();
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sticker, parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.resourceThumb = (ImageView) view.findViewById(R.id.resource_thumb);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceHolder holder, final int position) {
        ItemSticker.StickersBean resource = mResourceList.get(position);
        if (mMediaLoader != null) {
            mMediaLoader.loadThumbnail(mContext, mPlaceHolder, holder.resourceThumb,
                    Uri.parse(resource.getSmall_image()));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResourceList == null ? 0 : mResourceList.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.mListener = clickListener;
    }

    public class ResourceHolder extends RecyclerView.ViewHolder {

        public ImageView resourceThumb;

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnItemClickListener {
        void onClick(int position);
    }

}
