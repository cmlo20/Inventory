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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.NewItemActivityBinding;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.CategoryEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.viewmodel.ItemListViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewItemActivity extends AppCompatActivity
        implements View.OnClickListener, NewOptionDialogFragment.NewOptionDialogListener {

    public static final String TAG = NewItemActivity.class.getName();
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_PICK_IMAGE = 2;

    private String mCurrentPhotoPath;
    private Uri mPhotoUri;
    private File mPhotoFile;

    private NewItemActivityBinding mBinding;

    private ItemListViewModel mItemListViewModel;

    private List<ItemEntity> mAllItems;     // List of items stored in database, used for input validation
    private List<BrandEntity> mAllBrands;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = NewItemActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.collapsingToolbarLayout.setTitleEnabled(false);    // Disable custom toolbar title to display activity label in toolbar
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.selectImageButton.setOnClickListener(this);
        mBinding.itemImageButton.setOnClickListener(this);
        mBinding.addCategoryButton.setOnClickListener(this);
        mBinding.addBrandButton.setOnClickListener(this);
        mBinding.saveItemButton.setOnClickListener(this);

        try {
            mPhotoFile = createImageFile();
            mPhotoUri = FileProvider.getUriForFile(this,
                    "com.hku.lesinventory", mPhotoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mItemListViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ItemListViewModel.class);

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

        mItemListViewModel.loadBrands().observe(this, brands -> {
            mAllBrands = brands;
            brandSpinnerAdapter.clear();
            for (BrandEntity brand : brands) {
                brandSpinnerAdapter.add(brand.getName());
            }
            brandSpinnerAdapter.notifyDataSetChanged();
        });

        mItemListViewModel.loadCategories().observe(this, categories -> {
            categorySpinnerAdapter.clear();
            for (CategoryEntity category : categories) {
                categorySpinnerAdapter.add(category.getName());
            }
            categorySpinnerAdapter.notifyDataSetChanged();
        });

        mItemListViewModel.loadItems().observe(this, items -> {
            mAllItems = items;
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
                mItemListViewModel.insertCategory(newCategory);
                Toast.makeText(this, R.string.toast_category_saved, Toast.LENGTH_SHORT).show();
                break;

            case R.string.title_new_brand:
                BrandEntity newBrand = new BrandEntity(newOptionName);
                mItemListViewModel.insertBrand(newBrand);
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
            case R.id.select_image_button:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
                }
                break;

            case R.id.item_image_button:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    if (mPhotoFile != null) {
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
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {    // Pick image
                Uri imageUri = data.getData();
                // Write data to photo file in internal storage
                int maxBufferSize = 1 * 1024 * 1024;
                try (InputStream is = getContentResolver().openInputStream(imageUri);
                     FileOutputStream fos = new FileOutputStream(mPhotoFile)){
                    int bytesAvailable = is.available();
                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    final byte[] buffers = new byte[bufferSize];
                    int read = 0;
                    while ((read = is.read(buffers)) != -1) {
                        fos.write(buffers, 0, read);
                    }
                    // Display selected image
                    mBinding.itemImageButton.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {   // Capture image
                Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                mBinding.itemImageButton.setImageBitmap(bitmap);
            }
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

            int categoryId = mItemListViewModel.getCategoryId(category);
            int brandId = mItemListViewModel.getBrandId(brand);

            ItemEntity newItem = new ItemEntity(categoryId, brandId, name, description, photoUriString);
            mItemListViewModel.insertItem(newItem);

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