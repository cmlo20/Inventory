package com.hku.lesinventory.ui;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.InstanceListItemBinding;
import com.hku.lesinventory.model.Instance;
import com.hku.lesinventory.model.Location;

import java.util.List;

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.InstanceViewHolder>{

    List<? extends Instance> mInstanceList;
    List<? extends Location> mLocationList;

    @Nullable
    private final InstanceClickCallback mInstanceClickCallback;

    public InstanceAdapter(@Nullable InstanceClickCallback clickCallback) {
        mInstanceClickCallback = clickCallback;
        setHasStableIds(true);
    }

    public void setInstanceList(final List<? extends Instance> instanceList) {
        if (mInstanceList == null) {
            mInstanceList = instanceList;
            notifyItemRangeInserted(0, instanceList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return mInstanceList.size();
                }

                @Override
                public int getNewListSize() {
                    return instanceList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mInstanceList.get(oldItemPosition).getId() ==
                            instanceList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Instance newInstance = instanceList.get(newItemPosition);
                    Instance oldInstance = mInstanceList.get(oldItemPosition);
                    return newInstance.getId() == oldInstance.getId()
                            && newInstance.getLocationId() == oldInstance.getLocationId()
                            && TextUtils.equals(newInstance.getSerialNo(), oldInstance.getSerialNo())
                            && TextUtils.equals(newInstance.getRfidUii(), oldInstance.getRfidUii());
                }
            });
            mInstanceList = instanceList;
            result.dispatchUpdatesTo(this);
        }
    }

    public void setLocationList(final List<? extends Location> locationList) {
        if (mLocationList == null) {
            mLocationList = locationList;
            notifyItemRangeInserted(0, locationList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return mLocationList.size();
                }

                @Override
                public int getNewListSize() {
                    return locationList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mLocationList.get(oldItemPosition).getId() ==
                            locationList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Location newLocation = locationList.get(newItemPosition);
                    Location oldLocation = mLocationList.get(oldItemPosition);
                    return newLocation.getId() == oldLocation.getId()
                            && TextUtils.equals(newLocation.getName(), oldLocation.getName());
                }
            });
            mLocationList = locationList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    @NonNull
    public InstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        InstanceListItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.instance_list_item, parent, false);
        binding.setCallback(mInstanceClickCallback);
        return new InstanceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceViewHolder holder, int position) {
        Instance instance = mInstanceList.get(position);
        holder.binding.setInstance(instance);

        int locationId = instance.getLocationId();
        if (mLocationList != null) {
            for (Location location : mLocationList) {
                if (location.getId() == locationId) {
                    holder.binding.setLocation(location);
                }
            }
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() { return mInstanceList == null ? 0 : mInstanceList.size(); }

    @Override
    public long getItemId(int position) { return mInstanceList.get(position).getId(); }


    static class InstanceViewHolder extends RecyclerView.ViewHolder {

        final InstanceListItemBinding binding;

        public InstanceViewHolder(InstanceListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
