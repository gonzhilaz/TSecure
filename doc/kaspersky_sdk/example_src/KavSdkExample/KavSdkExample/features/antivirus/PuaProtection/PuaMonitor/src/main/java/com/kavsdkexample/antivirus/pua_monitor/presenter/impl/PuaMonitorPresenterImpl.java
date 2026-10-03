/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.presenter.impl.MonitorBasePresenterImpl;
import com.kavsdkexample.antivirus.pua_monitor.model.PuaMonitorModel;
import com.kavsdkexample.antivirus.pua_monitor.presenter.PuaMonitorPresenter;
import com.kavsdkexample.antivirus.pua_monitor.view.PuaMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class PuaMonitorPresenterImpl extends    MonitorBasePresenterImpl<PuaMonitorView, BaseViewState, PuaMonitorModel>
                                     implements PuaMonitorPresenter {
    @Inject
    public PuaMonitorPresenterImpl(@NonNull PuaMonitorModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull PuaMonitorView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            view.setProcessMissedState(mModel.getMissedAppCheckState());
        }
    }

    @Override
    public void enableMissedAppCheck(boolean value) {
        mModel.setMissedAppCheckState(value);
    }
}
