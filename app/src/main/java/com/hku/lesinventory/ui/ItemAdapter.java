package com.hku.lesinventory.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.InventoryListItemBinding;
import com.hku.lesinventory.model.Brand;
import com.hku.lesinventory.model.Item;
import com.hku.lesinventory.viewmodel.ItemViewModel;

import java.io.IOException;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{

    List<? extends Item> mItemList;
    List<? extends Brand> mBrandList;

    @Nullable
    private final ItemClickCallback mItemClickCallback;

    public ItemAdapter(@Nullable ItemClickCallback clickCallback) {
        mItemClickCallback = clickCallback;
        setHasStableIds(true);
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
                            && TextUtils.equals(newItem.getName(), oldItem.getName())
                            && TextUtils.equals(newItem.getDescription(), oldItem.getDescription())
                            && TextUtils.equals(newItem.getImageUriString(), oldItem.getImageUriString())
                            && newItem.getBrandId() == oldItem.getBrandId()
                            && newItem.getCategoryId() == oldItem.getCategoryId()
                            && newItem.getQuantity() == oldItem.getQuantity();
                }
            });
            mItemList = itemList;
            result.dispatchUpdatesTo(this);
        }
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

    @Override
    @NonNull
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InventoryListItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.inventory_list_item, parent, false);
        binding.setCallback(mItemClickCallback);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = mItemList.get(position);
        holder.binding.setItem(item);   // bind item data

        Context context = holder.binding.getRoot().getContext();
        Uri imageUri = Uri.parse(item.getImageUriString());
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            holder.binding.itemImage.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int itemBrandId = item.getBrandId();
        for (Brand brand : mBrandList) {
            if (brand.getId() == itemBrandId) {
                holder.binding.setBrand(brand);     // bind brand data
            }
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() { return mItemList == null ? 0 : mItemList.size(); }

    @Override
    public long getItemId(int position) { return mItemList.get(position).getId(); }


    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final InventoryListItemBinding binding;

        public ItemViewHolder(InventoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}