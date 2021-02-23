package com.hku.lesinventory.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.TagInstanceListItemBinding;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.model.Brand;
import com.hku.lesinventory.model.Instance;
import com.hku.lesinventory.model.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TagInstanceAdapter extends RecyclerView.Adapter<TagInstanceAdapter.TagInstanceViewHolder>{

    List<InstanceEntity> mInstanceList;
    List<? extends Brand> mBrandList;
    List<? extends Item> mItemList;

    @Nullable
    private final InstanceClickCallback mInstanceClickCallback;

    public TagInstanceAdapter(@Nullable InstanceClickCallback clickCallback) {
        mInstanceClickCallback = clickCallback;
        mInstanceList = new ArrayList<InstanceEntity>();
        setHasStableIds(true);
    }

    public void setBrandList(final List<? extends Brand> brandList) {
        if (mBrandList == null) {
            mBrandList = brandList;
            notifyItemRangeInserted(0, brandList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return mBrandList.size();
                }

                @Override
                public int getNewListSize() {
                    return brandList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mBrandList.get(oldItemPosition).getId() ==
                            brandList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Brand newBrand = brandList.get(newItemPosition);
                    Brand oldBrand = mBrandList.get(oldItemPosition);
                    return newBrand.getId() == oldBrand.getId()
                            && TextUtils.equals(newBrand.getName(), oldBrand.getName());
                }
            });
            mBrandList = brandList;
            result.dispatchUpdatesTo(this);
        }
    }

    public void setItemList(final List<? extends Item> itemList) {
        if (mItemList == null) {
            mItemList = itemList;
            notifyItemRangeInserted(0, itemList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return mItemList.size();
                }

                @Override
                public int getNewListSize() {
                    return itemList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mItemList.get(oldItemPosition).getId() ==
                            itemList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Item newItem = itemList.get(newItemPosition);
                    Item oldItem = mItemList.get(oldItemPosition);
                    return newItem.getId() == oldItem.getId()
                            && newItem.getBrandId() == oldItem.getBrandId()
                            && newItem.getCategoryId() == oldItem.getCategoryId()
                            && TextUtils.equals(newItem.getName(), oldItem.getName())
                            && TextUtils.equals(newItem.getDescription(), oldItem.getDescription())
                            && TextUtils.equals(newItem.getImageUriString(), oldItem.getImageUriString());
                }
            });
            mItemList = itemList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    @NonNull
    public TagInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TagInstanceListItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.tag_instance_list_item, parent, false);
        binding.setCallback(mInstanceClickCallback);
        return new TagInstanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagInstanceViewHolder holder, int position) {
        // Set instance
        Instance instance = mInstanceList.get(position);
        Log.i("TAdapter", "binding instance #" + instance.getRfidUii());
        holder.binding.setInstance(instance);
        // Set item
        int itemId = instance.getItemId();
        if (mItemList != null) {
            for (Item item : mItemList) {
                if (item.getId() == itemId) {
                    holder.binding.setItem(item);
                    // Set brand
                    int brandId = item.getBrandId();
                    if (mBrandList != null) {
                        for (Brand brand : mBrandList) {
                            if (brand.getId() == brandId) {
                                holder.binding.setBrand(brand);
                            }
                        }
                    }
                }
            }
        }
        // Set check-in time display
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        holder.binding.checkInTime.setText(formatter.format(date));
    }

    @Override
    public int getItemCount() { return mInstanceList == null ? 0 : mInstanceList.size(); }

    @Override
    public long getItemId(int position) { return mInstanceList.get(position).getId(); }

    public void addInstance(InstanceEntity instance) {
        mInstanceList.add(instance);
        this.notifyDataSetChanged();
    }

    public void clearInstanceList() {
        mInstanceList.clear();
        this.notifyDataSetChanged();
    }


    static class TagInstanceViewHolder extends RecyclerView.ViewHolder {

        final TagInstanceListItemBinding binding;

        public TagInstanceViewHolder(TagInstanceListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
