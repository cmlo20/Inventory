package com.hku.lesinventory.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.InventoryListItemBinding;
import com.hku.lesinventory.db.entity.ItemWithInstances;
import com.hku.lesinventory.model.Brand;
import com.hku.lesinventory.model.Item;
import com.hku.lesinventory.viewmodel.ItemViewModel;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{

    public static final String TAG = ItemAdapter.class.getName();

    List<ItemWithInstances> mItemList;
    List<? extends Brand> mBrandList;

    @Nullable
    private final ItemClickCallback mItemClickCallback;

    public ItemAdapter(@Nullable ItemClickCallback clickCallback) {
        mItemClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setItemList(final List<ItemWithInstances> itemList) {
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
                    return mItemList.get(oldItemPosition).item.getId() ==
                            itemList.get(newItemPosition).item.getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Item newItem = itemList.get(newItemPosition).item;
                    Item oldItem = mItemList.get(oldItemPosition).item;
                    int newQuantity = itemList.get(newItemPosition).instances.size();
                    int oldQuantity = mItemList.get(oldItemPosition).instances.size();
                    return newItem.getId() == oldItem.getId()
                            && TextUtils.equals(newItem.getName(), oldItem.getName())
                            && TextUtils.equals(newItem.getDescription(), oldItem.getDescription())
                            && TextUtils.equals(newItem.getImageUriString(), oldItem.getImageUriString())
                            && newItem.getBrandId() == oldItem.getBrandId()
                            && newItem.getCategoryId() == oldItem.getCategoryId()
                            && newQuantity == oldQuantity;
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
        if (mItemList != null) {
            Item item = mItemList.get(position).item;
            holder.binding.setItem(item);   // bind item data

            Integer quantity = mItemList.get(position).instances.size();
            holder.binding.setItemQuantity(quantity);   // bind item quantity

            // Set item image
            String imageUriString = item.getImageUriString();
            if (imageUriString != null && !imageUriString.isEmpty()) {
                try {
                    Bitmap imageThumbnail = getThumbnail(Uri.parse(imageUriString), holder);
                    holder.binding.itemImage.setImageBitmap(imageThumbnail);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (mBrandList != null) {
                int itemBrandId = item.getBrandId();
                for (Brand brand : mBrandList) {
                    if (brand.getId() == itemBrandId) {
                        holder.binding.setBrand(brand);     // bind brand data
                    }
                }
            }
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() { return mItemList == null ? 0 : mItemList.size(); }

    @Override
    public long getItemId(int position) { return mItemList.get(position).item.getId(); }

    // Scale item image according to list thumbnail size
    private Bitmap getThumbnail(Uri imageUri, ItemViewHolder holder) throws FileNotFoundException {
        Context context = holder.binding.getRoot().getContext();
        int targetWidth = (int) context.getResources().getDimension(R.dimen.list_item_image_width);
        int targetHeight = (int) context.getResources().getDimension(R.dimen.list_item_image_height);

        FileDescriptor fd = context.getContentResolver().openFileDescriptor(imageUri, "r").getFileDescriptor();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, bmOptions);

        int imageWidth = bmOptions.outWidth;
        int imageHeight = bmOptions.outHeight;
//        Log.i(TAG, "image width = " + imageWidth);
        int scaleFactor = Math.max(1, Math.min(imageWidth/targetWidth, imageHeight/targetHeight));
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeFileDescriptor(fd, null, bmOptions);
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final InventoryListItemBinding binding;

        public ItemViewHolder(InventoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}