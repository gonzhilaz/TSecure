/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import com.kavsdkexample.secure_connectivity.R;

public class DeprecatedFeatureDialogFragment extends DialogFragment {
    private static final String DEPRECATED_DIALOG_TAG = "deprecated_dialog";


    public static void show(@NonNull FragmentManager manager) {
        DeprecatedFeatureDialogFragment fragment = new DeprecatedFeatureDialogFragment();
        fragment.show(manager, DEPRECATED_DIALOG_TAG);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.str_unsupported_deprecated_dialog_title)
                .setMessage(String.format(context.getString(R.string.str_unsupported_deprecated_dialog_message), Build.VERSION.SDK_INT))
                .setCancelable(true)
                .create();
    }
}
