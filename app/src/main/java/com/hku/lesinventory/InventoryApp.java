package com.hku.lesinventory;

import android.app.Application;

import com.hku.lesinventory.db.AppExecutors;
import com.hku.lesinventory.db.InventoryRepository;
import com.hku.lesinventory.db.InventoryRoomDatabase;

/**
 * Android Application class. Used for accessing singletons.
 */
public class InventoryApp extends Application {

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppExecutors = new AppExecutors();
    }

    public InventoryRoomDatabase getDatabase() { return InventoryRoomDatabase.getInstance(this, mAppExecutors); }

    public InventoryRepository getRepository() { return InventoryRepository.getInstance(getDatabase(), mAppExecutors); }
}
