package com.hku.lesinventory.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.DialogFragment;

import com.hku.lesinventory.R;

public class NewOptionDialogFragment extends DialogFragment {

    public interface NewOptionDialogListener {
        void onDialogPositiveClick(NewOptionDialogFragment dialog);
        void onDialogNegativeClick(NewOptionDialogFragment dialog);
    }
    // Listener to which the action events are delivered
    NewOptionDialogListener mListener;

    private final int mTitleId;

    public int getTitleId() { return mTitleId; }

    public NewOptionDialogFragment(final int titleId) { mTitleId = titleId; }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (NewOptionDialogListener) context;  // instantiate the NewOptionDialogListener
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement NewOptionDialogListener interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setTitle(mTitleId)
                .setView(inflater.inflate(R.layout.new_option_dialog, null))
                .setPositiveButton(R.string.button_add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // Add new category
                        mListener.onDialogPositiveClick(NewOptionDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // Cancel
                        mListener.onDialogNegativeClick(NewOptionDialogFragment.this);
                    }
                });

        return builder.create();
    }
}
