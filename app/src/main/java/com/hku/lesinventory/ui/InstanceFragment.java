package com.hku.lesinventory.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.InstanceFragmentBinding;
import com.hku.lesinventory.viewmodel.InstanceViewModel;

import java.io.IOException;

public class InstanceFragment extends Fragment {

    private static final String KEY_RFID_UII = "rfid_uii";

    private InstanceFragmentBinding mBinding;

    public InstanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the databinding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.instance_fragment, container, false);
        mBinding.setIsLoading(true);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        InstanceViewModel.Factory factory = new InstanceViewModel.Factory(
                requireActivity().getApplication(), requireArguments().getString(KEY_RFID_UII));

        final InstanceViewModel model = new ViewModelProvider(this, factory)
                .get(InstanceViewModel.class);

        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setInstanceViewModel(model);
        mBinding.setIsLoading(false);

        subscribeToModel(model);
    }

    private void subscribeToModel(final InstanceViewModel model) {
        // Set item image
        model.getItem().observe(getViewLifecycleOwner(), itemEntity -> {
            if (itemEntity != null) {
                mBinding.setHasRecord(true);

                String imageUriString = itemEntity.getImageUriString();
                if (imageUriString != null) {
                    Uri imageUri = Uri.parse(imageUriString);
                    try {
                        Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        mBinding.itemImage.setImageBitmap(imageBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {    // no record in database
                mBinding.setHasRecord(false);
            }
        });
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param rfidUii The RFID UII of the item instance to display
     * @return A new instance of fragment InstanceFragment.
     */
    public static InstanceFragment forInstance(String rfidUii) {
        InstanceFragment fragment = new InstanceFragment();
        Bundle args = new Bundle();
        args.putString(KEY_RFID_UII, rfidUii);
        fragment.setArguments(args);
        return fragment;
    }
}