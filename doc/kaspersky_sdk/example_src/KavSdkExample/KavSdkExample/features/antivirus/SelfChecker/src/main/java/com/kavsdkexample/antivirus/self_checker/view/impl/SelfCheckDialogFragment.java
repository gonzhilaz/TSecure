/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.view.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.kavsdkexample.antivirus.self_checker.R;
import com.kavsdkexample.antivirus.self_checker.presenter.SelfCheckPresenter;
import com.kavsdkexample.antivirus.self_checker.view.SelfCheckView;
import com.kavsdkexample.core.app.utils.ExceptionUtils;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseDialogFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;


public class SelfCheckDialogFragment extends    BaseDialogFragment<SelfCheckView,
                                                                   BaseViewState,
                                                                   SelfCheckPresenter>
                                     implements SelfCheckView {

    private static final String SELF_CHECK_DIALOG_TAG = "selfcheck_dialog";

    @Inject
    SelfCheckPresenter mPresenter;

    public static void show(@NonNull FragmentManager manager) {
        SelfCheckDialogFragment fragment = new SelfCheckDialogFragment();
        fragment.show(manager, SELF_CHECK_DIALOG_TAG);
    }

    @NonNull
    @Override
    protected SelfCheckPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.str_self_checker_dialog_title)
                .setMessage("")
                .setCancelable(false)
                .setPositiveButton(R.string.str_self_checker_dialog_positive_button, null)
                .setNegativeButton(R.string.str_self_checker_dialog_negative_button, (dlg, which) -> dismiss())
                .create();
        dialog.setOnShowListener(dialogInterface ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                  .setOnClickListener(view -> mPresenter.checkSelf())
        );
        return dialog;
    }

    @Override
    public void showSelfCheckResult(boolean isCompromised) {
        int resultStrId = isCompromised ? R.string.str_self_checker_dialog_compromised : R.string.str_self_checker_dialog_clean;
        ((AlertDialog) getDialog()).setMessage(requireContext().getString(resultStrId));
    }

    @Override
    public void showSelfCheckFailed(@NonNull String message) {
        ((AlertDialog) getDialog()).setMessage(message);
    }

    @Override
    public void showAntivirusInitFailed(@NonNull Exception e) {
        ((AlertDialog) getDialog()).setMessage(
                requireContext().getString(R.string.str_antivirus_init_failed_error) + ExceptionUtils.stackTraceToString(e));
    }

    @Override
    public void showBasesUnavailable() {
        ((AlertDialog) getDialog()).setMessage(requireContext().getString(R.string.str_no_bases_available));
    }

    @Override
    public void showProgressDialog() {
        ((AlertDialog) getDialog()).setMessage(requireContext().getString(R.string.str_self_checker_dialog_progress_msg));
    }

    @Override
    public void hideProgressDialog() {
    }
}
