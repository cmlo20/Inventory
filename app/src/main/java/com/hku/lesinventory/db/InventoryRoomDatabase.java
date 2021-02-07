package com.hku.lesinventory.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hku.lesinventory.db.dao.BrandDao;
import com.hku.lesinventory.db.dao.CategoryDao;
import com.hku.lesinventory.db.dao.ItemDao;
import com.hku.lesinventory.db.dao.ItemInstanceDao;
import com.hku.lesinventory.db.dao.LocationDao;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.ItemInstanceEntity;
import com.hku.lesinventory.db.entity.LocationEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ItemEntity.class, BrandEntity.class, CategoryEntity.class, ItemInstanceEntity.class, LocationEntity.class},
        version = 1, exportSchema = false)
public abstract class InventoryRoomDatabase extends RoomDatabase {

    public abstract ItemDao itemDao();
    public abstract BrandDao brandDao();
    public abstract CategoryDao categoryDao();
    public abstract ItemInstanceDao itemInstanceDao();
    public abstract LocationDao locationDao();

    private static volatile InventoryRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static InventoryRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (InventoryRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            InventoryRoomDatabase.class, "inventory_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                CategoryDao categoryDao  = INSTANCE.categoryDao();
                categoryDao.deleteAll();

                CategoryEntity category = new CategoryEntity("Mic");
                categoryDao.insert(category);
                category = new CategoryEntity("VR");
                categoryDao.insert(category);

                BrandDao brandDao = INSTANCE.brandDao();
                brandDao.deleteAll();

                BrandEntity brand = new BrandEntity("Oculus");
                brandDao.insert(brand);
                brand = new BrandEntity("Sennheiser");
                brandDao.insert(brand);

                LocationDao locationDao = INSTANCE.locationDao();
                locationDao.deleteAll();

                LocationEntity location = new LocationEntity("CPD-2.74");
                locationDao.insert(location);
                location = new LocationEntity("CPD-2.76");
                locationDao.insert(location);
            });
        }
    };
}