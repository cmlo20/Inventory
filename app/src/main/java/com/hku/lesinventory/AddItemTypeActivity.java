package com.hku.lesinventory;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddItemTypeActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Cursor cursor;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_type);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(this);
        try {
            db = inventoryDatabaseHelper.getReadableDatabase();
            cursor = db.query("CATEGORY",
                    new String[]{"_id", "NAME"},
                    null, null, null, null, "NAME ASC");
            Spinner categorySpinner = findViewById(R.id.item_category);
            SimpleCursorAdapter categoryAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1}, 0);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this,
                    "Database unavailable",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }

    /* Create a file for the photo */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onClickAddImage(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occured while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.hku.lesinventory", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else {
            Toast toast = Toast.makeText(AddItemTypeActivity.this,
                    "Camera not available", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            ImageButton itemImage = findViewById(R.id.item_image);
            int targetW = itemImage.getWidth();
            int targetH = itemImage.getHeight();
            int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            try {
                ExifInterface ei = new ExifInterface(currentPhotoPath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Bitmap rotatedBitmap = null;
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotatedBitmap = rotateImage(bitmap, 90);
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotatedBitmap = rotateImage(bitmap, 180);
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotatedBitmap = rotateImage(bitmap, 270);
                    default:
                        rotatedBitmap = bitmap;
                }
                itemImage.setImageBitmap(rotatedBitmap);
            } catch (IOException ioe) {
                // log message?
            }
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                                    matrix, true);
    }


    public void onClickSave(View view) {
        EditText name = findViewById(R.id.item_name);
        EditText description = findViewById(R.id.item_description);
        Spinner category = findViewById(R.id.item_category);
        ImageButton imageButton = findViewById(R.id.item_image);

        ContentValues itemValues = new ContentValues();
        itemValues.put("NAME", name.getText().toString());
        itemValues.put("DESCRIPTION", description.getText().toString());
        itemValues.put("CATEGORY", category.getSelectedItemId());

        //imageButton.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) imageButton.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] image = bos.toByteArray();
        itemValues.put("IMAGE", image);

        SQLiteOpenHelper inventoryDatabaseHelper = new InventoryDatabaseHelper(
                AddItemTypeActivity.this);
        try {
            SQLiteDatabase db = inventoryDatabaseHelper.getWritableDatabase();
            db.insert("ITEM", null, itemValues);
            db.close();
            Toast.makeText(AddItemTypeActivity.this, "Item added", Toast.LENGTH_SHORT).show();

        } catch (SQLiteException e) {
            Toast.makeText(AddItemTypeActivity.this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}