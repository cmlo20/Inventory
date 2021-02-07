package com.hku.lesinventory.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.hku.lesinventory.db.dao.BrandDao;
import com.hku.lesinventory.db.dao.CategoryDao;
import com.hku.lesinventory.db.dao.ItemDao;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;

import java.util.List;

/**
 *  Repository that fetch data from data sources using DAOs
 */
public class InventoryRepository {

    private final ItemDao mItemDao;
    private final BrandDao mBrandDao;
    private final CategoryDao mCategoryDao;

    private final LiveData<List<ItemEntity>> mAllItems;
    private final LiveData<List<String>> mAllItemNames;
    private final LiveData<List<CategoryEntity>> mAllCategories;
    private final LiveData<List<String>> mAllCategoryNames;
    private final LiveData<List<BrandEntity>> mAllBrands;
    private final LiveData<List<String>> mAllBrandNames;

    public InventoryRepository(Application application) {
        InventoryRoomDatabase db = InventoryRoomDatabase.getDatabase(application);
        mItemDao = db.itemDao();
        mBrandDao = db.brandDao();
        mCategoryDao = db.categoryDao();

        mAllItems = mItemDao.getAllItems();
        mAllItemNames = mItemDao.getAllItemNames();
        mAllCategories = mCategoryDao.getAllCategories();
        mAllCategoryNames = mCategoryDao.getAllCategoryNames();
        mAllBrands = mBrandDao.getAllBrands();
        mAllBrandNames = mBrandDao.getAllBrandNames();
    }

    public LiveData<List<ItemEntity>> getAllItems() {
        return mAllItems;
    }

    public LiveData<List<ItemEntity>> getItemsByCategory(int categoryId) {
        return mItemDao.getItemsByCategory(categoryId);
    }

    public LiveData<List<String>> getAllItemNames() { return mAllItemNames; }

    public LiveData<List<CategoryEntity>> getAllCategories() { return mAllCategories; }

    public LiveData<List<String>> getAllCategoryNames() { return mAllCategoryNames; }

    public int getCategoryId(String categoryName) { return mCategoryDao.getIdByName(categoryName); }

    public LiveData<List<BrandEntity>> getAllBrands() { return mAllBrands; }

    public LiveData<List<String>> getAllBrandNames() { return mAllBrandNames; }

    public int getBrandId(String brandName) { return mBrandDao.getIdByName(brandName); }

    public void insert(ItemEntity item) {
        // Run on background thread
        InventoryRoomDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.insert(item);
        });
    }
}
