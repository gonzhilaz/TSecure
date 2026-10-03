/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.view.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.kavsdk.rootdetector.RootCause;
import com.kavsdk.rootdetector.RootDetector;
import com.kavsdkexample.antivirus.root_checker.R;
import com.kavsdkexample.antivirus.root_checker.presenter.RootCheckPresenter;
import com.kavsdkexample.antivirus.root_checker.view.RootCheckView;
import com.kavsdkexample.core.app.utils.ExceptionUtils;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseDialogFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;


public class RootCheckDialogFragment extends    BaseDialogFragment<RootCheckView,
                                                                   BaseViewState,
                                                                   RootCheckPresenter>
                                     implements RootCheckView {

    private static final String ROOT_CHECK_DIALOG_TAG = "rootcheck_dialog";

    @Inject
    RootCheckPresenter mPresenter;

    public static void show(@NonNull FragmentManager manager) {
        RootCheckDialogFragment fragment = new RootCheckDialogFragment();
        fragment.show(manager, ROOT_CHECK_DIALOG_TAG);
    }

    @NonNull
    @Override
    protected RootCheckPresenter getPresenter() {
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
                .setTitle(R.string.str_root_detector_dialog_title)
                .setMessage("")
                .setCancelable(false)
                .setPositiveButton(R.string.str_root_detector_dialog_positive_button, null)
                .setNegativeButton(R.string.str_root_detector_dialog_negative_button, (dlg, which) -> {
                    if (dlg != null && isAdded() && isResumed()) {
                        dismiss();
                    }
                })
                .create();
        dialog.setOnShowListener(dialogInterface ->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                  .setOnClickListener(view -> mPresenter.checkRoot())
        );
        return dialog;
    }

    @Override
    public void showRootCheckResult(boolean isRooted) {
        String resultMessage;
        if (isRooted) {
            String rootedMessage = requireContext().getString(R.string.str_root_detector_dialog_rooted);
            resultMessage = String.format("%s \n%s", rootedMessage, getRootCauseMessage(RootDetector.getInstance().getRootCause()));
        } else {
            resultMessage = requireContext().getString(R.string.str_root_detector_dialog_not_rooted);
        }
        ((AlertDialog) getDialog()).setMessage(resultMessage);
    }

    @Override
    public void showRootCheckFailed(@NonNull String message) {
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
        ((AlertDialog) getDialog()).setMessage(requireContext().getString(R.string.str_root_detector_dialog_progress_msg));
    }

    @Override
    public void hideProgressDialog() {
    }

    private String getRootCauseMessage(@NonNull RootCause rootCause) {
        String rootCausePath = rootCause.getRootCausePath();
        if (rootCausePath != null) {
            return requireContext().getString(R.string.str_root_detector_dialog_root_cause_path) + rootCausePath;
        }
        return requireContext().getString(R.string.str_root_detector_dialog_root_cause_self_check_failed);
    }
}
