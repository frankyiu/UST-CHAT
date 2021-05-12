package com.example.ustchat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class ThreeLevelListAdapter extends BaseExpandableListAdapter {
    List<String> parentHeaders;
    List<List<String>> secondLevel;
    private Context context;
    List<LinkedHashMap<String, List<String>>> data;

    public ThreeLevelListAdapter(Context context, List<String> parentHeader,
                                 List<List<String>> secondLevel, List<LinkedHashMap<String, List<String>>> data) {
        this.context = context;
        this.parentHeaders = parentHeader;
        this.secondLevel = secondLevel;
        this.data = data;
    }

    @Override
    public int getGroupCount() {
        return parentHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) { return data.get(groupPosition).size() > 0 ? 1 : 0; }

    @Override
    public Object getGroup(int groupPosition) { return groupPosition; }

    @Override
    public Object getChild(int group, int child) { return child; }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_first, null);
        TextView text = (TextView) convertView.findViewById(R.id.firstRowHeader);

        if (Objects.requireNonNull(this.data.get(groupPosition)).size() > 0) {
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            if (isExpanded) {
                ivIcon.setImageResource(R.drawable.ic_collapse);
            } else {
                ivIcon.setImageResource(R.drawable.ic_expand);
            }
        }

        text.setText(this.parentHeaders.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SecondLevelExpandableListView secondLevelELV = new SecondLevelExpandableListView(context);
        List<String> headers = secondLevel.get(groupPosition);
        List<List<String>> childData = new ArrayList<>();
        HashMap<String, List<String>> secondLevelData = data.get(groupPosition);
        for (String key : secondLevelData.keySet()) {
            childData.add(secondLevelData.get(key));
        }
        secondLevelELV.setAdapter(new SecondLevelAdapter(context, headers, childData));
        secondLevelELV.setGroupIndicator(null);
        secondLevelELV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                CourseActivity mc = (CourseActivity) context;
                mc.drawerLayout.closeDrawer(GravityCompat.START);
                String courseCode = childData.get(groupPosition).get(childPosition);
                mc.switchChatroomFragment(courseCode);
                return false;
            }
        });
        return secondLevelELV;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}