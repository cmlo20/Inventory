package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.List;

@Dao
public interface InstanceDao {

    @Query("SELECT * FROM instances")
    LiveData<List<InstanceEntity>> loadAllInstances();

    @Query("SELECT * FROM instances WHERE locationId = :locationId")
    LiveData<List<InstanceEntity>> loadInstancesInLocation(int locationId);

    @Query("SELECT * FROM instances WHERE rfidUii = :rfid")
    LiveData<InstanceEntity> getInstanceByRfid(String rfid);

    @Query("SELECT * FROM instances WHERE rfidUii = :rfid")
    InstanceEntity loadInstanceInBackground(String rfid);

    @Query("SELECT * FROM items WHERE id IN " +
            "(SELECT itemId FROM instances WHERE rfidUii = :rfid)")
    LiveData<ItemEntity> getItemByRfid(String rfid);

    @Query("SELECT * FROM categories WHERE id IN " +
            "(SELECT categoryId FROM items WHERE id IN" +
            "(SELECT itemId FROM instances WHERE rfidUii = :rfid))")
    LiveData<CategoryEntity> getItemCategoryByRfid(String rfid);

    @Query("SELECT * FROM locations WHERE id IN" +
            "(SELECT locationId FROM instances WHERE rfidUii = :rfid)")
    LiveData<LocationEntity> getLocationByRfid(String rfid);

    @Query("SELECT * FROM brands WHERE id IN " +
            "(SELECT brandId FROM items WHERE id IN " +
            "(SELECT itemId FROM instances WHERE rfidUii = :rfid))")
    LiveData<BrandEntity> getItemBrandByRfid(String rfid);

    @Query("SELECT * FROM instances WHERE itemId = :itemId")
    LiveData<List<InstanceEntity>> loadItemInstances(int itemId);


    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(InstanceEntity instances);

    @Update
    void update(InstanceEntity instance);

    @Delete
    void delete(InstanceEntity instance);
}
