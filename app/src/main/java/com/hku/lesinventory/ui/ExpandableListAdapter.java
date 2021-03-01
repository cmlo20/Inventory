package com.hku.lesinventory.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.hku.lesinventory.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> mExpandableListGroupTitles;
    private HashMap<String, List<String>> mExpandableListDetails;

    public ExpandableListAdapter(Context context, List<String> groupTitles, HashMap<String, List<String>> listDetail) {
        mContext = context;
        mExpandableListGroupTitles = groupTitles;
        mExpandableListDetails = listDetail;
    }

    @Override
    public int getGroupCount() {
        return mExpandableListGroupTitles.size();
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return mExpandableListDetails.get(mExpandableListGroupTitles.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return mExpandableListGroupTitles.get(listPosition);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return mExpandableListDetails.get(mExpandableListGroupTitles.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View view, ViewGroup parent) {
        String groupTitle = (String) getGroup(listPosition);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_group, null);
        }
        TextView groupTitleTextView = view.findViewById(R.id.group_title);
        groupTitleTextView.setText(groupTitle);
        return view;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View view, ViewGroup parent) {
        final String itemTitle = (String) getChild(listPosition, expandedListPosition);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_item, null);
        }
        TextView itemTitleTextView = view.findViewById(R.id.item_title);
        itemTitleTextView.setText(itemTitle);
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
