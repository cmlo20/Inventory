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

    @Query("SELECT * FROM locations")
    LiveData<List<LocationEntity>> getAllLocations();

    @Query("SELECT * FROM locations WHERE id = :locationId")
    LiveData<LocationEntity> getLocation(int locationId);

    @Query("SELECT name FROM locations")
    LiveData<List<String>> getAllLocationNames();

    @Query("SELECT id FROM locations WHERE name = :locationName")
    int getIdByName(String locationName);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertAll(List<LocationEntity> locations);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(LocationEntity location);

    @Update
    void update(LocationEntity location);

    @Delete
    void delete(LocationEntity location);

    @Query("DELETE FROM locations")
    void deleteAll();
}
