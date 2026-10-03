/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.presenter;

import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.self_defense.view.SelfDefenseFeatureView;

@UiThread
public interface SelfDefenseFeaturePresenter extends BasePresenter<SelfDefenseFeatureView, BaseViewState> {
    void setSdkAutoRestartEnabled(boolean value);
    void enableForegroundService();
    void disableForegroundService();
    void refreshNotificationAccessState();
    void switchToCheckSignatureFragment();
}
