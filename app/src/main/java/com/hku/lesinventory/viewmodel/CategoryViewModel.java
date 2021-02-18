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
import com.hku.lesinventory.db.entity.ItemWithInstances;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final int mCategoryId;

    private final LiveData<CategoryEntity> mCategory;
    private final LiveData<List<ItemWithInstances>> mItemsInCategory;
    private final LiveData<List<BrandEntity>> mAllBrands;

    public CategoryViewModel(@NonNull Application application, InventoryRepository repository,
                             final int categoryId) {
        super(application);
        mCategoryId = categoryId;

        mCategory = repository.loadCategory(mCategoryId);
        mItemsInCategory = repository.loadItemsInCategory(mCategoryId);
        mAllBrands = repository.loadAllBrands();
    }

    public LiveData<CategoryEntity> getCategory() { return mCategory; }

    public LiveData<List<ItemWithInstances>> getItems() { return mItemsInCategory; }

    public LiveData<List<BrandEntity>> getBrands() { return mAllBrands; }

    // Factory class to inject category ID into the ViewModel
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mCategoryId;

        private final InventoryRepository mRepository;

        public Factory(@NonNull Application application, int categoryId) {
            mApplication = application;
            mCategoryId = categoryId;
            mRepository = ((InventoryApp) application).getRepository();
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CategoryViewModel(mApplication, mRepository, mCategoryId);
        }
    }
}
