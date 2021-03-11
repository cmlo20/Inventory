package com.hku.lesinventory.ui;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.densowave.scannersdk.Common.CommException;
import com.densowave.scannersdk.Common.CommManager;
import com.densowave.scannersdk.Common.CommScanner;
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener;
import com.hku.lesinventory.BaseActivity;
import com.hku.lesinventory.R;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.LocationEntity;
import com.hku.lesinventory.model.Option;
import com.hku.lesinventory.viewmodel.ItemListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

// Todo: Searching function by field e.g. category, brand, location, name, ...
public class MainActivity extends BaseActivity
        implements ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener,
        ScannerAcceptStatusListener {

    public static final String TAG = MainActivity.class.getName();
    public static final String serviceKey = "serviceParam";

//    private ViewPager mPager;
//    private NavigationView mNavigationView;
    private ExpandableListView mExpandableListView;
    private List<NavMenuGroupItem> mExpandableListGroupItems;
    private HashMap<String, List<Option>> mExpandableListDetails;

    private ItemListViewModel mItemListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setTopActivity(true);
        super.startService();   // start the service to automatically close connection to RFID reader

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

        mExpandableListView = findViewById(R.id.nav_expandable_list);
        mExpandableListView.setOnChildClickListener(this);
        mExpandableListView.setOnGroupClickListener(this);
//        mNavigationView = findViewById(R.id.nav_view);
//        mNavigationView.setNavigationItemSelectedListener(this);

        mItemListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ItemListViewModel.class);

//        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager(),
//                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        mPager = findViewById(R.id.pager);
//        mPager.setAdapter(pagerAdapter);
//        // Attach the ViewPager to the TabLayout
//        TabLayout tabLayout = findViewById(R.id.tabs);
//        tabLayout.setupWithViewPager(mPager);

        subscribeToModel();
    }

    @Override
    public void onStart() {
        super.onStart();
//        mNavigationView.setCheckedItem(R.id.nav_item_list);
    }

    @Override
    public void onRestart() {
        super.onRestart();
//        mPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!super.isCommScanner()) {
            // Start connecting to RFID reader
            CommManager.addAcceptStatusListener(this);
            CommManager.startAccept();
            Toast.makeText(this, R.string.waiting_for_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CommManager.endAccept();
        CommManager.removeAcceptStatusListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (super.isCommScanner()) {
            super.disconnectCommScanner();
        }
        CommManager.endAccept();
    }

//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        Fragment fragment = null;
//        Intent intent = null;
//
//        switch(id) {
//            case R.id.nav_item_list:
//                break;
//            case R.id.nav_rfidscan:
//                intent = new Intent(this, RfidScanActivity.class);
//                break;
//            case R.id.nav_stocktaking:
//                intent = new Intent(this, StocktakingActivity.class);
//                break;
//            default:
//        }
//        CommManager.endAccept();
//        CommManager.removeAcceptStatusListener(this);
//
//        if (intent != null) startActivity(intent);
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

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
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_add_item:
                intent = new Intent(this, NewItemActivity.class);
                break;
        }
        CommManager.endAccept();
        CommManager.removeAcceptStatusListener(this);

        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnScannerAppeared(CommScanner commScanner) {
        try {
            commScanner.claim();
            CommManager.endAccept();
            CommManager.removeAcceptStatusListener(this);
        } catch (CommException e) {
            e.printStackTrace();
        }

        try {
            super.setConnectedCommScanner(commScanner);
            mCommScanner = getCommScanner();
            runOnUiThread(() -> {
                Toast.makeText(this, getString(R.string.rfid_reader_connected, mCommScanner.getBTLocalName()), Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeToModel() {
        final List<Option> categories = new ArrayList<>();
        final List<Option> brands = new ArrayList<>();
        final List<Option> locations = new ArrayList<>();

        mExpandableListDetails = new LinkedHashMap<>();
        mExpandableListDetails.put(NavMenu.KEY_CATEGORIES, categories);
        mExpandableListDetails.put(NavMenu.KEY_BRANDS, brands);
        mExpandableListDetails.put(NavMenu.KEY_LOCATIONS, locations);

        mExpandableListGroupItems = NavMenu.getGroupItems();
        ExpandableListAdapter adapter = new ExpandableListAdapter(this, mExpandableListGroupItems, mExpandableListDetails);
        mExpandableListView.setAdapter(adapter);

        mItemListViewModel.loadCategories().observe(this, categoryEntities -> {
            categories.clear();
            for (CategoryEntity category : categoryEntities) {
                categories.add(category);
                adapter.notifyDataSetChanged();
            }
        });

        mItemListViewModel.loadBrands().observe(this, brandEntities -> {
            brands.clear();
            for (BrandEntity brand : brandEntities) {
                brands.add(brand);
                adapter.notifyDataSetChanged();
            }
        });

        mItemListViewModel.loadLocations().observe(this, locationEntities -> {
            locations.clear();
            for (LocationEntity location : locationEntities) {
                locations.add(location);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Intent intent = null;
        switch (mExpandableListGroupItems.get(groupPosition).getName()) {
            case NavMenu.KEY_SCANITEM:
                intent = new Intent(this, RfidScanActivity.class);
                break;
            case NavMenu.KEY_STOCKTAKING:
                intent = new Intent(this, StocktakingActivity.class);
                break;
            default:
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,int childPosition, long id) {
        Fragment fragment = null;
        switch (mExpandableListGroupItems.get(groupPosition).getName()) {
            case NavMenu.KEY_CATEGORIES:
                int categoryId = mExpandableListDetails.get(NavMenu.KEY_CATEGORIES).get(childPosition).getId();
                fragment = CategoryFragment.forCategory(categoryId);
                break;
            case NavMenu.KEY_BRANDS:

                break;
            case NavMenu.KEY_LOCATIONS:

                break;
            default:
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack("fragment")
                    .replace(R.id.fragment_container, fragment, null)
                    .commit();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    static class NavMenu {
        // Expandable List menu items
        static final String KEY_CATEGORIES = "Categories";
        static final String KEY_BRANDS = "Brands";
        static final String KEY_LOCATIONS = "Locations";
        static final String KEY_SCANITEM = "Scan Item";
        static final String KEY_STOCKTAKING = "Stocktaking";

        static public List<NavMenuGroupItem> getGroupItems() {
            NavMenuGroupItem categories = new NavMenuGroupItem(KEY_CATEGORIES, R.drawable.baseline_category_black_24);
            NavMenuGroupItem brands = new NavMenuGroupItem(KEY_BRANDS, R.drawable.baseline_view_list_black_24);
            NavMenuGroupItem locations = new NavMenuGroupItem(KEY_LOCATIONS, R.drawable.round_location_on_black_24);
            NavMenuGroupItem scanItem = new NavMenuGroupItem(KEY_SCANITEM, R.drawable.baseline_qr_code_scanner_black_24);
            NavMenuGroupItem stocktaking = new NavMenuGroupItem(KEY_STOCKTAKING, R.drawable.baseline_account_balance_black_24);

            return Arrays.asList(categories, brands, locations, scanItem, stocktaking);
        }
    }

//    private class CategoryPagerAdapter extends FragmentPagerAdapter {
//
//        public CategoryPagerAdapter(@NonNull FragmentManager fm, int behavior) {
//            super(fm, behavior);
//        }
//
//        @Override
//        public int getCount() { // Return the number of categories
//            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
//            return categories == null ? 0 : categories.size();
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
//            int categoryId = categories.get(position).getId();
//            CategoryFragment categoryFragment = CategoryFragment.forCategory(categoryId);
////            CategoryFragment categoryFragment = new CategoryFragment();
////            categoryFragment.setCategoryId(categoryId);
//            return categoryFragment;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            List<CategoryEntity> categories = mItemListViewModel.loadCategories().getValue();
//            String categoryName = categories.get(position).getName();
//            return categoryName;
//        }
//    }
}