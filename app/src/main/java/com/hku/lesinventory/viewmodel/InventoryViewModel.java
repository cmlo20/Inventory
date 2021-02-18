package com.hku.lesinventory.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hku.lesinventory.InventoryApp;
import com.hku.lesinventory.db.InventoryRepository;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.ItemWithInstances;

import java.util.List;

public class InventoryViewModel extends AndroidViewModel {

    private final InventoryRepository mRepository;

    private final LiveData<List<ItemEntity>> mItems;
    private final LiveData<List<CategoryEntity>> mCategories;
    private final LiveData<List<String>> mCategoryNames;
    private final LiveData<List<BrandEntity>> mBrands;
    private final LiveData<List<String>> mBrandNames;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((InventoryApp) application).getRepository();

        mItems = mRepository.loadAllItems();
        mCategories = mRepository.loadAllCategories();
        mCategoryNames = mRepository.loadAllCategoryNames();
        mBrands = mRepository.loadAllBrands();
        mBrandNames = mRepository.loadAllBrandNames();
    }

    /**
     * Expose LiveData query so the UI can observe it
     */
    public LiveData<List<ItemEntity>> getItems() { return mItems; }

    public LiveData<List<CategoryEntity>> getCategories() { return mCategories; }

    public LiveData<List<String>> getCategoryNames() { return mCategoryNames; }

    public int getCategoryId(String categoryName) { return mRepository.getCategoryId(categoryName); }

    public LiveData<List<BrandEntity>> getBrands() { return mBrands; }

    public LiveData<List<String>> getBrandNames() { return mBrandNames; }

    public int getBrandId(String brandName) { return mRepository.getBrandId(brandName); }


    public void insertItem(ItemEntity item) { mRepository.insert(item); }

    public void insertCategory(CategoryEntity category) { mRepository.insert(category); }

    public void insertBrand(BrandEntity brand) { mRepository.insert(brand); }
}
