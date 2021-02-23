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
import com.hku.lesinventory.db.entity.ItemWithInstances;
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
    private final LiveData<List<CategoryEntity>> mAllCategories;
    private final LiveData<List<BrandEntity>> mAllBrands;
    private final LiveData<List<LocationEntity>> mAllLocations;

    private InventoryRepository(final InventoryRoomDatabase database, final AppExecutors executors) {
        mDatabase = database;
        mExecutors = executors;

        mItemDao = database.itemDao();
        mInstanceDao = database.instanceDao();
        mBrandDao = database.brandDao();
        mCategoryDao = database.categoryDao();
        mLocationDao = database.locationDao();

        mAllItems = mItemDao.getAllItems();
        mAllCategories = mCategoryDao.getAllCategories();
        mAllBrands = mBrandDao.getAllBrands();
        mAllLocations = mLocationDao.loadAllLocations();
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
    /*Item Entity*/
    public LiveData<List<ItemEntity>> loadAllItems() {
        return mAllItems;
    }

    public LiveData<ItemEntity> loadItem(final int itemId) {
        return mItemDao.getItemById(itemId);
    }

    public LiveData<String> getImageUriString(final int itemId) { return mItemDao.getItemImage(itemId); }

    public LiveData<Integer> getItemQuantity(final int itemId) { return mItemDao.getItemQuantity(itemId); }


    /*Instance Entity*/
    public LiveData<List<InstanceEntity>> loadItemInstances(final int itemId) {
        return mInstanceDao.loadItemInstances(itemId);
    }

    public LiveData<List<InstanceEntity>> loadAllInstances() { return mInstanceDao.loadAllInstances(); }

    public LiveData<List<InstanceEntity>> loadInstancesInLocation(final int locationId) {
        return mInstanceDao.loadInstancesInLocation(locationId);
    }

    public LiveData<InstanceEntity> loadInstanceByRfid(final String rfidUii) { return mInstanceDao.getInstanceByRfid(rfidUii); }

    public InstanceEntity loadInstanceInBackground(final String rfidUii) { return mInstanceDao.loadInstanceInBackground(rfidUii); }

    public LiveData<ItemEntity> loadInstanceItemByRfid(final String rfidUii) { return mInstanceDao.getItemByRfid(rfidUii); }

    public LiveData<CategoryEntity> loadInstanceCategoryByRfid(final String rfidUii) { return mInstanceDao.getItemCategoryByRfid(rfidUii); }

    public LiveData<LocationEntity> loadInstanceLocationByRfid(final String rfidUii) { return mInstanceDao.getLocationByRfid(rfidUii); }

    public LiveData<BrandEntity> loadInstanceBrandByRfid(final String rfidUii) { return mInstanceDao.getItemBrandByRfid(rfidUii); }


    /*Category Entity*/
    public LiveData<List<CategoryEntity>> loadAllCategories() { return mAllCategories; }

    public LiveData<CategoryEntity> loadCategory(final int categoryId) {
        return mCategoryDao.getCategoryById(categoryId);
    }

    public LiveData<List<ItemWithInstances>> loadItemsInCategory(int categoryId) {
        return mItemDao.getItemsWithInstances(categoryId);
    }

    public int getCategoryId(String categoryName) { return mCategoryDao.getIdByName(categoryName); }

    public LiveData<CategoryEntity> loadItemCategory(int itemId) { return mItemDao.getItemCategory(itemId); }


    /*Brand Entity*/
    public LiveData<List<BrandEntity>> loadAllBrands() { return mAllBrands; }

    public LiveData<BrandEntity> loadItemBrand(int itemId) { return mItemDao.getItemBrand(itemId); }

    public int getBrandId(String brandName) { return mBrandDao.getIdByName(brandName); }


    /*Location Entity*/
    public LiveData<List<LocationEntity>> loadAllLocations() { return mAllLocations; }

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

    public void update(InstanceEntity instance) {
        mExecutors.diskIO().execute(() -> {
            mInstanceDao.update(instance);
        });
    }

    public void insert(CategoryEntity category) {
        mExecutors.diskIO().execute(() -> {
            mCategoryDao.insert(category);
        });
    }

    public void insert(BrandEntity brand) {
        mExecutors.diskIO().execute(() -> {
            mBrandDao.insert(brand);
        });
    }

    public void insert(LocationEntity location) {
        mExecutors.diskIO().execute(() -> {
            mLocationDao.insert(location);
        });
    }
}
