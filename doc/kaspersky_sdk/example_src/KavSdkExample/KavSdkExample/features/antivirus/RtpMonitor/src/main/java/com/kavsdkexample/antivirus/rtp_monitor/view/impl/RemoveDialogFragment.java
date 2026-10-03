/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view.impl;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import com.kavsdkexample.antivirus.rtp_monitor.R;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveDialogPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveType;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatDialogFragment;

public class RemoveDialogFragment extends DaggerAppCompatDialogFragment {
    private static final String REMOVE_DIALOG_TAG = "remove_dialog";
    private static final String TYPE_PARAM        = "type";
    private static final String TYPE_FOLDER       = "folder";

    @Inject
    RemoveDialogPresenter mPresenter;

    public static void show(@NonNull FragmentManager manager, @NonNull RemoveType type, @NonNull String folder) {
        RemoveDialogFragment fragment = new RemoveDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE_PARAM, type.ordinal());
        args.putString(TYPE_FOLDER, folder);
        fragment.setArguments(args);
        fragment.setCancelable(true);
        fragment.show(manager, REMOVE_DIALOG_TAG);
    }

    @Override
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleId;
        int messageId;

        Bundle arguments  = getArguments();
        RemoveType type   = RemoveType.values()[arguments.getInt(TYPE_PARAM)];
        String     folder = arguments.getString(TYPE_FOLDER);
        switch (type) {
            case FolderToMonitor:
                titleId   = R.string.str_av_protection_rtp_delete_selected_folder_title;
                messageId = R.string.str_av_protection_rtp_delete_selected_folder_text;
                break;
            case Exclusion:
                titleId   = R.string.str_av_protection_rtp_delete_selected_exclusion_title;
                messageId = R.string.str_av_protection_rtp_delete_selected_exclusion_text;
                break;
            default:
                throw new IllegalStateException("Unsupported dialog type: " + type);
        }

        return new AlertDialog
                .Builder(requireContext())
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> mPresenter.removeFolder(type, folder))
                .setNegativeButton(android.R.string.cancel,  (dialog, which) -> dismiss())
                .create();
    }
}
