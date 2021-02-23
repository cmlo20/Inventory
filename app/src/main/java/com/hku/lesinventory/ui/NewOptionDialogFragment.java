package com.hku.lesinventory.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.hku.lesinventory.R;
import com.hku.lesinventory.databinding.NewOptionDialogBinding;

public class NewOptionDialogFragment extends DialogFragment {

    public static final String TAG = NewOptionDialogFragment.class.getName();

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
            mListener = (NewOptionDialogListener) context;  // register the context as listener
        } catch (ClassCastException e) {    // if the context does not implements the required interface, throws an exception
            throw new ClassCastException(context.toString() + " must implement NewOptionDialogListener interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.new_option_dialog, null);

        final AlertDialog dialog = builder.setTitle(mTitleId)
                .setView(rootView)
                .setPositiveButton(R.string.button_add, null)
                .setNegativeButton(R.string.button_cancel, (dialogInterface, id) -> mListener.onDialogNegativeClick(NewOptionDialogFragment.this))
                .create();

        // Todo: Validate duplicate option name (category/brand/location)
        // Override onClick listener for positive button to validate user input
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                EditText newOptionEditText = rootView.findViewById(R.id.new_option_name);
                String newOptionName = newOptionEditText.getText().toString();
                Log.i(TAG, "New option name: " + newOptionName);
                if (newOptionName.isEmpty()) {
                    newOptionEditText.setError(getString(R.string.warn_empty_name));
                } else {
                    mListener.onDialogPositiveClick(NewOptionDialogFragment.this);
                    dialog.dismiss();
                }
            });
        });
//        dialog.show();
        return dialog;
    }
}
