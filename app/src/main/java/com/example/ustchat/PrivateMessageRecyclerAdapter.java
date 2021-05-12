package com.example.ustchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PrivateMessageRecyclerAdapter extends RecyclerView.Adapter<PrivateMessageRecyclerAdapter.ViewHolder> {

    List<PrivateMessageRecord> PrivateMessageRecords;
    LayoutInflater inflater;

    public PrivateMessageRecyclerAdapter(Context context, List<PrivateMessageRecord> _PrivateMessageRecords) {
        inflater = LayoutInflater.from(context);
        PrivateMessageRecords = _PrivateMessageRecords;
    }

    @NonNull
    @Override
    public PrivateMessageRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_pm_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateMessageRecyclerAdapter.ViewHolder holder, int position) {
        PrivateMessageRecord privateMessageRecord = PrivateMessageRecords.get(position);
        holder.tvTitle.setText(privateMessageRecord.getTitle());
        holder.tvLatestName.setText(privateMessageRecord.getLatestName());
        String latestReply = privateMessageRecord.getLatestReply();
        if (latestReply.equals("{photo}")) {
            holder.ivLatestReply.setBackgroundResource(R.drawable.ic_photo);
            holder.tvLatestReply.setTextColor(inflater.getContext().getResources().getColor(R.color.gray_400));
            latestReply = "Photo";
        }
        holder.tvLatestReply.setText(latestReply);

        int unreadCount = privateMessageRecord.getUnreadCount();
        if (unreadCount == 0) {
            holder.tvUnreadCounter.setVisibility(View.GONE);
        }
        else {
            String unreadCountStr;
            if (unreadCount > 99) {
                unreadCountStr = "99+";
            }
            else {
                unreadCountStr = String.valueOf(unreadCount);
            }
            holder.tvLatestReplyTime.setTextColor(inflater.getContext().getResources().getColor(R.color.pm_unread_counter_bg_color));
            holder.tvUnreadCounter.setText(unreadCountStr);
        }

//        holder.cvPM.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), ChatActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("chatroomTitle", PMRecords.get(position).getTitle());
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtras(bundle);
//                v.getContext().startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return PrivateMessageRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvPM;
        TextView tvTitle, tvLatestName, tvLatestReply, tvLatestReplyTime, tvUnreadCounter;
        ImageView ivLatestReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvPM = itemView.findViewById(R.id.cv_pm);
            tvTitle = itemView.findViewById(R.id.tv_pm_title);
            tvLatestName = itemView.findViewById(R.id.tv_pm_latest_name);
            tvLatestReply = itemView.findViewById(R.id.tv_pm_latest_reply);
            ivLatestReply = itemView.findViewById(R.id.iv_pm_latest_reply);
            tvLatestReplyTime = itemView.findViewById(R.id.tv_pm_latest_reply_time);
            tvUnreadCounter = itemView.findViewById(R.id.tv_pm_unread_counter);
        }
    }
}