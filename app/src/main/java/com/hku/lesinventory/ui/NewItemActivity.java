package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.hku.lesinventory.AddItemTypeActivity;
import com.hku.lesinventory.R;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewItemActivity extends AppCompatActivity {

    public static final String TAG = NewItemActivity.class.getName();

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private Uri mPhotoUri;

    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private Spinner mBrandSpinner;
    private Spinner mCategorySpinner;
    private ImageButton mItemImage;
    private ProgressBar mLoadingIndicator;

    private InventoryViewModel mInventoryViewModel;

    private List<ItemEntity> mAllItems; // List of items stored in database (for form validation)
    private List<BrandEntity> mAllBrands;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_item_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInventoryViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(InventoryViewModel.class);

        mNameEditText = findViewById(R.id.et_name);
        mDescriptionEditText = findViewById(R.id.et_description);
        mBrandSpinner = findViewById(R.id.sp_brand);
        mCategorySpinner = findViewById(R.id.sp_category);
        mItemImage = findViewById(R.id.item_image);
        mLoadingIndicator = findViewById(R.id.loading_indicator);

        populateSpinnersWithData();

        mInventoryViewModel.getItems().observe(this, items -> {
            mAllItems = items;
        });
        mInventoryViewModel.getBrands().observe(this, brands -> {
            mAllBrands = brands;
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

    /* Create a file for the item photo */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,    /* prefix */
                ".jpg",     /* suffix */
                storageDir        /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onClickAddImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                mPhotoUri = FileProvider.getUriForFile(this,
                        "com.hku.lesinventory", photoFile);
//                Log.i(TAG, mPhotoUri.toString());
//                Log.i(TAG, mCurrentPhotoPath);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else {
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mItemImage.setImageBitmap(bitmap);
        }
    }

    private void populateSpinnersWithData() {
        final ArrayAdapter<String> brandSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        brandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBrandSpinner.setAdapter(brandSpinnerAdapter);

        final ArrayAdapter<String> categorySpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(categorySpinnerAdapter);

        mInventoryViewModel.getBrandNames().observe(this, brands -> {
            brandSpinnerAdapter.clear();
            brandSpinnerAdapter.addAll(brands);
            brandSpinnerAdapter.notifyDataSetChanged();
        });

        mInventoryViewModel.getCategoryNames().observe(this, categories -> {
            categorySpinnerAdapter.clear();
            categorySpinnerAdapter.addAll(categories);
            categorySpinnerAdapter.notifyDataSetChanged();
        });
    }

    private boolean formIsValid() {
        String newItemName = mNameEditText.getText().toString();
        String newItemBrand = mBrandSpinner.getSelectedItem().toString();

        if (newItemName.isEmpty()) {
            mNameEditText.setError(getString(R.string.warn_empty_item_name));
            return false;
        }
        // Check for duplicate item (and brand) name in the database
        if (mAllItems != null) {
            for (ItemEntity item : mAllItems) {
                if (newItemName.equals(item.getName())) {
                    if (getBrandId(newItemBrand) == item.getBrandId()) {
                        mNameEditText.setError(getString(R.string.warn_duplicate_item_name));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int getBrandId(String brandName) {
        if (mAllBrands != null) {
            for (BrandEntity brand : mAllBrands) {
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
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String category = mCategorySpinner.getSelectedItem().toString();
            String brand = mBrandSpinner.getSelectedItem().toString();
            String name = mNameEditText.getText().toString();
            String description = mDescriptionEditText.getText().toString();
            String photoUriString = mPhotoUri.toString();

            int categoryId = mInventoryViewModel.getCategoryId(category);
            int brandId = mInventoryViewModel.getBrandId(brand);

            ItemEntity newItem = new ItemEntity(categoryId, brandId, name, description, photoUriString);
            mInventoryViewModel.insertItem(newItem);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (success) {
                Toast.makeText(context, R.string.toast_item_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}