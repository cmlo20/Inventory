package com.hku.lesinventory.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hku.lesinventory.db.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories")
    LiveData<List<CategoryEntity>> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    LiveData<CategoryEntity> getCategoryById(int categoryId);

    @Query("SELECT name FROM categories")
    LiveData<List<String>> getAllCategoryNames();

    @Query("SELECT id FROM categories WHERE name = :categoryName")
    int getIdByName(String categoryName);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("DELETE FROM categories")
    void deleteAll();
}
