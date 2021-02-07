package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.hku.lesinventory.R;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class NewItemActivity extends AppCompatActivity {

    public static final String TAG = NewItemActivity.class.getName();

    private EditText nameEditText;
    private EditText descriptionEditText;
    private Spinner brandSpinner;
    private Spinner categorySpinner;
    private ProgressBar loadingIndicator;

    private InventoryViewModel inventoryViewModel;

    private List<ItemEntity> allItems; // List of items stored in database (for form validation)
    private List<BrandEntity> allBrands;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_item_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inventoryViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(InventoryViewModel.class);

        nameEditText = findViewById(R.id.et_name);
        descriptionEditText = findViewById(R.id.et_description);
        brandSpinner = findViewById(R.id.sp_brand);
        categorySpinner = findViewById(R.id.sp_category);
        loadingIndicator = findViewById(R.id.pb_loading_indicator);

        populateSpinnersWithData();

        inventoryViewModel.getItems().observe(this, items -> {
            this.allItems = items;
        });
        inventoryViewModel.getBrands().observe(this, brands -> {
            this.allBrands = brands;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_item:
                if (formIsValid()) {
                    new SaveItemTask(this).execute();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateSpinnersWithData() {
        final ArrayAdapter<String> brandSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        brandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandSpinner.setAdapter(brandSpinnerAdapter);

        final ArrayAdapter<String> categorySpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        inventoryViewModel.getBrandNames().observe(this, brands -> {
            brandSpinnerAdapter.clear();
            brandSpinnerAdapter.addAll(brands);
            brandSpinnerAdapter.notifyDataSetChanged();
        });

        inventoryViewModel.getCategoryNames().observe(this, categories -> {
            categorySpinnerAdapter.clear();
            categorySpinnerAdapter.addAll(categories);
            categorySpinnerAdapter.notifyDataSetChanged();
        });
    }

    private boolean formIsValid() {
        String newItemName = nameEditText.getText().toString();
        String newItemBrand = brandSpinner.getSelectedItem().toString();

        if (newItemName.isEmpty()) {
            nameEditText.setError(getString(R.string.warn_empty_item_name));
            return false;
        }
        // Check for duplicate item (and brand) name in the database
        if (allItems != null) {
            for (ItemEntity item : allItems) {
                if (newItemName.equals(item.getName())) {
                    if (getBrandId(newItemBrand) == item.getBrandId()) {
                        nameEditText.setError(getString(R.string.warn_duplicate_item_name));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int getBrandId(String brandName) {
        if (allBrands != null) {
            for (BrandEntity brand : allBrands) {
                if (brandName.equals(brand.getName()))
                    return brand.getId();
            }
        }
        return -1;
    }

    public class SaveItemTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        public SaveItemTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String category = categorySpinner.getSelectedItem().toString();
            String brand = brandSpinner.getSelectedItem().toString();
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();

            int categoryId = inventoryViewModel.getCategoryId(category);
            int brandId = inventoryViewModel.getBrandId(brand);

            ItemEntity newItem = new ItemEntity(categoryId, brandId, name, description);
            inventoryViewModel.insertItem(newItem);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            loadingIndicator.setVisibility(View.INVISIBLE);
            if (success) {
                Toast.makeText(context, "Item Saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }
}