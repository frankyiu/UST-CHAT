package com.example.ustchat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PrivateChatRecyclerAdapter extends RecyclerView.Adapter<PrivateChatRecyclerAdapter.ViewHolder> {
    List<PrivateChatRecord> privateChatRecords;
    LayoutInflater inflater;
    Context context;

    public PrivateChatRecyclerAdapter(Context context, List<PrivateChatRecord> privateChatRecords) {
        inflater = LayoutInflater.from(context);
        this.privateChatRecords = privateChatRecords;
        this.context = context;
    }

    @NonNull
    @Override
    public PrivateChatRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_chat_bubble, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateChatRecyclerAdapter.ViewHolder holder, int position) {
        PrivateChatRecord privateChatRecord = privateChatRecords.get(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (privateChatRecord.isUser()) {
            params.gravity = Gravity.RIGHT;
        }
        else {
            params.gravity = Gravity.LEFT;
        }
        holder.cvPrivateChat.setLayoutParams(params);

        // TO-DO : not id for now
        String quotatedID = privateChatRecord.getQuotedID();
        if (quotatedID.isEmpty()) {
            holder.vQuoteDivider.setVisibility(View.GONE);
            holder.ivQuotedText.setVisibility(View.GONE);
            holder.ivQuotedImage.setVisibility(View.GONE);
        } else {
            holder.vQuoteDivider.setVisibility(View.VISIBLE);
            holder.ivQuotedText.setVisibility(View.VISIBLE);
            if (quotatedID.equals("{photo}")) {
                holder.ivQuotedImage.setVisibility(View.VISIBLE);
                holder.ivQuotedImage.setBackgroundResource(R.drawable.ic_photo);
                quotatedID = "Photo";
            }
            else {
                holder.ivQuotedImage.setVisibility(View.GONE);
            }
            holder.ivQuotedText.setText(quotatedID);
        }
        String image = privateChatRecord.getImage();
        String text = privateChatRecord.getText();
        if (image.isEmpty()) {
            holder.ivImage.setVisibility(View.GONE);
        } else {
            holder.ivImage.setVisibility(View.VISIBLE);
            Drawable imageDrawable = Utility.loadImageFromUrl(image);
            holder.ivImage.setImageDrawable(imageDrawable);
        }
        holder.tvText.setText(text);
        holder.tvTime.setText(privateChatRecord.getTime());

        holder.cvPrivateChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((PrivateMessageChatActivity) context).openReplyHandlerDialog(privateChatRecords.get(position));
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return privateChatRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvPrivateChat;
        TextView tvName, tvText, ivQuotedText, tvTime;
        ImageView ivImage, ivQuotedImage;
        View vQuoteDivider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvPrivateChat = itemView.findViewById(R.id.cv_chat_bubble);
            tvName = itemView.findViewById(R.id.tv_chat_name);
            tvText = itemView.findViewById(R.id.tv_chat_text);
            ivImage = itemView.findViewById(R.id.tv_chat_image);
            tvTime = itemView.findViewById(R.id.tv_chat_time);
            ivQuotedImage = itemView.findViewById(R.id.tv_chat_quote_image);
            ivQuotedText = itemView.findViewById(R.id.tv_chat_quote_text);
            vQuoteDivider = itemView.findViewById(R.id.v_chat_quote_divider);
            tvName.setVisibility(View.GONE);
        }
    }

}