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
import com.cgfay.cameralibrary.loader.MediaLoader;
import com.cgfay.cameralibrary.loader.impl.GlideMediaLoader;
import com.cgfay.cameralibrary.media.bgmusic.ItemMusic;
import com.cgfay.filterlibrary.utils.BitmapUtils;

import java.util.List;

/**
 * 音乐
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ResourceHolder> {

    private Drawable mPlaceHolder;
    private List<ItemMusic> mResourceList;
    private Context mContext;
    private int mSelected;

    private MediaLoader mMediaLoader;

    private OnItemClickListener mListener;

    public MusicAdapter(Context context, List<ItemMusic> itemList) {
        mContext = context;
        mResourceList = itemList;
        mSelected = 0;
        mPlaceHolder = context.getDrawable(R.drawable.ic_camera_thumbnail_placeholder);
        mMediaLoader = new GlideMediaLoader();
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music, parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.name = view.findViewById(R.id.name);
        holder.resourceThumb = (ImageView) view.findViewById(R.id.resource_thumb);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceHolder holder, final int position) {
        ItemMusic resource = mResourceList.get(position);
        holder.name.setText(resource.getName());
        if (!TextUtils.isEmpty(resource.getThumbPath()) && resource.getThumbPath().startsWith("assets://")) {
            holder.resourceThumb.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext,
                    resource.getThumbPath().substring("assets://".length())));
        } else {
            if (mMediaLoader != null) {
                mMediaLoader.loadThumbnail(mContext, mPlaceHolder, holder.resourceThumb,
                        Uri.parse(resource.getThumbPath()));
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null){
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
