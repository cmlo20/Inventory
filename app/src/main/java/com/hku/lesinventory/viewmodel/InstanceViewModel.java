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

public class InstanceViewModel extends AndroidViewModel {

    private final String mRfidUii;

    private InventoryRepository mRepository;

    private final LiveData<InstanceEntity> mInstance;
    private final LiveData<LocationEntity> mLocation;
    private final LiveData<ItemEntity> mItem;
    private final LiveData<CategoryEntity> mCategory;
    private final LiveData<BrandEntity> mBrand;


    public InstanceViewModel(@NonNull Application application, InventoryRepository repository,
                             final String rfidUii) {
        super(application);
        mRfidUii = rfidUii;
        mRepository = repository;
        mInstance = repository.loadInstanceByRfid(mRfidUii);
        mLocation = repository.loadInstanceLocationByRfid(mRfidUii);
        mItem = repository.loadInstanceItemByRfid(mRfidUii);
        mCategory = repository.loadInstanceCategoryByRfid(mRfidUii);
        mBrand = repository.loadInstanceBrandByRfid(mRfidUii);
    }

    public LiveData<InstanceEntity> getInstance() { return mInstance; }

    public LiveData<LocationEntity> getLocation() { return mLocation; }

    public LiveData<ItemEntity> getItem() { return mItem; }

    public LiveData<CategoryEntity> getCategory() { return mCategory; }

    public LiveData<BrandEntity> getBrand() { return mBrand; }

    /**
     * A factory is used to inject the item ID into the ViewModel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final String mRfidUii;

        private final InventoryRepository mRepository;

        public Factory(@NonNull Application application, final String rfidUii) {
            mApplication = application;
            mRfidUii = rfidUii;
            mRepository = ((InventoryApp) application).getRepository();
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new InstanceViewModel(mApplication, mRepository, mRfidUii);
        }
    }
}
