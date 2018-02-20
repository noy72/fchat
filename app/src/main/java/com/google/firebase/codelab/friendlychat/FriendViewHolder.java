package com.google.firebase.codelab.friendlychat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by noy on 2018/02/20.
 */

class FriendViewHolder extends RecyclerView.ViewHolder {

    FriendViewHolder.ClickListener mClickListenr;
    TextView mFriendName;
    CircleImageView mImageView;

    public interface ClickListener {
        public void onItemClick(View view, int position);
    }

    public FriendViewHolder(View itemView) {
        super(itemView);
        mFriendName = itemView.findViewById(R.id.friendName);
        mImageView = itemView.findViewById(R.id.imageView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListenr.onItemClick(view, getAdapterPosition());
            }
        });
    }

    public void setOnClickListener(FriendViewHolder.ClickListener clickListener) {
        mClickListenr = clickListener;
    }
}
