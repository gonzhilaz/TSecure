/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.presenter.impl;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.model.BaseModel;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UiThread
public abstract class BasePresenterImpl<VIEW      extends BaseView,
        VIEWSTATE extends BaseViewState,
        MODEL     extends BaseModel>
        implements BasePresenter<VIEW,VIEWSTATE> {
    @NonNull
    protected final MODEL             mModel;
    private   final Set<Object>       mExtraObservers;
    private   final Set<Object>       mExtraStaticObservers;
    private   final List<Runnable>    mSubscriptionPendingActions;
    @Nullable
    protected       VIEW              mView;
    private         SdkStatusObserver mObserver;
    private         boolean           mIsSubscribed;
    private         boolean           mViewWasCreated;

    protected BasePresenterImpl(@NonNull MODEL model) {
        super();
        mModel                      = model;
        mExtraObservers             = new HashSet<>();
        mExtraStaticObservers       = new HashSet<>();
        mSubscriptionPendingActions = new ArrayList<>();
    }

    protected VIEW requireView() {
        if (mView == null) {
            throw new IllegalStateException("Expected presenter to be subscribed on view here");
        }
        return mView;
    }

    protected final boolean isViewCreated() {
        return mViewWasCreated;
    }

    @Override
    public boolean isInitialized() {
        return mModel.isInitialized();
    }

    @Override
    @CallSuper
    public void subscribe(@NonNull VIEW view, @Nullable VIEWSTATE state, boolean viewCreated) {
        mView             = view;
        mViewWasCreated   = viewCreated;
        registerModelObserver(this);
        performAdditionalSubscriptionActions();
        mIsSubscribed     = true;
        for (Runnable action : mSubscriptionPendingActions) {
            action.run();
        }
        mSubscriptionPendingActions.clear();
    }

    @Override
    @CallSuper
    public void unsubscribe() {
        if (mIsSubscribed) {
            if (mObserver != null) {
                mModel.removeObserver(mObserver);
            }
            for (Object extraObserver : mExtraObservers) {
                mModel.removeAdditionalObserver(extraObserver);
            }

            for (Object extraObserver : mExtraStaticObservers) {
                mModel.removeAdditionalObserver(extraObserver, true);
            }
            mExtraObservers.clear();
            mExtraStaticObservers.clear();
            mSubscriptionPendingActions.clear();
            mObserver = null;
            mView = null;
            mIsSubscribed = false;
        }
    }

    @Override
    public boolean isSubscribed() {
        return mIsSubscribed;
    }

    private void registerModelObserver(@NonNull SdkStatusObserver observer) {
        mObserver = observer;
        mModel.addObserver(observer);
    }

    protected <OBSERVER> void registerModelObserver(@NonNull OBSERVER observer) {
        mExtraObservers.add(observer);
        mModel.addAdditionalObserver(observer);
    }

    protected <OBSERVER> void registerModelObserver(@NonNull OBSERVER observer, boolean isStatic) {
        if (isStatic) {
            mExtraStaticObservers.add(observer);
        } else {
            mExtraObservers.add(observer);
        }
        mModel.addAdditionalObserver(observer, isStatic);
    }

    protected void performAdditionalSubscriptionActions() {}

    @Override
    public void viewPaused(@NonNull VIEW view) {
    }

    @Override
    public void viewResumed(@NonNull VIEW view) {
    }

    @Override
    public void onSdkInited() {
        if (mView != null) {
            mView.onSdkInited();
        }
    }

    @Override
    public void onSdkInitFailed() {
    }

    @Override
    public void runOnSubscription(@NonNull Runnable action) {
        mSubscriptionPendingActions.add(action);
    }
}
