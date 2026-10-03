/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.view.impl;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;

import com.kavsdkexample.core.app.utils.AndroidUtils;

public abstract class BaseDialogFragment extends DialogFragment {
    private static final String TAG = BaseDialogFragment.class.getSimpleName();
    private static final String PARENT_FRAGMENT_ID_EXTRA = "parentTag";
    private String mParentFragmentTag;
    private int mRequestId;

    protected static void show(FragmentActivity activity, Class<? extends DialogFragment> classFragment,
                               @NonNull Bundle args, int requestId, String parentFragmentTag) {

        final String flagmentTag = String.format("%s_%s", TAG, classFragment.getSimpleName());

        // remove prev if exists
        FragmentManager fm = activity.getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(flagmentTag);
        if (prev != null) {
            ft.remove(prev);
        }

        DialogFragment fragment = AndroidUtils.newInstance(classFragment);
        if (fragment instanceof BaseDialogFragment) {
            ((BaseDialogFragment)fragment).mRequestId = requestId;
        }

        if (parentFragmentTag != null) {
            args.putString(PARENT_FRAGMENT_ID_EXTRA, parentFragmentTag);
        }

        fragment.setArguments(args);
        fragment.show(ft, flagmentTag);
    }

    public int getRequestId() {
        return mRequestId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        if (args != null) {
            mParentFragmentTag = args.getString(PARENT_FRAGMENT_ID_EXTRA);
        }
    }

    @Override
    public void onDestroyView() {
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        // for support lib use:     getDialog().setOnDismissListener(null);
        // for real fragments:      getDialog().setDismissMessage(null);
        Dialog dlg = getDialog();
        if (dlg != null && getRetainInstance()) {
            dlg.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    protected void setDialogResult(Object data) {
        FragmentActivity activity = getActivity();

        Log.d(TAG, String.format("dialog result is %s", data));
        if (activity != null) {
            Fragment parentFragment = (mParentFragmentTag == null) ? null : AndroidUtils.findFragmentByTag(activity, mParentFragmentTag);
            if (parentFragment instanceof OnDialogFragmentResultListener) {
                ((OnDialogFragmentResultListener) parentFragment).onDialogFragmentResult(this, data);
            } else if (activity instanceof OnDialogFragmentResultListener) {
                ((OnDialogFragmentResultListener) activity).onDialogFragmentResult(this, data);
            }
        }
    }
}
