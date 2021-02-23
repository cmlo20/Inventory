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

public class ItemListViewModel extends AndroidViewModel {

    private final InventoryRepository mRepository;

    private final LiveData<List<ItemEntity>> mItems;
    private final LiveData<List<CategoryEntity>> mCategories;
    private final LiveData<List<BrandEntity>> mBrands;

    public ItemListViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((InventoryApp) application).getRepository();

        mItems = mRepository.loadAllItems();
        mCategories = mRepository.loadAllCategories();
        mBrands = mRepository.loadAllBrands();
    }

    /**
     * Expose LiveData query so the UI can observe it
     */
    public LiveData<List<ItemEntity>> loadItems() { return mItems; }

    public LiveData<List<CategoryEntity>> loadCategories() { return mCategories; }

    public int getCategoryId(String categoryName) { return mRepository.getCategoryId(categoryName); }

    public LiveData<List<BrandEntity>> loadBrands() { return mBrands; }

    public int getBrandId(String brandName) { return mRepository.getBrandId(brandName); }


    public void insertItem(ItemEntity item) { mRepository.insert(item); }

    public void insertCategory(CategoryEntity category) { mRepository.insert(category); }

    public void insertBrand(BrandEntity brand) { mRepository.insert(brand); }
}
