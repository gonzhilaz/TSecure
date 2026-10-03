/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

@UiThread
public interface BasePresenter<VIEW extends BaseView, VIEWSTATE extends BaseViewState> extends SdkStatusObserver {
    void    subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated);
    void    unsubscribe();
    boolean isSubscribed();
    boolean isInitialized();
    void    viewResumed(@NonNull VIEW view);
    void    viewPaused(@NonNull VIEW view);
    void    runOnSubscription(@NonNull Runnable action);
}