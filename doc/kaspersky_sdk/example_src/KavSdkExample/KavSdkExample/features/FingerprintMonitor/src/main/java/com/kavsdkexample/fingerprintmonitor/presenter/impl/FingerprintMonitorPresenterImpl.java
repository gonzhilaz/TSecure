/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdk.fingerprint.OnFingerprintChangedListener;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.fingerprintmonitor.R;
import com.kavsdkexample.fingerprintmonitor.model.FingerprintMonitorModel;
import com.kavsdkexample.fingerprintmonitor.model.FingerprintMonitorModelObserver;
import com.kavsdkexample.fingerprintmonitor.presenter.FingerprintMonitorPresenter;
import com.kavsdkexample.fingerprintmonitor.view.FingerprintMonitorView;

import javax.inject.Inject;

public class FingerprintMonitorPresenterImpl extends BasePresenterImpl<FingerprintMonitorView, BaseViewState, FingerprintMonitorModel>
        implements FingerprintMonitorPresenter, FingerprintMonitorModelObserver {

    @Inject
    FingerprintMonitorPresenterImpl(@NonNull FingerprintMonitorModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull FingerprintMonitorView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(this);
    }

    @Override
    public void setMonitorEnabled(boolean enabled) {
        if (enabled) {
            mModel.enable();
        } else {
            mModel.disable();
        }

        if (mView != null) {
            mView.displayMonitorState(enabled);
        }
    }

    @Override
    public boolean getMonitorEnabled() {
        return mModel.isEnabled();
    }

}
