/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.presenter.impl;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.EmptyPresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

@UiThread
public final class EmptyPresenterImpl implements EmptyPresenter {
    @Inject
    EmptyPresenterImpl() {
    }

    @Override
    @CallSuper
    public void subscribe(@NonNull BaseView view, @Nullable BaseViewState state, boolean viewCreated) {
    }

    @Override
    @CallSuper
    public void unsubscribe() {
    }

    @Override
    public boolean isSubscribed() {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void viewPaused(@NonNull BaseView view) {
    }

    @Override
    public void viewResumed(@NonNull BaseView view) {
    }

    @Override
    public void onSdkInited() {
    }

    @Override
    public void onSdkInitFailed() {
    }

    @Override
    public void runOnSubscription(@NonNull Runnable action) {
    }
}
