package com.hku.lesinventory.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hku.lesinventory.R;
import com.hku.lesinventory.model.Option;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<NavMenuGroupItem> mExpandableListGroupItems;
    private HashMap<String, List<Option>> mExpandableListDetails;   // mapping the title of the group to its options

    public ExpandableListAdapter(Context context, List<NavMenuGroupItem> groupItems, HashMap<String, List<Option>> listDetail) {
        mContext = context;
        mExpandableListGroupItems = groupItems;
        mExpandableListDetails = listDetail;
    }

    @Override
    public int getGroupCount() {
        return mExpandableListGroupItems.size();
    }

    @Override
    public int getChildrenCount(int listPosition) {
        List<Option> childrenList = mExpandableListDetails.get(mExpandableListGroupItems.get(listPosition).getName());
        if (childrenList != null) {
            return childrenList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int listPosition) {
        return mExpandableListGroupItems.get(listPosition);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return mExpandableListDetails.get(mExpandableListGroupItems.get(listPosition).getName()).get(expandedListPosition);
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
        return true;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View view, ViewGroup parent) {
        NavMenuGroupItem groupItem = (NavMenuGroupItem) getGroup(listPosition);
        String groupTitle = groupItem.getName();
        int groupIconId = groupItem.getIconId();

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_group, null);
        }
        ImageView indicator = view.findViewById(R.id.group_indicator);
        if (getChildrenCount(listPosition) > 0) {
            if (isExpanded) {
                indicator.setImageResource(R.drawable.baseline_expand_less_black_24);
            } else {
                indicator.setImageResource(R.drawable.baseline_expand_more_black_24);
            }
        }
        TextView groupTitleTextView = view.findViewById(R.id.group_title);
        ImageView groupIcon = view.findViewById(R.id.group_icon);
        groupTitleTextView.setText(groupTitle);
        groupIcon.setImageResource(groupIconId);

        return view;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View view, ViewGroup parent) {
        final String itemTitle = (getChild(listPosition, expandedListPosition)).toString();
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
