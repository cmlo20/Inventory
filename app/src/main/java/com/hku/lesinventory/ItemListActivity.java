package com.hku.lesinventory;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class ItemListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private SQLiteOpenHelper inventoryDatabaseHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                                                                 drawer,
                                                                 toolbar,
                                                                 R.string.nav_open_drawer,
                                                                 R.string.nav_close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        inventoryDatabaseHelper = new InventoryDatabaseHelper(this);
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(), this);
        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);

        // Attach the ViewPager to the TabLayout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        Intent intent = null;

        switch(id) {
            case R.id.nav_additemtype:
                intent = new Intent(this, AddItemTypeActivity.class);
                break;
            case R.id.nav_addcategory:
                intent = new Intent(this, AddCategoryActivity.class);
                break;
            default:
        }
        if (intent != null)
            startActivity(intent);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                Intent intent = new Intent(this, AddInstanceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        pager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }


    private class CategoryPagerAdapter extends FragmentPagerAdapter {
        private Context context;

        public CategoryPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            context = c;
        }

        @Override
        public int getCount() {     // Return the number of categories
            try {
                db = inventoryDatabaseHelper.getReadableDatabase();
                cursor = db.query(true,
                        "CATEGORY",
                        new String[]{"_id", "NAME"},
                        null, null, null,
                        null, "NAME ASC", null);
            } catch (SQLiteException e) {
                Toast toast = Toast.makeText(context, "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
            return cursor.getCount();
        }

        @Override
        public Fragment getItem(int position) {
            int categoryId;
            if (cursor.moveToFirst()) {
                for (int i = 0; i <= position; i++) {
                    if (i == position) {
                        categoryId = cursor.getInt(0);
                        ItemFragment itemFrag = new ItemFragment();
                        itemFrag.setCategory(categoryId);     // Pass the category info to the fragment
                        return itemFrag;
                    } else if (!cursor.moveToNext())
                        break;
                }
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (cursor.moveToFirst()) {
                for (int i = 0; i <= position; i++) {
                    if (i == position) {
                        return cursor.getString(1);
                    } else if (!cursor.moveToNext())
                        break;
                }
            }
            return null;
        }
    }
}