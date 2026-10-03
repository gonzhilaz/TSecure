/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.presenter.impl.MonitorBasePresenterImpl;
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModel;
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModelObserver;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FolderMonitorPresenter;
import com.kavsdkexample.antivirus.folder_monitor.view.FolderMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.Collections;

import javax.inject.Inject;

public class FolderMonitorPresenterImpl extends    MonitorBasePresenterImpl<FolderMonitorView, BaseViewState, FolderMonitorModel>
                                        implements FolderMonitorPresenter {
    @Inject
    FolderMonitorPresenterImpl(@NonNull FolderMonitorModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull FolderMonitorView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new FolderMonitorModelObserverImpl(view));
        if (viewCreated) {
            view.setTryCure(mModel.getTryCure());
            view.setFoldersToMonitor(mModel.getMonitoredFolders());
        }
    }

    @Override
    public void enableTryCure(boolean value) {
        mModel.setTryCure(value);
    }

    @Override
    public void folderItemClicked(@NonNull String path) {
        //noinspection ConstantConditions
        mView.showDeleteFolderFragment(path);
    }

    @Override
    public void addButtonClicked() {
        //noinspection ConstantConditions
        mView.showAddFolderFragment();
    }

    @Override
    public void clearButtonClicked() {
        //noinspection ConstantConditions
        mView.showClearAllFragment();
    }

    @Override
    public void addFolder(@NonNull String folder) {
        mModel.addFolderForMonitoring(folder);
    }

    private static class FolderMonitorModelObserverImpl implements FolderMonitorModelObserver {
        @NonNull private final FolderMonitorView mView;

        FolderMonitorModelObserverImpl(@NonNull FolderMonitorView view) {
            mView = view;
        }

        @Override
        public void onMonitorFolderAdded(@NonNull String folder) {
            mView.showAddedFolder(folder);
        }

        @Override
        public void onMonitorFolderRemoved(@NonNull String folder) {
            mView.showRemovedFolder(folder);
        }

        @Override
        public void onMonitorFolderCleared() {
            mView.setFoldersToMonitor(Collections.emptySet());
        }
    }
}
