package com.hku.lesinventory.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.hku.lesinventory.db.converter.DateConverter;
import com.hku.lesinventory.db.dao.BrandDao;
import com.hku.lesinventory.db.dao.CategoryDao;
import com.hku.lesinventory.db.dao.InstanceDao;
import com.hku.lesinventory.db.dao.ItemDao;
import com.hku.lesinventory.db.dao.LocationDao;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.db.entity.LocationEntity;


@Database(entities = {ItemEntity.class, BrandEntity.class, CategoryEntity.class, InstanceEntity.class, LocationEntity.class},
        version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class InventoryRoomDatabase extends RoomDatabase {

    private static InventoryRoomDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "inventory-db";

    public abstract ItemDao itemDao();
    public abstract BrandDao brandDao();
    public abstract CategoryDao categoryDao();
    public abstract InstanceDao instanceDao();
    public abstract LocationDao locationDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static InventoryRoomDatabase getInstance(final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (InventoryRoomDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated(context);

                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static InventoryRoomDatabase buildDatabase(final Context appContext,
                                                       final AppExecutors executors) {
        return Room.databaseBuilder(appContext, InventoryRoomDatabase.class, DATABASE_NAME)
            .addCallback(new Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    executors.diskIO().execute(() ->{
                        InventoryRoomDatabase database = InventoryRoomDatabase.getInstance(appContext, executors);
                        // Pre-populate data for testing
                        CategoryDao categoryDao  = database.categoryDao();
                        categoryDao.deleteAll();
                        CategoryEntity category = new CategoryEntity("Mic");
                        categoryDao.insert(category);
                        category = new CategoryEntity("VR");
                        categoryDao.insert(category);

                        BrandDao brandDao = database.brandDao();
                        brandDao.deleteAll();
                        BrandEntity brand = new BrandEntity("Oculus");
                        brandDao.insert(brand);
                        brand = new BrandEntity("Sennheiser");
                        brandDao.insert(brand);

                        LocationDao locationDao = database.locationDao();
                        locationDao.deleteAll();
                        LocationEntity location = new LocationEntity("CPD-2.74");
                        locationDao.insert(location);
                        location = new LocationEntity("CPD-2.76");
                        locationDao.insert(location);

                        database.setDatabaseCreated();
                    });
                }
            }).build();
    }

    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated() { mIsDatabaseCreated.postValue(true); }

    public LiveData<Boolean> getDatabaseCreated() { return mIsDatabaseCreated; }
}