package com.hku.lesinventory.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hku.lesinventory.databinding.CategoryFragmentBinding;
import com.hku.lesinventory.db.entity.BrandEntity;
import com.hku.lesinventory.db.entity.ItemWithInstances;
import com.hku.lesinventory.viewmodel.CategoryViewModel;
import com.hku.lesinventory.viewmodel.InventoryViewModel;

import java.util.List;

/**
 * Display a list of inventory items of a specific category
 */
public class CategoryFragment extends Fragment {

    public static final String TAG = CategoryFragment.class.getName();

    private static final String KEY_CATEGORY_ID = "category_id";

//    private int mCategoryId;

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
        // Extract category ID from argument
        int categoryId = requireArguments().getInt(KEY_CATEGORY_ID);
        CategoryViewModel.Factory factory = new CategoryViewModel.Factory(
                requireActivity().getApplication(), categoryId);

        final CategoryViewModel model = new ViewModelProvider(this, factory)
                .get(CategoryViewModel.class);

//        final InventoryViewModel viewModel = new ViewModelProvider(this,
//                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()))
//                .get(InventoryViewModel.class);

        LiveData<List<ItemWithInstances>> itemsInCategory = model.getItems();
//        LiveData<List<ItemEntity>> itemsInCategory = viewModel.getItemsByCategory(mCategoryId);
        LiveData<List<BrandEntity>> allBrands = model.getBrands();
        subscribeUi(itemsInCategory, allBrands);
    }

    private void subscribeUi(LiveData<List<ItemWithInstances>> itemsLiveData,
                             LiveData<List<BrandEntity>> brandsLiveData) {
        // Update the list when data changes
        itemsLiveData.observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                mBinding.setIsLoading(false);
                mItemAdapter.setItemList(items);
                mItemAdapter.notifyDataSetChanged();
            } else {
                mBinding.setIsLoading(true);
            }
            mBinding.executePendingBindings();
        });

        brandsLiveData.observe(getViewLifecycleOwner(), brands -> {
            if (brands != null) {
                mItemAdapter.setBrandList(brands);
                mItemAdapter.notifyDataSetChanged();
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
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            Intent intent = new Intent(getActivity(), ItemActivity.class);
            intent.putExtra(ItemActivity.KEY_ITEM_ID, item.getId());
            startActivity(intent);
        }
    };

    /** Create category fragment for specific category ID (dependency injection) **/
    public static CategoryFragment forCategory(int categoryId) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

//    public void setCategoryId(int categoryId) {
//        mCategoryId = categoryId;
//    }
}