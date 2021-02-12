package com.hku.lesinventory.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hku.lesinventory.InventoryApp;
import com.hku.lesinventory.db.InventoryRepository;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.List;

public class ItemViewModel extends AndroidViewModel {

    private final int mItemId;

    private InventoryRepository mRepository;

    private final LiveData<ItemEntity> mItem;
    private final LiveData<BrandEntity> mItemBrand;
    private final LiveData<CategoryEntity> mItemCategory;
    private final LiveData<List<InstanceEntity>> mItemInstances;
    private final LiveData<List<InstanceEntity>> mAllInstances;
    private final LiveData<List<LocationEntity>> mLocations;
    private final LiveData<List<String>> mLocationNames;
    private final LiveData<String> mImageUriString;

    public ItemViewModel(@NonNull Application application, InventoryRepository repository,
                         final int itemId) {
        super(application);
        mItemId = itemId;
        mRepository = repository;
        mItem = repository.loadItem(mItemId);
        mItemBrand = repository.getItemBrand(mItemId);
        mItemCategory = repository.getItemCategory(mItemId);
        mItemInstances = repository.loadItemInstances(mItemId);
        mAllInstances = repository.loadAllInstances();
        mLocations = repository.loadAllLocations();
        mLocationNames = repository.loadAllLocationNames();
        mImageUriString = repository.getImageUriString(mItemId);
    }

    public LiveData<ItemEntity> getItem() { return mItem; }

    public LiveData<BrandEntity> getItemBrand() { return mItemBrand; }

    public LiveData<CategoryEntity> getItemCategory() { return mItemCategory; }

    public LiveData<String> getImageUriString() { return mImageUriString; }

    public LiveData<List<InstanceEntity>> getInstances() { return mItemInstances; }

    public LiveData<List<InstanceEntity>> getAllInstances() { return mAllInstances; }

    public LiveData<List<LocationEntity>> getLocations() { return mLocations; }

    public LiveData<List<String>> getLocationNames() { return mLocationNames; }

    public int getLocationId(String locationName) { return mRepository.getLocationId(locationName); }


    public void insertInstance(InstanceEntity instance) { mRepository.insert(instance); }

    /**
     * A factory is used to inject the item ID into the ViewModel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mItemId;

        private final InventoryRepository mRepository;

        public Factory(@NonNull Application application, final int itemId) {
            mApplication = application;
            mItemId = itemId;
            mRepository = ((InventoryApp) application).getRepository();
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemViewModel(mApplication, mRepository, mItemId);
        }
    }
}
