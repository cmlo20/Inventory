package com.hku.lesinventory.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;

import com.hku.lesinventory.R;

import java.util.List;

public class MissingItemsDialogFragment extends DialogFragment {

    public static final String TAG = MissingItemsDialogFragment.class.getName();

    private final List<String> mMissingItemList;

    public MissingItemsDialogFragment(List<String> itemList) { mMissingItemList = itemList; }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.missing_instances_dialog, null);

        ListView missingItemsList = rootView.findViewById(R.id.missing_items_list);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_list_item_1, mMissingItemList);
        missingItemsList.setAdapter(arrayAdapter);

        final AlertDialog dialog = builder.setTitle(
                mMissingItemList.size() == 0 ? R.string.title_all_items_counted : R.string.title_missing_items)
                .setView(rootView)
                .setPositiveButton(R.string.button_ok, null)
                .create();

        return dialog;
    }
}
