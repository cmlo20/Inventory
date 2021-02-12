package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.InstanceEntity;

import java.util.List;

@Dao
public interface InstanceDao {

    @Query("SELECT * FROM instances")
    LiveData<List<InstanceEntity>> loadAllInstances();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(InstanceEntity instances);

    @Query("SELECT * FROM instances WHERE itemId = :itemId")
    LiveData<List<InstanceEntity>> loadItemInstances(int itemId);

    @Update
    void update(InstanceEntity instance);

    @Delete
    void delete(InstanceEntity instance);
}
