package com.owoh.video.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owoh.R;
import com.owoh.video.ItemSticker;

import java.util.List;

/**
 * 音乐
 */
public class StickerIndexAdapter extends RecyclerView.Adapter<StickerIndexAdapter.ResourceHolder> {

    private List<ItemSticker.ItemStickerIndex> mResourceList;
    private Context mContext;
    private int mSelected;
    private OnItemClickListener mListener;

    public StickerIndexAdapter(Context context, List<ItemSticker.ItemStickerIndex> itemList) {
        mContext = context;
        mResourceList = itemList;
        mSelected = 0;
    }

    @NonNull
    @Override
    public ResourceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sticker_index, parent, false);
        ResourceHolder holder = new ResourceHolder(view);
        holder.name = view.findViewById(R.id.tvName);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceHolder holder, final int position) {
        ItemSticker.ItemStickerIndex resource = mResourceList.get(position);
        holder.name.setText(resource.getName_cn());
        holder.name.setSelected(resource.isSelected());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelected == position) {
                    return;
                }
                int last = mSelected;
                mSelected = position;
                mResourceList.get(last).setSelected(false);
                mResourceList.get(mSelected).setSelected(true);
                notifyItemChanged(last);
                notifyItemChanged(position);
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

        public ResourceHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnItemClickListener {
        void onClick(int position);
    }

}
