package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.ItemActivityBinding;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.viewmodel.ItemViewModel;

import java.io.IOException;

public class ItemActivity extends AppCompatActivity {

    static final String KEY_ITEM_ID = "item_id";

    private ItemActivityBinding mBinding;

    private InstanceAdapter mInstanceAdapter;

    private String mItemCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.item_activity);
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInstanceAdapter = new InstanceAdapter(mInstanceClickCallback);
        mBinding.instanceList.setAdapter(mInstanceAdapter);

        final int itemId = getIntent().getExtras().getInt(KEY_ITEM_ID);
        // Inject itemId into view model
        ItemViewModel.Factory factory = new ItemViewModel.Factory(getApplication(), itemId);
        final ItemViewModel itemViewModel = new ViewModelProvider(this, factory)
                .get(ItemViewModel.class);

        mBinding.addInstanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewInstanceActivity.class);
            intent.putExtra(KEY_ITEM_ID, itemId);
            startActivity(intent);
        });

        mBinding.setLifecycleOwner(this);
        mBinding.setItemViewModel(itemViewModel);
        subscribeToModel(itemViewModel);

        /* Set toolbar title and hide it when expanded */
        final CollapsingToolbarLayout collapsingToolbarLayout = mBinding.collapsingToolbarLayout;
        AppBarLayout appBarLayout = mBinding.appBarLayout;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1)
                    scrollRange = appBarLayout.getTotalScrollRange();
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mItemCategory);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_item:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void subscribeToModel(final ItemViewModel model) {
        // Observes instance list livedata and pass to adapter when it changes
        model.getInstances().observe(this, instanceEntities -> {
            if (instanceEntities != null) {
                mBinding.setIsLoading(false);
                mInstanceAdapter.setInstanceList(instanceEntities);
            } else {
                mBinding.setIsLoading(true);
            }
        });

        model.getLocations().observe(this, locationEntities -> {
            if (locationEntities != null) {
                mBinding.setIsLoading(false);
                mInstanceAdapter.setLocationList(locationEntities);
            } else {
                mBinding.setIsLoading(true);
            }
        });

        model.getImageUriString().observe(this, imageUriString -> {
            Uri imageUri = Uri.parse(imageUriString);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mBinding.itemImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        model.getItemCategory().observe(this, category -> {
            mItemCategory = category.getName();
        });
    }

    private final InstanceClickCallback mInstanceClickCallback = instance -> {
        // no-op
    };
}