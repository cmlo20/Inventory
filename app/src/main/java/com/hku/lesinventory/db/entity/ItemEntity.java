package com.hku.lesinventory.db.entity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hku.lesinventory.model.Item;

@Entity(tableName = "items",
        foreignKeys = {
                @ForeignKey(entity = BrandEntity.class,
                        parentColumns = "id",
                        childColumns = "brandId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = CategoryEntity.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "brandId"), @Index(value = "categoryId")
        })
public class ItemEntity implements Item {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int categoryId;
    private int brandId;
    private String name;
    private String description;
    private String imageUriString;
    @Ignore
    private int quantity;

    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public int getCategoryId() { return categoryId; }

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    @Override
    public int getBrandId() { return brandId; }

    public void setBrandId(int brandId) { this.brandId = brandId; }

    @Override
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @Override
    public String getImageUriString() { return imageUriString; }

    public void setImageUriString(String uriString) { this.imageUriString = uriString; }

    @Override
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public ItemEntity() {

    }

    @Ignore
    public ItemEntity(int categoryId, int brandId, String name, String description) {
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.name = name;
        this.description = description;
    }
}



