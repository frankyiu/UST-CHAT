package com.example.ustchat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ChatroomChatRecyclerAdapter extends RecyclerView.Adapter<ChatroomChatRecyclerAdapter.ViewHolder> {
    List<ChatroomChatRecord> chatroomChatRecords;
    LayoutInflater inflater;
    Context context;

    public ChatroomChatRecyclerAdapter(Context context, List<ChatroomChatRecord> chatroomChatRecords) {
        inflater = LayoutInflater.from(context);
        this.chatroomChatRecords = chatroomChatRecords;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatroomChatRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_chatroom_chat_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomChatRecyclerAdapter.ViewHolder holder, int position) {
        ChatroomChatRecord chatroomChatRecord = chatroomChatRecords.get(position);
        if (chatroomChatRecord.isUser()) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                             LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.RIGHT;
            holder.cvChatroomChat.setLayoutParams(params);
        }

        holder.tvName.setText(chatroomChatRecord.getName());
        String quotatedContent = chatroomChatRecord.getQuotedContent();
        if (quotatedContent.isEmpty()) {
            holder.vQuoteDivider.setVisibility(View.GONE);
            holder.ivQuotedText.setVisibility(View.GONE);
        }
        else {
            if (quotatedContent.equals("{photo}")) {
                holder.ivQuotedImage.setBackgroundResource(R.drawable.ic_photo);
                quotatedContent = "Photo";
            }
            holder.ivQuotedText.setText(quotatedContent);
        }
        String text = chatroomChatRecord.getContent();
        holder.tvText.setText(text);
        holder.tvTime.setText(chatroomChatRecord.getTime());

        holder.cvChatroomChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((ChatActivity) context).openDialog(chatroomChatRecords.get(position));
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatroomChatRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvChatroomChat;
        TextView tvName, tvText, ivQuotedText, tvTime;
        ImageView ivImage, ivQuotedImage;
        View vQuoteDivider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvChatroomChat = itemView.findViewById(R.id.cv_chatroom_chat_bubble);
            tvName = itemView.findViewById(R.id.tv_chatroom_chat_name);
            tvText = itemView.findViewById(R.id.tv_chatroom_chat_text);
            ivImage = itemView.findViewById(R.id.tv_chatroom_chat_image);
            tvTime = itemView.findViewById(R.id.tv_chatroom_chat_time);
            ivQuotedImage = itemView.findViewById(R.id.tv_chatroom_chat_quote_image);
            ivQuotedText = itemView.findViewById(R.id.tv_chatroom_chat_quote_text);
            vQuoteDivider = itemView.findViewById(R.id.v_chatroom_chat_quote_divider);
        }
    }
}