package com.hku.lesinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

public class EditItemActivity extends AppCompatActivity
{
    public static final String EXTRA_ITEM_ID = "itemId";
    private long itemId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        itemId = (Integer) getIntent().getExtras().get(EXTRA_ITEM_ID);
        AddInstanceFragment frag = new AddInstanceFragment();
        frag.setItemType(itemId);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.edit_instance_frame, frag);
        ft.commit();
    }
}