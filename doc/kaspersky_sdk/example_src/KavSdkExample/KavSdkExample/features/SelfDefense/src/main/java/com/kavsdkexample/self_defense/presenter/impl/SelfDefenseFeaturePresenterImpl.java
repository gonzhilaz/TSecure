/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.self_defense.model.SelfDefenseModel;
import com.kavsdkexample.self_defense.model.sdk.NotificationStateObserver;
import com.kavsdkexample.self_defense.presenter.SelfDefenseFeaturePresenter;
import com.kavsdkexample.self_defense.view.SelfDefenseFeatureView;

import javax.inject.Inject;

@UiThread
public final class SelfDefenseFeaturePresenterImpl extends    BasePresenterImpl<SelfDefenseFeatureView, BaseViewState, SelfDefenseModel>
                                                   implements SelfDefenseFeaturePresenter,
                                                              NotificationStateObserver {
    @Inject
    SelfDefenseFeaturePresenterImpl(@NonNull SelfDefenseModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull SelfDefenseFeatureView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();
        mModel.setNotificationStateObserver(null);
    }

    @Override
    public void onSdkInited() {
        if (mView != null) {
            mView.setAutorestartState(mModel.isSdkAutorestartEnabled());
            mView.setForegroundServiceState(mModel.isForegroundServiceEnabled());
            mView.setNotificationAccessState(mModel.isNotificationAccessEnabled());
            mModel.setNotificationStateObserver(this);
        }
    }

    @Override
    public void setSdkAutoRestartEnabled(boolean value) {
        mModel.setSdkAutoRestartEnabled(value);
    }

    @Override
    public void enableForegroundService() {
        mModel.enableForegroundService();
    }

    @Override
    public void disableForegroundService() {
        mModel.disableForegroundService();
    }

    @Override
    public void refreshNotificationAccessState() {
        if (mView != null) {
            mView.setNotificationAccessState(mModel.isNotificationAccessEnabled());
        }
    }

    @Override
    public void switchToCheckSignatureFragment() {
        mModel.switchToCheckSignatureView();
    }

    @Override
    public void onNotificationAccessStateChanged(boolean enabled) {
        if (mView != null) {
            mView.setNotificationAccessState(enabled);
        }
    }
}
