package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.BrandEntity;

import java.util.List;

@Dao
public interface BrandDao {

    @Query("SELECT * FROM brands")
    LiveData<List<BrandEntity>> getAllBrands();

    @Query("SELECT * FROM brands WHERE id = :brandId")
    LiveData<List<BrandEntity>> getBrandById(int brandId);

    @Query("SELECT name FROM brands")
    LiveData<List<String>> getAllBrandNames();

    @Query("SELECT id FROM brands WHERE name = :brandName")
    int getIdByName(String brandName);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(BrandEntity brand);

    @Update
    void update(BrandEntity brand);

    @Delete
    void delete(BrandEntity brand);

    @Query("DELETE FROM brands")
    void deleteAll();
}
