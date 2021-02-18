package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.NewItemActivityBinding;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewItemActivity extends AppCompatActivity
        implements View.OnClickListener, NewOptionDialogFragment.NewOptionDialogListener {

    public static final String TAG = NewItemActivity.class.getName();

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mCurrentPhotoPath;
    private Uri mPhotoUri;

    private NewItemActivityBinding mBinding;

    private InventoryViewModel mInventoryViewModel;

    private List<ItemEntity> mAllItems;     // List of items stored in database, used for input validation
    private List<BrandEntity> mAllBrands;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = NewItemActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.itemImageButton.setOnClickListener(this);
        mBinding.addCategoryButton.setOnClickListener(this);
        mBinding.addBrandButton.setOnClickListener(this);
        mBinding.saveItemButton.setOnClickListener(this);

        mInventoryViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(InventoryViewModel.class);

        subscribeToModel();
    }

    private void subscribeToModel() {
        // Create adapters for brand and category spinners
        ArrayAdapter<String> brandSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        brandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.brandSpinner.setAdapter(brandSpinnerAdapter);

        ArrayAdapter<String> categorySpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.categorySpinner.setAdapter(categorySpinnerAdapter);

        mInventoryViewModel.getItems().observe(this, items -> {
            mAllItems = items;
        });

        mInventoryViewModel.getBrands().observe(this, brands -> {
            mAllBrands = brands;
            brandSpinnerAdapter.clear();
            for (BrandEntity brand : brands) {
                brandSpinnerAdapter.add(brand.getName());
            }
            brandSpinnerAdapter.notifyDataSetChanged();
        });

        mInventoryViewModel.getCategories().observe(this, categories -> {
            categorySpinnerAdapter.clear();
            for (CategoryEntity category : categories) {
                categorySpinnerAdapter.add(category.getName());
            }
            categorySpinnerAdapter.notifyDataSetChanged();
        });
    }

    private boolean formIsValid() {
        String newItemName = mBinding.nameEdittext.getText().toString();
        String newItemBrand = mBinding.brandSpinner.getSelectedItem().toString();

        if (newItemName.isEmpty()) {
            mBinding.nameEdittext.setError(getString(R.string.warn_empty_item_name));
            return false;
        }
        // Check for duplicate item (and brand) name in the database
        if (mAllItems != null) {
            for (ItemEntity item : mAllItems) {
                if (newItemName.equals(item.getName())) {
                    if (getBrandId(newItemBrand) == item.getBrandId()) {
                        mBinding.nameEdittext.setError(getString(R.string.warn_duplicate_item_name));
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

    @Override
    public void onDialogPositiveClick(NewOptionDialogFragment dialog) {
        int dialogTitleId = dialog.getTitleId();
        EditText nameEditText = dialog.getDialog().findViewById(R.id.new_option_name);
        String newOptionName = nameEditText.getText().toString();

        switch (dialogTitleId) {
            case R.string.title_new_category:
                CategoryEntity newCategory = new CategoryEntity(newOptionName);
                mInventoryViewModel.insertCategory(newCategory);
                Toast.makeText(this, R.string.toast_category_saved, Toast.LENGTH_SHORT).show();
                break;

            case R.string.title_new_brand:
                BrandEntity newBrand = new BrandEntity(newOptionName);
                mInventoryViewModel.insertBrand(newBrand);
                Toast.makeText(this, R.string.toast_brand_saved, Toast.LENGTH_SHORT).show();
                break;

            default:

        }
    }

    @Override
    public void onDialogNegativeClick(NewOptionDialogFragment dialog) {
        // no-op
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_image_button:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        // Error occurred while creating the File
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(this,
                                "com.hku.lesinventory", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.add_category_button:
                DialogFragment newCategoryDialog = new NewOptionDialogFragment(R.string.title_new_category);
                newCategoryDialog.show(getSupportFragmentManager(), String.valueOf(R.string.title_new_category));
                break;

            case R.id.add_brand_button:
                DialogFragment newBrandDialog = new NewOptionDialogFragment(R.string.title_new_brand);
                newBrandDialog.show(getSupportFragmentManager(), String.valueOf(R.string.title_new_brand));
                break;

            case R.id.save_item_button:
                if (formIsValid()) {
                    new SaveItemTask(this).execute();
                }
                break;

            default:
                return;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mBinding.itemImageButton.setImageBitmap(bitmap);
        }
    }

    public class SaveItemTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        public SaveItemTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBinding.loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String category = mBinding.categorySpinner.getSelectedItem().toString();
            String brand = mBinding.brandSpinner.getSelectedItem().toString();
            String name = mBinding.nameEdittext.getText().toString();
            String description = mBinding.descriptionEdittext.getText().toString();
            String photoUriString = mPhotoUri == null ? null : mPhotoUri.toString();

            int categoryId = mInventoryViewModel.getCategoryId(category);
            int brandId = mInventoryViewModel.getBrandId(brand);

            ItemEntity newItem = new ItemEntity(categoryId, brandId, name, description, photoUriString);
            mInventoryViewModel.insertItem(newItem);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mBinding.loadingIndicator.setVisibility(View.INVISIBLE);
            if (success) {
                Toast.makeText(context, R.string.toast_item_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}