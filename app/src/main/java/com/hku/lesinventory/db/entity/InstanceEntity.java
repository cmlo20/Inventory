package com.hku.lesinventory.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hku.lesinventory.model.Instance;

import java.util.Date;

@Entity(tableName = "instances",
        foreignKeys = {
                @ForeignKey(entity = LocationEntity.class,
                        parentColumns = "id",
                        childColumns = "locationId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = ItemEntity.class,
                        parentColumns = "id",
                        childColumns = "itemId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index(value = {"rfidUii", "serialNo"}, unique = true),
                @Index(value = "locationId"),
                @Index(value = "itemId")
        })
public class InstanceEntity implements Instance {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int itemId;
    private int locationId;
    private String rfidUii;
    private String serialNo;
    private Date checkedInAt;

    @Override
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    @Override
    public int getItemId() { return itemId; }

    public void setItemId(int itemId) { this.itemId = itemId; }

    @Override
    public int getLocationId() { return locationId; }

    public void setLocationId(int locationId) { this.locationId = locationId; }

    @Override
    public String getRfidUii() { return rfidUii; }

    public void setRfidUii(String uii) { this.rfidUii = uii; }

    @Override
    public String getSerialNo() { return serialNo; }

    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }

    @Override
    public Date getCheckedInAt() { return checkedInAt; }

    public void setCheckedInAt(Date checkedInAt) { this.checkedInAt = checkedInAt; }

    public InstanceEntity() {

    }

    @Ignore
    public InstanceEntity(int itemId, int locationId, String rfidUii, Date checkedInAt) {
        this.itemId = itemId;
        this.locationId = locationId;
        this.rfidUii = rfidUii;
        this.checkedInAt = checkedInAt;
//        this.serialNo = serialNo;
    }
}
