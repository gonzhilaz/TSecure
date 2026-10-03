/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.presenter.impl.AntivirusBasePresenterImpl;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckModel;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckModelObserver;
import com.kavsdkexample.antivirus.self_checker.presenter.SelfCheckPresenter;
import com.kavsdkexample.antivirus.self_checker.view.SelfCheckView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public final class SelfCheckPresenterImpl extends    AntivirusBasePresenterImpl<SelfCheckView,
                                                                                BaseViewState,
                                                                                SelfCheckModel>
                                          implements SelfCheckPresenter {

    @Inject
    SelfCheckPresenterImpl(@NonNull SelfCheckModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull SelfCheckView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new SelfCheckModelObserverImpl(view));
        preCheck(view);
    }

    private boolean preCheck(@Nullable SelfCheckView view) {
        if (mModel.isCheckCompleted()) {
            if (view != null) {
                view.showSelfCheckResult(mModel.isCompromised());
            }
            return true;
        }
        return false;
    }

    @Override
    public void checkSelf() {
        if (!preCheck(mView)) {
            mModel.checkSelf();
            if (mView != null) {
                mView.showProgressDialog();
            }
        }
    }

    private static final class SelfCheckModelObserverImpl implements SelfCheckModelObserver {
        private final SelfCheckView mView;

        SelfCheckModelObserverImpl(@NonNull SelfCheckView view) {
            mView = view;
        }

        @Override
        public void onSelfCheckResults(boolean isCompromised) {
            mView.hideProgressDialog();
            mView.showSelfCheckResult(isCompromised);
        }

        @Override
        public void onError(@NonNull String message) {
            mView.hideProgressDialog();
            mView.showSelfCheckFailed(message);
        }

        @Override
        public void onBasesUnavailable() {
            mView.hideProgressDialog();
            mView.showBasesUnavailable();
        }
    }
}
