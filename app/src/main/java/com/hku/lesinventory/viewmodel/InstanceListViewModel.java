package com.hku.lesinventory.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hku.lesinventory.InventoryApp;
import com.hku.lesinventory.db.InventoryRepository;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.List;

public class InstanceListViewModel extends AndroidViewModel {

    private final InventoryRepository mRepository;

    private final LiveData<List<LocationEntity>> mLocations;
    private final LiveData<List<ItemEntity>> mItems;
    private final LiveData<List<BrandEntity>> mBrands;

    public InstanceListViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((InventoryApp) application).getRepository();

        mItems = mRepository.loadAllItems();
        mLocations = mRepository.loadAllLocations();
        mBrands = mRepository.loadAllBrands();
    }

    public LiveData<List<LocationEntity>> loadLocations() { return mLocations; }

    public LiveData<List<ItemEntity>> loadItems() { return mItems; }

    public LiveData<List<BrandEntity>> loadBrands() { return mBrands; }

    public LiveData<List<InstanceEntity>> loadInstancesInLocation(int locationId) {
        return mRepository.loadInstancesInLocation(locationId);
    }

    public InstanceEntity loadInstanceByRfid(String rfidUii) {
        return mRepository.loadInstanceInBackground(rfidUii);
    }

    public void updateInstance(InstanceEntity instance) {
        mRepository.update(instance);
    }
}
