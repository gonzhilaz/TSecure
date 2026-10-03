/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.view.impl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import com.kavsdkexample.antivirus.folder_monitor.R;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FoldersActionPresenter;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FoldersActionType;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatDialogFragment;

public class FoldersActionDialogFragment extends DaggerAppCompatDialogFragment {
    private static final String ACTION_DIALOG_TAG = "action_dialog";
    private static final String TYPE_PARAM        = "type";
    private static final String TYPE_FOLDER       = "folder";

    @Inject
    FoldersActionPresenter mPresenter;

    public static void show(@NonNull FragmentManager manager, @NonNull FoldersActionType type, @Nullable String folder) {
        FoldersActionDialogFragment fragment = new FoldersActionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE_PARAM, type.ordinal());
        if (type == FoldersActionType.Remove) {
            args.putString(TYPE_FOLDER, folder);
        }
        fragment.setArguments(args);
        fragment.setCancelable(true);
        fragment.show(manager, ACTION_DIALOG_TAG);
    }

    @Override
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleId;
        int messageId;

        Bundle            arguments = getArguments();
        FoldersActionType type      = FoldersActionType.values()[arguments.getInt(TYPE_PARAM)];
        String            folder    = arguments.getString(TYPE_FOLDER);
        titleId = R.string.str_av_folder_monitor_alert_fragment_title;
        DialogInterface.OnClickListener listener;
        switch (type) {
            case Clean:
                messageId = R.string.str_av_folder_monitor_delete_all_folder_monitors;
                listener = (dialog, which) -> mPresenter.cleanFolders();
                break;
            case Remove:
                messageId = R.string.str_av_folder_monitor_delete_selected_folder_monitor;
                listener = (dialog, which) -> mPresenter.removeFolder(folder);
                break;
            default:
                throw new IllegalStateException("Unsupported dialog type: " + type);
        }

        return new AlertDialog
                .Builder(requireContext())
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel,  (dialog, which) -> dismiss())
                .create();
    }
}
