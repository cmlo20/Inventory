package com.hku.lesinventory.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.hku.lesinventory.db.InventoryRepository;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;

import java.util.List;

public class InventoryViewModel extends AndroidViewModel {

    private InventoryRepository mRepo;

    private final LiveData<List<ItemEntity>> mItems;
    private final LiveData<List<String>> mItemNames;
    private final LiveData<List<CategoryEntity>> mCategories;
    private final LiveData<List<String>> mCategoryNames;
    private final LiveData<List<BrandEntity>> mBrands;
    private final LiveData<List<String>> mBrandNames;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        mRepo = new InventoryRepository(application);
        mItems = mRepo.getAllItems();
        mItemNames = mRepo.getAllItemNames();
        mCategories = mRepo.getAllCategories();
        mCategoryNames = mRepo.getAllCategoryNames();
        mBrands = mRepo.getAllBrands();
        mBrandNames = mRepo.getAllBrandNames();
    }

    public LiveData<List<ItemEntity>> getItems() { return mItems; }

    public LiveData<List<ItemEntity>> getItemsByCategory(int categoryId) {
        return mRepo.getItemsByCategory(categoryId);
    }

    public LiveData<List<String>> getItemNames() { return mItemNames; }

    public LiveData<List<CategoryEntity>> getCategories() { return mCategories; }

    public LiveData<List<String>> getCategoryNames() { return mCategoryNames; }

    public int getCategoryId(String categoryName) { return mRepo.getCategoryId(categoryName); }

    public LiveData<List<BrandEntity>> getBrands() { return mBrands; }

    public LiveData<List<String>> getBrandNames() { return mBrandNames; }

    public int getBrandId(String brandName) { return mRepo.getBrandId(brandName); }

    public void insertItem(ItemEntity item) { mRepo.insert(item); }
}
