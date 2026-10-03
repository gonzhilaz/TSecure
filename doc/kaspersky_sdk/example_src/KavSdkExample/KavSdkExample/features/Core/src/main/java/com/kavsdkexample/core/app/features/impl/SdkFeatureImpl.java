/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features.impl;

import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.BuildConfig;
import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.core.app.view.BaseView;

import java.util.HashSet;
import java.util.Set;


public abstract class SdkFeatureImpl implements SdkFeature {
    private static final String TAG = SdkFeatureImpl.class.getSimpleName();
    @NonNull
    private final Set<SdkStatusObserver> mSdkStatusObservers = new HashSet<>();
    @NonNull
    protected final ServiceInteractor      mServiceInteractor;
    @NonNull
    protected final NotificationInteractor mNotificationInteractor;

    protected boolean mSdkInited;

    protected boolean dependsOnSdkInit() {
        return true;
    }

    protected SdkFeatureImpl(@NonNull ServiceInteractor      serviceInteractor,
                             @NonNull NotificationInteractor notificationInteractor) {
        mServiceInteractor      = serviceInteractor;
        mNotificationInteractor = notificationInteractor;
    }

    @Override
    @CallSuper
    @UiThread
    public boolean onSdkInited() {
        if (BuildConfig.DEBUG) {
            ThreadManagerInstance.getInstance().checkUiThread();
            Log.v(TAG, String.format("onSdkInited called for %s", getName()));
        }

        if (!mSdkInited && dependsOnSdkInit() && mSdkStatusObservers.size() == 0) {
            throw new IllegalStateException("No model(s) subscribed for sdk status event: feature - " + getName());
        }
        mSdkInited = true;
        if (BuildConfig.DEBUG) {
            Log.v(TAG, String.format("onSdkInited: mSdkStatusObservers.size() for %s is %d", getName(), mSdkStatusObservers.size()));
        }
        for (SdkStatusObserver observer : mSdkStatusObservers) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, String.format("onSdkInited: Calling SdkStatusObserver::onSdkInited for %s", getName()));
            }
            observer.onSdkInited();
        }
        if (BuildConfig.DEBUG) {
            Log.v(TAG, String.format("onSdkInited: Clear mSdkStatusObservers for %s", getName()));
        }
        mSdkStatusObservers.clear();
        return false;
    }

    @Override
    @CallSuper
    @UiThread
    public void onOwnerViewEvent(OwnerViewEvent event, BaseView view) {
        if (BuildConfig.DEBUG) {
            ThreadManagerInstance.getInstance().checkUiThread();
        }
    }

    @Override
    @UiThread
    public void subscribe(@NonNull SdkStatusObserver observer) {
        if (!mSdkStatusObservers.add(observer)) {
            throw new IllegalStateException("Observer: " + observer + " attempts to subscribe one more time");
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, String.format("subscribe for %s", getName()));
        }

        if (mSdkInited) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, String.format("subscribe: Already inited. Calling observers for %s", getName()));
            }
            for (SdkStatusObserver currentObservers : mSdkStatusObservers) {
                currentObservers.onSdkInited();
            }
            if (BuildConfig.DEBUG) {
                Log.v(TAG, String.format("subscribe: Already inited. Clear observers for %s", getName()));
            }
            mSdkStatusObservers.clear();
        }
    }
}
