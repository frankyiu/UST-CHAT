package com.example.ustchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChatroomRecyclerAdapter extends RecyclerView.Adapter<ChatroomRecyclerAdapter.ViewHolder> {
    List<ChatroomRecord> chatroomRecords;
    LayoutInflater inflater;

    public ChatroomRecyclerAdapter(Context context, List<ChatroomRecord> _chatroomRecords) {
        inflater = LayoutInflater.from(context);
        chatroomRecords = _chatroomRecords;
    }

    @NonNull
    @Override
    public ChatroomRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_chatroom_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomRecyclerAdapter.ViewHolder holder, int position) {
        ChatroomRecord chatroomRecord = chatroomRecords.get(position);
        holder.tvTitle.setText(chatroomRecord.getTitle());
        holder.tvLatestName.setText(chatroomRecord.getLatestName());
        String latestReply = chatroomRecord.getLatestReply();
        if (latestReply.equals("{photo}")) {
            holder.ivLatestReply.setBackgroundResource(R.drawable.ic_photo);
            holder.tvLatestReply.setTextColor(inflater.getContext().getResources().getColor(R.color.gray_400));
            latestReply = "Photo";
        }
        holder.tvLatestReply.setText(latestReply);
        int chatCnt = chatroomRecord.getChatCount();
        int viewCnt = chatroomRecord.getViewCount();
        if (chatCnt < 100) {
            holder.tvChatCnt.setText(String.valueOf(chatCnt));
        }
        else {
            holder.tvChatCnt.setText("99+");
        }
        if (viewCnt < 100) {
            holder.tvViewCnt.setText(String.valueOf(viewCnt));
        }
        else {
            holder.tvViewCnt.setText("99+");
        }
        holder.tvPosterName.setText(chatroomRecord.getPosterName());
        holder.tvTitle.setText(chatroomRecord.getTitle());
        holder.tvCreateDate.setText(chatroomRecord.getCreateDate());
        holder.setBookmarked(chatroomRecord.isBookmarked());

        List<String> selectedTags = chatroomRecord.getTags();
        for (int i = 0; i < holder.tagGroup.getChildCount(); i++) {
            Chip tag = (Chip) holder.tagGroup.getChildAt(i);
            boolean contains = false;
            for (int j = 0; j < selectedTags.size(); j++) {
                if (tag.getText().equals(selectedTags.get(j))) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                tag.setVisibility(View.GONE);
            }
        }

        holder.cvChatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChatroomChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("chatroomTitle", chatroomRecords.get(position).getTitle());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatroomRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvChatroom;
        TextView tvTitle, tvLatestName, tvLatestReply, tvChatCnt, tvViewCnt, tvPosterName, tvCreateDate;
        ImageView ivLatestReply, ivBookmark;
        ChipGroup tagGroup;
        boolean isBookmarked = false;

        public void setBookmarked(boolean bookmarked) {
            isBookmarked = bookmarked;
        }

        public void fillBookmarkIconColor() {
            if (isBookmarked) {
                ivBookmark.setColorFilter(Color.argb(255, 0 , 0, 0));
            }
            else {
                ivBookmark.setColorFilter(Color.argb(255, 127 , 127, 127));
            }
        }

        View.OnClickListener mToggleBookmarkIcon = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                isBookmarked = !isBookmarked;
                fillBookmarkIconColor();
            }
        };

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvChatroom = itemView.findViewById(R.id.cv_chatroom);
            tvTitle = itemView.findViewById(R.id.tv_chatroom_record_title);
            tvLatestName = itemView.findViewById(R.id.tv_chatroom_record_latest_name);
            tvLatestReply = itemView.findViewById(R.id.tv_chatroom_record_latest_reply);
            tvChatCnt = itemView.findViewById(R.id.tv_chatroom_record_chat_count);
            tvViewCnt = itemView.findViewById(R.id.tv_chatroom_record_view_count);
            tvPosterName = itemView.findViewById(R.id.tv_chatroom_record_poster_name);
            tvCreateDate = itemView.findViewById(R.id.tv_chatroom_record_create_date);
            ivBookmark = itemView.findViewById(R.id.iv_chatroom_record_bookmark);
            tagGroup = itemView.findViewById(R.id.chipgroup_chatroom_create_tags);
            ivLatestReply = itemView.findViewById(R.id.iv_chatroom_record_latest_reply);

            ivBookmark.setOnClickListener(mToggleBookmarkIcon);

            fillBookmarkIconColor();

        }
    }
}