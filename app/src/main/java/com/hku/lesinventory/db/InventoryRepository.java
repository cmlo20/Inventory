package com.hku.lesinventory.db;

import androidx.lifecycle.LiveData;

import com.hku.lesinventory.db.dao.BrandDao;
import com.hku.lesinventory.db.dao.CategoryDao;
import com.hku.lesinventory.db.dao.InstanceDao;
import com.hku.lesinventory.db.dao.ItemDao;
import com.hku.lesinventory.db.dao.LocationDao;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.List;

/**
 *  Repository that fetch data from data sources using DAOs
 */
public class InventoryRepository {

    private static InventoryRepository sInstance;

    private final InventoryRoomDatabase mDatabase;

    private final AppExecutors mExecutors;

    private final ItemDao mItemDao;
    private final InstanceDao mInstanceDao;
    private final BrandDao mBrandDao;
    private final CategoryDao mCategoryDao;
    private final LocationDao mLocationDao;

    private final LiveData<List<ItemEntity>> mAllItems;
    private final LiveData<List<String>> mAllItemNames;
    private final LiveData<List<CategoryEntity>> mAllCategories;
    private final LiveData<List<String>> mAllCategoryNames;
    private final LiveData<List<BrandEntity>> mAllBrands;
    private final LiveData<List<String>> mAllBrandNames;
    private final LiveData<List<LocationEntity>> mAllLocations;
    private final LiveData<List<String>> mAllLocationNames;

    private InventoryRepository(final InventoryRoomDatabase database, final AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mItemDao = database.itemDao();
        mInstanceDao = database.instanceDao();
        mBrandDao = database.brandDao();
        mCategoryDao = database.categoryDao();
        mLocationDao = database.locationDao();

        mAllItems = mItemDao.getAllItems();
        mAllItemNames = mItemDao.getAllItemNames();
        mAllCategories = mCategoryDao.getAllCategories();
        mAllCategoryNames = mCategoryDao.getAllCategoryNames();
        mAllBrands = mBrandDao.getAllBrands();
        mAllBrandNames = mBrandDao.getAllBrandNames();
        mAllLocations = mLocationDao.getAllLocations();
        mAllLocationNames = mLocationDao.getAllLocationNames();
    }

    public static InventoryRepository getInstance(final InventoryRoomDatabase database,
                                                  final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (InventoryRepository.class) {
                if (sInstance == null) {
                    sInstance = new InventoryRepository(database, executors);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get LiveData from the inventory database
     */
    // Item Entity
    public LiveData<List<ItemEntity>> loadAllItems() {
        return mAllItems;
    }

    public LiveData<List<ItemEntity>> loadItemsByCategory(int categoryId) {
        return mItemDao.getItemsByCategory(categoryId);
    }

    public LiveData<ItemEntity> loadItem(final int itemId) {
        return mItemDao.getItemById(itemId);
    }

    public LiveData<List<String>> loadAllItemNames() { return mAllItemNames; }

    public LiveData<String> getImageUriString(int itemId) { return mItemDao.getItemImage(itemId); }

    // Instance Entity
    public LiveData<List<InstanceEntity>> loadItemInstances(final int itemId) {
        return mInstanceDao.loadItemInstances(itemId);
    }

    public LiveData<List<InstanceEntity>> loadAllInstances() { return mInstanceDao.loadAllInstances(); }

    // Category Entity
    public LiveData<List<CategoryEntity>> loadAllCategories() { return mAllCategories; }

    public LiveData<List<String>> loadAllCategoryNames() { return mAllCategoryNames; }

    public int getCategoryId(String categoryName) { return mCategoryDao.getIdByName(categoryName); }

    public LiveData<CategoryEntity> getItemCategory(int itemId) { return mItemDao.getItemCategory(itemId); }

    // Brand Entity
    public LiveData<List<BrandEntity>> loadAllBrands() { return mAllBrands; }

    public LiveData<List<String>> loadAllBrandNames() { return mAllBrandNames; }

    public LiveData<BrandEntity> getItemBrand(int itemId) { return mItemDao.getItemBrand(itemId); }

    public LiveData<String> getBrandName(int brandId) { return mBrandDao.getBrandName(brandId); }

    public int getBrandId(String brandName) { return mBrandDao.getIdByName(brandName); }

    // Location Entity
    public LiveData<List<LocationEntity>> loadAllLocations() { return mAllLocations; }

    public LiveData<List<String>> loadAllLocationNames() { return mAllLocationNames; }

    public int getLocationId(String locationName) { return mLocationDao.getIdByName(locationName); }


    public void insert(ItemEntity item) {
        mExecutors.diskIO().execute(() -> {     // Run on background thread
            mItemDao.insert(item);
        });
    }

    public void insert(InstanceEntity instance) {
        mExecutors.diskIO().execute(() -> {
            mInstanceDao.insert(instance);
        });
    }
}
