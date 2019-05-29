package com.cgfay.cameralibrary.adapter;

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
import android.widget.TextView;

import com.cgfay.cameralibrary.R;
import com.cgfay.cameralibrary.bean.ItemSticker;
import com.cgfay.cameralibrary.loader.MediaLoader;
import com.cgfay.cameralibrary.loader.impl.GlideMediaLoader;
import com.cgfay.cameralibrary.media.bgmusic.ItemMusic;
import com.cgfay.utilslibrary.utils.BitmapUtils;

import java.util.List;

/**
 * 音乐
 */
public class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private List<ItemSticker> mResourceList;
    private Context mContext;
    private int mSelected;

    private MediaLoader mMediaLoader;

    private OnItemClickListener mListener;

    public StickersAdapter(Context context, List<ItemSticker> itemList) {
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
        ItemSticker resource = mResourceList.get(position);

        if (mMediaLoader != null) {
            mMediaLoader.loadImage(mContext, mPlaceHolder, holder.resourceThumb,resource.getPath());
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

        public TextView name;
        public ImageView resourceThumb;

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnItemClickListener {
        void onClick(int position);
    }

}
