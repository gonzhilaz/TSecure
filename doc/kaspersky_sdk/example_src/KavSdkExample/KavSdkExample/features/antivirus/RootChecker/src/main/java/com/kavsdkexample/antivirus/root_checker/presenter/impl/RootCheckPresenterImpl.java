/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.presenter.impl.AntivirusBasePresenterImpl;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckModel;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckModelObserver;
import com.kavsdkexample.antivirus.root_checker.presenter.RootCheckPresenter;
import com.kavsdkexample.antivirus.root_checker.view.RootCheckView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public final class RootCheckPresenterImpl extends    AntivirusBasePresenterImpl<RootCheckView,
                                                                                BaseViewState,
                                                                                RootCheckModel>
                                          implements RootCheckPresenter {

    @Inject
    RootCheckPresenterImpl(@NonNull RootCheckModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull RootCheckView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new RootCheckModelObserverImpl(view));
        preCheck(view);
    }

    private boolean preCheck(@Nullable RootCheckView view) {
        if (mModel.isCheckCompleted()) {
            if (view != null) {
                view.showRootCheckResult(mModel.isRooted());
            }
            return true;
        }
        return false;
    }


    @Override
    public void checkRoot() {
        if (!preCheck(mView)) {
            mModel.checkRoot();
            if (mView != null) {
                mView.showProgressDialog();
            }
        }
    }

    private static final class RootCheckModelObserverImpl implements RootCheckModelObserver {
        private final RootCheckView mView;

        RootCheckModelObserverImpl(@NonNull RootCheckView view) {
            mView = view;
        }

        @Override
        public void onRootCheckResults(boolean isRooted) {
            mView.hideProgressDialog();
            mView.showRootCheckResult(isRooted);
        }

        @Override
        public void onError(@NonNull String message) {
            mView.hideProgressDialog();
            mView.showRootCheckFailed(message);
        }

        @Override
        public void onBasesUnavailable() {
            mView.hideProgressDialog();
            mView.showBasesUnavailable();
        }
    }
}
