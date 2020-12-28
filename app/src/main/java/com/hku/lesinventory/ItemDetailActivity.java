package com.hku.lesinventory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

public class ItemDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_ID = "itemId";
    private int itemId;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            /* Hide the toolbar title when expanded */
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1)
                    scrollRange = appBarLayout.getTotalScrollRange();
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("LES Inventory");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        itemId = (Integer) getIntent().getExtras().get(EXTRA_ITEM_ID);
        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(this);
        try {
            SQLiteDatabase db = inventoryDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("ITEM",
                                    new String[] {"_id", "NAME", "IMAGE"},
                                    "_id = ?",
                                    new String[] {Integer.toString(itemId)},
                                    null, null, null);
            if (cursor.moveToFirst()) {
                String nameText = cursor.getString(1);
                byte[] imageByte = cursor.getBlob(2);
                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);

                // Populate views with data
                ImageView photo = findViewById(R.id.item_image);
                photo.setImageBitmap(image);
                photo.setContentDescription(nameText);
            }

            cursor = db.query("ITEMINSTANCE",
                    new String[] {"_id", "ITEM"},
                    "ITEM = ?",
                    new String[] {Integer.toString(itemId)},
                    null, null, null);
            int instanceQuantity = cursor.moveToFirst() ? cursor.getCount() : 0;

            InstancePagerAdapter pagerAdapter = new InstancePagerAdapter(getSupportFragmentManager(), this, instanceQuantity + 2);
            pager = findViewById(R.id.pager);
            pager.setAdapter(pagerAdapter);
            TabLayout tabLayout = findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(pager);

            cursor.close();
            db.close();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_item:
                Intent intent = new Intent(this, EditItemActivity.class);
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

    private class InstancePagerAdapter extends FragmentPagerAdapter {
        private Context context;
        private int pageCount;

        public InstancePagerAdapter(FragmentManager fm, Context c, int count) {
            super(fm);
            context = c;
            pageCount = count;
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                ItemDetailFragment itemInfoFrag = new ItemDetailFragment();
                itemInfoFrag.setItem(itemId);
                return itemInfoFrag;
            } else if (position == pageCount - 1) {     // add new item instance
                AddInstanceFragment addInstanceFrag = new AddInstanceFragment();
                addInstanceFrag.setItemType(itemId);
                return addInstanceFrag;
            } else {
                InstanceDetailFragment instanceFrag = new InstanceDetailFragment();
                instanceFrag.setItem(itemId);
                instanceFrag.setInstance(position);
                return instanceFrag;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Info";
            } else if (position == pageCount - 1) {
                return "Add";
            } else
                return "#" + Integer.toString(position);
        }
    }
}