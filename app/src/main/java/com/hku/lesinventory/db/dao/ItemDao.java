package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.ItemEntity;

import java.util.List;

@Dao
public interface ItemDao {

    @Query("SELECT * FROM items")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM items WHERE categoryId = :categoryId")
    LiveData<List<ItemEntity>> getItemsByCategory(int categoryId);

    @Query("SELECT name FROM items")
    LiveData<List<String>> getAllItemNames();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<ItemEntity> items);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(ItemEntity item);

    @Query("SELECT * FROM items WHERE id = :itemId")
    LiveData<List<ItemEntity>> getItemById(int itemId);

    @Update
    void update(ItemEntity item);

    @Delete
    void delete(ItemEntity item);
}
