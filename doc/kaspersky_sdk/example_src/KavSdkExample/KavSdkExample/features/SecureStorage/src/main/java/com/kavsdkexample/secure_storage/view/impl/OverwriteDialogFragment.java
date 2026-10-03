/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.impl;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kavsdkexample.secure_storage.presenter.file.OverwriteOperation;
import com.kavsdkexample.secure_storage.R;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatDialogFragment;

public class OverwriteDialogFragment extends DaggerAppCompatDialogFragment {
    private static final String OVERWRITE_DIALOG_TAG = "overwrite_dialog";
    private static final String APPEND_PARAM         = "append";

    @Inject
    OverwriteOperation mPresenter;

    public static void show(@NonNull FragmentManager manager, boolean append) {
        OverwriteDialogFragment fragment = new OverwriteDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(APPEND_PARAM, append);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        fragment.show(manager, OVERWRITE_DIALOG_TAG);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions")
        boolean append = getArguments().getBoolean(APPEND_PARAM);
        return new AlertDialog
                .Builder(requireContext())
                .setTitle(R.string.str_secure_storage_test_file_overwrite_dialog_title)
                .setMessage(R.string.str_secure_storage_test_file_overwrite_dialog_message)
                .setPositiveButton(R.string.str_secure_storage_test_file_overwrite_dialog_positive, (dialog, which) -> mPresenter.acceptOverwrite(append))
                .setNegativeButton(R.string.str_secure_storage_test_file_overwrite_dialog_negative,  (dialog, which) -> mPresenter.discardOverwrite())
                .setOnCancelListener(dialog -> mPresenter.discardOverwrite())
                .create();
    }
}
