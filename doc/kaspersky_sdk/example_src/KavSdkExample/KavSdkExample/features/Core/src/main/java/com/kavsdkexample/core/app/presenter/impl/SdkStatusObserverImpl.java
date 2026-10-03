/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.presenter.impl;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.view.BaseView;

public class SdkStatusObserverImpl<T extends BaseView> implements SdkStatusObserver {
    @NonNull
    protected T mView;

    protected SdkStatusObserverImpl(@NonNull T view) {
        mView = view;
    }

    @Override
    @CallSuper
    public void onSdkInited() {
        mView.hideProgressDialog();
    }

    @Override
    public void onSdkInitFailed() {
        // ignored by default. Should not come to sdk feature fragments
    }
}
