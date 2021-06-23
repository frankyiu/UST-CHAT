package com.example.ustchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SecondLevelAdapter extends BaseExpandableListAdapter {
    private Context context;
    List<List<String>> data;
    List<String> headers;

    public SecondLevelAdapter(Context context, List<String> headers, List<List<String>> data) {
        this.context = context;
        this.data = data;
        this.headers = headers;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return headers.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_second, null);
        TextView text = (TextView) convertView.findViewById(R.id.secondRowHeader);

        ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
        if (isExpanded) {
            ivIcon.setImageResource(R.drawable.ic_collapse);
        } else {
            ivIcon.setImageResource(R.drawable.ic_expand);
        }

        String groupText = getGroup(groupPosition).toString();
        text.setText(groupText);
        return convertView;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<String> childData;
        childData = data.get(groupPosition);
        return childData.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_third, null);
        TextView textView = (TextView) convertView.findViewById(R.id.rowThirdText);
        String text = data.get(groupPosition).get(childPosition);
        textView.setText(text);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(groupPosition).size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
