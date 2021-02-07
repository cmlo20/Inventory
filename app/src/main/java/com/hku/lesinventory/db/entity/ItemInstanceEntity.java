package com.hku.lesinventory.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hku.lesinventory.model.ItemInstance;

@Entity(tableName = "itemInstances",
        foreignKeys = {
                @ForeignKey(entity = LocationEntity.class,
                        parentColumns = "id",
                        childColumns = "locationId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index(value = {"rfidUii", "barcode"}, unique = true),
                @Index(value = "locationId")
        })
public class ItemInstanceEntity implements ItemInstance {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int locationId;
    private String rfidUii;
    private String barcode;

    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public int getLocationId() { return locationId; }

    public void setLocationId(int locationId) { this.locationId = locationId; }

    @Override
    public String getRfidUii() { return rfidUii; }

    public void setRfidUii(String uii) { this.rfidUii = uii; }

    @Override
    public String getBarcode() { return barcode; }

    public void setBarcode(String barcode) { this.barcode = barcode; }

    public ItemInstanceEntity() {

    }

    @Ignore
    public ItemInstanceEntity(int id, int locationId, String rfidUii, String barcode) {
        this.id = id;
        this.locationId = locationId;
        this.rfidUii = rfidUii;
        this.barcode = barcode;
    }
}
