package com.hku.lesinventory.ui;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.hku.lesinventory.R;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.util.List;

public class InventoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = InventoryActivity.class.getName();

    private ViewPager pager;

    private InventoryViewModel inventoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_activity);
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

        inventoryViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(InventoryViewModel.class);

        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(), this);
        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        // Attach the ViewPager to the TabLayout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);

        // Set an observer to refresh the viewpager when data is updated
        inventoryViewModel.getCategories().observe(this, categories -> {
            pager.setAdapter(pagerAdapter);
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        Intent intent = null;

        switch(id) {
            case R.id.nav_inventory:

                break;
            case R.id.nav_rfidscan:
                //intent = new Intent(this, RFIDScanActivity.class);
                break;
            case R.id.nav_stocktaking:
                //intent = new Intent(this, StocktakingActivity.class);
                break;
            default:
        }
        if (intent != null) startActivity(intent);
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
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                Intent intent = new Intent(this, NewItemActivity.class);
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


    private class CategoryPagerAdapter extends FragmentPagerAdapter {

        private Context context;

        public CategoryPagerAdapter(FragmentManager fm, Context c) {
            super(fm);
            context = c;
        }

        @Override
        public int getCount() { // Return the number of categories
            List<CategoryEntity> categories = inventoryViewModel.getCategories().getValue();
            return categories == null ? 0 : categories.size();
        }

        @Override
        public Fragment getItem(int position) {
            List<CategoryEntity> categories = inventoryViewModel.getCategories().getValue();
            int categoryId = categories.get(position).getId();
            CategoryFragment categoryFragment = new CategoryFragment(categoryId);
            return categoryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            List<CategoryEntity> categories = inventoryViewModel.getCategories().getValue();
            String categoryName = categories.get(position).getName();
            return categoryName;
        }
    }
}