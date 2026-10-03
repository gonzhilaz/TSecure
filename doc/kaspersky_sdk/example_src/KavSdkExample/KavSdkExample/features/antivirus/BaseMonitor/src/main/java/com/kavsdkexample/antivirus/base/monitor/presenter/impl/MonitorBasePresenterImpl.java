/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModel;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModelObserver;
import com.kavsdkexample.antivirus.base.monitor.view.MonitorBaseView;
import com.kavsdkexample.antivirus.base.presenter.impl.AntivirusBasePresenterImpl;
import com.kavsdkexample.antivirus.base.monitor.presenter.MonitorBasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

public class MonitorBasePresenterImpl<VIEW      extends MonitorBaseView,
                                      VIEWSTATE extends BaseViewState,
                                      MODEL     extends MonitorBaseModel>
                              extends    AntivirusBasePresenterImpl<VIEW, VIEWSTATE, MODEL>
                              implements MonitorBasePresenter<VIEW, VIEWSTATE> {

    public MonitorBasePresenterImpl(@NonNull MODEL model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new MonitorBaseModelObserverImpl(view, mModel));
        if (isViewCreated()) {
            view.setMonitorEnabledState(mModel.getMonitorState());
            view.setAllowCloudCheckState(mModel.getAllowCloudScan());
            view.setCloudOnlyCheckState(mModel.getCloudOnlyScan());
            view.setMaxFileCheckSize(mModel.getMaxFileCheckSize());
            view.setRiskwareCheckState(mModel.getScanRiskware());
        }
    }

    @Override
    public void enableMonitor(boolean value) {
        mModel.setMonitorState(value);
    }

    @Override
    public void enableCloudOnlyCheck(boolean value) {
        mModel.setCloudOnlyScan(value);
    }

    @Override
    public void enableCloudCheck(boolean value) {
        mModel.setAllowCloudScan(value);
    }

    @Override
    public void enableRiskwareCheck(boolean value) {
        mModel.setScanRiskware(value);
    }

    @Override
    public void enableSuspicious(boolean value) {
        mModel.setScanSuspicious(value);
    }

    @Override
    public void setMaxFileCheckSize(long size) {
        mModel.setMaxFileCheckSize(size);
    }

    private static class MonitorBaseModelObserverImpl implements MonitorBaseModelObserver  {
        @NonNull private final MonitorBaseView mView;
        @NonNull private final MonitorBaseModel mModel;

        MonitorBaseModelObserverImpl(@NonNull MonitorBaseView view, @NonNull MonitorBaseModel model) {
            mView = view;
            mModel = model;
        }

        @Override
        public void onMonitorStateChanged(boolean enabled) {
            mView.onMonitorStateChanged(enabled);
            mView.setCloudOnlyCheckState(mModel.getCloudOnlyScan());
        }
    }
}
