package com.hku.lesinventory.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hku.lesinventory.model.Location;

@Entity(tableName = "locations",
        indices = {@Index(value = "name", unique = true)})
public class LocationEntity implements Location {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;

    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public LocationEntity() {

    }

    public LocationEntity(String name) {
        this.name = name;
    }
}