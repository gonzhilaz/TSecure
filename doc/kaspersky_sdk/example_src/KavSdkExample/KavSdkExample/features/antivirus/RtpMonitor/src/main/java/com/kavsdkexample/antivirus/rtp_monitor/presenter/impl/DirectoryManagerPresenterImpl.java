/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import com.kavsdkexample.antivirus.rtp_monitor.model.RtpModelObserver;
import com.kavsdkexample.antivirus.rtp_monitor.model.RtpMonitorModel;
import com.kavsdkexample.antivirus.rtp_monitor.view.DirectoryManagerView;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.DirectoryManagerPresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class DirectoryManagerPresenterImpl extends    BasePresenterImpl<DirectoryManagerView, BaseViewState, RtpMonitorModel>
                                           implements DirectoryManagerPresenter {
    @Inject
    DirectoryManagerPresenterImpl(@NonNull RtpMonitorModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull DirectoryManagerView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new RtpModelObserverImpl(view));
        if (viewCreated) {
            view.updateExcludedFolders(mModel.getExcludedFolders());
            view.updateFoldersToMonitor(mModel.getMonitoredFolders());
        }
    }

    @Override
    public void addFolderForMonitoring(@NonNull String folder, int flags) {
         mModel.addFolderForMonitoring(folder, flags);
    }

    @Override
    public void addExcludeFolder(@NonNull String folder) {
        mModel.addExcludedFolder(folder);
    }

    @Override
    public void addDefaultDirectories() {
        mModel.addDefaultDirectories();
    }

    @Override
    public void removeDefaultDirectories() {
        mModel.removeDefaultDirectories();
    }

    private static final class RtpModelObserverImpl implements RtpModelObserver {
        @NonNull
        private final DirectoryManagerView mView;

        RtpModelObserverImpl(@NonNull DirectoryManagerView view) {
            mView = view;
        }

        @Override
        public void onMonitorFolderAdded(@NonNull String folder) {
            mView.addMonitoringFolder(folder);
        }

        @Override
        public void onMonitorFolderRemoved(@NonNull String folder) {
            mView.removeMonitoringFolder(folder);
        }

        @Override
        public void onMonitorFoldersChanged(@NonNull Map<String, Integer> items) {
            mView.updateFoldersToMonitor(items);
        }

        @Override
        public void onExclusionFolderAdded(@NonNull String folder) {
            mView.addExclusionFolder(folder);
        }

        @Override
        public void onExclusionRemoved(@NonNull String folder) {
            mView.removeExclusionFolder(folder);
        }
    }
}
