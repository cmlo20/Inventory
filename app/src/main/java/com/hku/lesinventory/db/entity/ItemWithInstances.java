package com.hku.lesinventory.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ItemWithInstances {
    @Embedded public ItemEntity item;
    @Relation(
            parentColumn = "id",
            entityColumn = "itemId"
    )
    public List<InstanceEntity> instances;
}
