package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.List;

@Dao
public interface LocationDao {

    @Query("SELECT * FROM locations ORDER BY name")
    LiveData<List<LocationEntity>> loadAllLocations();

    @Query("SELECT * FROM locations WHERE id = :locationId")
    LiveData<LocationEntity> loadLocation(int locationId);

    @Query("SELECT id FROM locations WHERE name = :locationName")
    int getIdByName(String locationName);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(LocationEntity location);

    @Update
    void update(LocationEntity location);

    @Delete
    void delete(LocationEntity location);
}
