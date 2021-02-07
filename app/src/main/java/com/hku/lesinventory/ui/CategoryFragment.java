package com.hku.lesinventory.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.CategoryFragmentBinding;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.ItemEntity;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.util.List;

/**
 * Display a list of inventory items of a specific category
 */
public class CategoryFragment extends Fragment {

    public static final String TAG = CategoryFragment.class.getName();

    private int mCategoryId;

    private CategoryFragmentBinding mBinding;

    private ItemAdapter mItemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = CategoryFragmentBinding.inflate(inflater, container, false);

        // Create and set the adapter for the RecyclerView
        mItemAdapter = new ItemAdapter(mItemClickCallback);
        mBinding.itemList.setAdapter(mItemAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final InventoryViewModel viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
                .get(InventoryViewModel.class);

        LiveData<List<ItemEntity>> itemsInCategory = viewModel.getItemsByCategory(mCategoryId);
        subscribeUi(itemsInCategory, viewModel.getBrands());
    }

    private void subscribeUi(LiveData<List<ItemEntity>> itemsLiveData, LiveData<List<BrandEntity>> brandsLiveData) {
        // Update the list when data changes
        itemsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                mItemAdapter.setItemList(items);
            }
        });

        brandsLiveData.observe(getViewLifecycleOwner(), brands -> {
            if (brands != null) {
                mItemAdapter.setBrandList(brands);
            }
        });
    }


    @Override
    public void onDestroyView() {
        mBinding = null;
        mItemAdapter = null;
        super.onDestroyView();
    }

    private final ItemClickCallback mItemClickCallback = item -> {

    };

    public void setCategory(int categoryId) {
        this.mCategoryId = categoryId;
    }
}