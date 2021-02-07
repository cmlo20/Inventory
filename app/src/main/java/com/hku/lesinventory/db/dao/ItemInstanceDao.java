package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.ItemInstanceEntity;
import com.hku.lesinventory.model.ItemInstance;

import java.util.List;

@Dao
public interface ItemInstanceDao {

    @Query("SELECT * FROM itemInstances")
    LiveData<List<ItemInstanceEntity>> loadAllItemInstances();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<ItemInstanceEntity> instances);

    @Query("SELECT * FROM itemInstances WHERE id = :instanceId")
    LiveData<List<ItemInstanceEntity>> loadItemInstance(int instanceId);

    @Update
    void update(ItemInstanceEntity instance);

    @Delete
    void delete(ItemInstanceEntity instance);
}
