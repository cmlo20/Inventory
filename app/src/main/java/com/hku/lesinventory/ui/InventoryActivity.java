package com.hku.lesinventory.ui;

import androidx.annotation.NonNull;
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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.hku.lesinventory.R;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.viewmodel.ItemListViewModel;

import java.util.List;

// Todo: Navigation menu revamp
public class InventoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = InventoryActivity.class.getName();

    private ViewPager mPager;
    private NavigationView mNavigationView;

    private ItemListViewModel mItemListViewModel;

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
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mItemListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ItemListViewModel.class);

        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(pagerAdapter);
        // Attach the ViewPager to the TabLayout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mPager);

        // Todo: Fix: Category pages out of order when new one is created while sorted by name
        // Set an observer to refresh the viewpager when data is updated
        mItemListViewModel.loadCategories().observe(this, categories -> {
            pagerAdapter.notifyDataSetChanged();
            mPager.setAdapter(pagerAdapter);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mNavigationView.setCheckedItem(R.id.nav_item_list);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        Intent intent = null;

        switch(id) {
            case R.id.nav_item_list:

                break;
            case R.id.nav_rfidscan:
                intent = new Intent(this, RfidScanActivity.class);
                break;
            case R.id.nav_stocktaking:
                intent = new Intent(this, StocktakingActivity.class);
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
        mPager.getAdapter().notifyDataSetChanged();
    }


    private class CategoryPagerAdapter extends FragmentPagerAdapter {

        public CategoryPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public int getCount() { // Return the number of categories
            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
            return categories == null ? 0 : categories.size();
        }

        @Override
        public Fragment getItem(int position) {
            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
            int categoryId = categories.get(position).getId();
            CategoryFragment categoryFragment = CategoryFragment.forCategory(categoryId);
//            CategoryFragment categoryFragment = new CategoryFragment();
//            categoryFragment.setCategoryId(categoryId);
            return categoryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
            String categoryName = categories.get(position).getName();
            return categoryName;
        }
    }
}