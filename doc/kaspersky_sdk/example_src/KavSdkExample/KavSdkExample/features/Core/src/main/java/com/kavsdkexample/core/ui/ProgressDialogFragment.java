/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;

import com.kavsdkexample.core.BuildConfig;
import com.kavsdkexample.core.R;

public class ProgressDialogFragment extends DialogFragment {
    private static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private static final String OVERWRITE_DIALOG_TAG = "progress_dialog";

    public static void show(@NonNull FragmentManager manager) {
        Fragment fragment = manager.findFragmentByTag(OVERWRITE_DIALOG_TAG);
        if (fragment == null) {
            DialogFragment dialogFragment = new ProgressDialogFragment();
            dialogFragment.show(manager, OVERWRITE_DIALOG_TAG);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "dialog shown");
            }
        }
    }

    public static void hide(@NonNull FragmentManager manager) {
        Fragment fragment = manager.findFragmentByTag(OVERWRITE_DIALOG_TAG);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "dialog dismissed");
            }
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity()) {
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        };

        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.str_core_loading_application));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }
}
