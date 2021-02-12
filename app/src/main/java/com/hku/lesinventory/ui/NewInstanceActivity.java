package com.hku.lesinventory.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.content.AsyncTaskLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.NewInstanceActivityBinding;
import com.hku.lesinventory.db.entity.InstanceEntity;
import com.hku.lesinventory.viewmodel.ItemViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewInstanceActivity extends AppCompatActivity {

    static final String KEY_ITEM_ID = "item_id";

    private int mItemId;

    private ItemViewModel mItemViewModel;

    private NewInstanceActivityBinding mBinding;

    private List<InstanceEntity> mAllInstances;     // instance list used for form validation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.new_instance_activity);
        setSupportActionBar(mBinding.toolbar.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItemId = getIntent().getExtras().getInt(KEY_ITEM_ID);
        // Inject itemId into view model
        ItemViewModel.Factory factory = new ItemViewModel.Factory(getApplication(), mItemId);
        mItemViewModel = new ViewModelProvider(this, factory)
                .get(ItemViewModel.class);

        mBinding.setLifecycleOwner(this);
        mBinding.setItemViewModel(mItemViewModel);

        populateLocationSpinner(mBinding.locationSpinner);
        // Display item image
        mItemViewModel.getImageUriString().observe(this, imageUriString -> {
            Uri imageUri = Uri.parse(imageUriString);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mBinding.itemImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mItemViewModel.getInstances().observe(this, instances -> {
            mAllInstances = instances;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
                    new SaveInstanceTask(this).execute();
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateLocationSpinner(Spinner spinner) {
        final ArrayAdapter<String> locationSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        locationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(locationSpinnerAdapter);
        // Observer location livedata from view model
        mItemViewModel.getLocationNames().observe(this, locations -> {
            locationSpinnerAdapter.clear();
            locationSpinnerAdapter.addAll(locations);
            locationSpinnerAdapter.notifyDataSetChanged();
        });
    }

    private boolean formIsValid() {
        String rfidUii = mBinding.rfidEdittext.getText().toString();
        if (rfidUii.isEmpty()) {
            mBinding.rfidEdittext.setError(getString(R.string.warn_empty_rfid));
            return false;
        }

        if (mAllInstances != null) {
            for (InstanceEntity instance : mAllInstances) {
                if (rfidUii.equals(instance.getRfidUii())) {
                    mBinding.rfidEdittext.setError(getString(R.string.warn_duplicate_rfid));
                    return false;
                }
            }
        }
        return true;
    }

    public class SaveInstanceTask extends AsyncTask<Void, Void, Boolean> {

        private Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBinding.loadingIndicator.setVisibility(View.VISIBLE);
        }

        public SaveInstanceTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String rfidUii = mBinding.rfidEdittext.getText().toString();
            String location = mBinding.locationSpinner.getSelectedItem().toString();
            int locationId = mItemViewModel.getLocationId(location);

            InstanceEntity newInstance = new InstanceEntity(mItemId, locationId, rfidUii);
            mItemViewModel.insertInstance(newInstance);

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