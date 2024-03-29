package com.owoh.video.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.owoh.R;
import com.owoh.video.video.VideoEditInfo;


import java.util.ArrayList;
import java.util.List;


public class ThumbVideoAdapter extends RecyclerView.Adapter {

    private List<VideoEditInfo> lists = new ArrayList<>();
    private LayoutInflater inflater;

    private int itemW;
    private Context context;

    public ThumbVideoAdapter(Context context, int itemW) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemW = itemW;
    }

    public List<VideoEditInfo> getDatas() {
        return lists;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoHolder(inflater.inflate(R.layout.item_video_thumb, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoHolder viewHolder = (VideoHolder) holder;
        Glide.with(context)
            .load(lists.get(position).getPath())
            .into(viewHolder.img);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    private final class VideoHolder extends RecyclerView.ViewHolder {

        public ImageView img;

        VideoHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.thumb);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) img
                .getLayoutParams();
            layoutParams.width = itemW;
            img.setLayoutParams(layoutParams);
        }
    }

    public void addItemVideoInfo(VideoEditInfo info) {
        lists.add(info);
        notifyItemInserted(lists.size());
    }
}
