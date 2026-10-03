/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.presenter;

import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.self_defense.view.CheckSignatureView;
import com.kavsdkexample.self_defense.view.CheckSignatureViewState;

@UiThread
public interface CheckSignaturePresenter extends BasePresenter<CheckSignatureView, CheckSignatureViewState> {
    void switchToSelfDefenseFeatureFragment();
    void checkAppSignature();
}
