package com.owoh.video.widget.recycleview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owoh.R;

import java.util.List;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder> implements View.OnClickListener {

    Context context;
    List<String> mList;

    private onItemClickLisitenter onItem;

    public RvAdapter(Context context, List<String> mList) {
        this.context = context;
        this.mList = mList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = View.inflate(context, R.layout.item_center_tv, null);
        view.setOnClickListener(this);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.mTxt.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickLisitenter(onItemClickLisitenter onItem) {
        this.onItem = onItem;
    }

    @Override
    public void onClick(View v) {
        if (onItem != null) {
            onItem.onItemClick(v, (Integer) v.getTag());
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        TextView mTxt;

        public ViewHolder(View itemView) {
            super(itemView);

            mTxt = (TextView) itemView.findViewById(R.id.itemView);
        }
    }


    public interface onItemClickLisitenter {
        void onItemClick(View v, int position);
    }

    ;
}
