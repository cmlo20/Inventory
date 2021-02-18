package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.ItemWithInstances;

import java.util.List;

@Dao
public interface ItemDao {

    // Todo: Order items by brand names
    @Query("SELECT * FROM items ORDER BY brandId")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY brandId")
    LiveData<List<ItemEntity>> getItemsInCategory(int categoryId);

    @Transaction
    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY brandId")
    LiveData<List<ItemWithInstances>> getItemsWithInstances(int categoryId);

    @Query("SELECT name FROM items ORDER BY brandId")
    LiveData<List<String>> getAllItemNames();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<ItemEntity> items);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(ItemEntity item);

    @Query("SELECT * FROM items WHERE id = :itemId")
    LiveData<ItemEntity> getItemById(int itemId);

    @Query("SELECT * FROM brands WHERE id IN " +
            "(SELECT brandId FROM items WHERE id = :itemId)")
    LiveData<BrandEntity> getItemBrand(int itemId);

    @Query("SELECT * FROM categories WHERE id IN " +
            "(SELECT categoryId FROM items WHERE id = :itemId)")
    LiveData<CategoryEntity> getItemCategory(int itemId);

    @Query("SELECT imageUriString FROM items WHERE id = :itemId")
    LiveData<String> getItemImage(int itemId);

    @Query("SELECT COUNT(*) FROM instances WHERE itemId = :itemId")
    LiveData<Integer> getItemQuantity(int itemId);

    @Update
    void update(ItemEntity item);

    @Delete
    void delete(ItemEntity item);
}
