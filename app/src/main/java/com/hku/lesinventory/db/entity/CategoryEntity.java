package com.hku.lesinventory.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hku.lesinventory.model.Category;

@Entity(tableName = "categories",
        indices = {@Index(value = "name", unique = true)})
public class CategoryEntity implements Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;

    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public CategoryEntity() {

    }

    @Ignore
    public CategoryEntity(String name) {
        this.name = name;
    }


}