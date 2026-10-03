/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.license.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.model.LicenseStatusObserver;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;
import com.kavsdkexample.presenter.license.LicenseInfoPresenter;
import com.kavsdkexample.view.sdk.license.LicenseInfoView;

import javax.inject.Inject;

public final class LicenseInfoPresenterImpl extends BasePresenterImpl<LicenseInfoView, BaseViewState, AppModel> implements LicenseInfoPresenter {
    @Inject
    LicenseInfoPresenterImpl(@NonNull AppModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull LicenseInfoView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new LicenseStatusObserverImpl(view));
        view.enableRetryButton();
    }

    @Override
    public void activate(@NonNull String activationCode) {
        if (mModel.getLicenseInfo().isInitialActivationFailed()) {
            if (mView != null) {
                mView.showProgressDialog();
            }
            mModel.activateLicense(activationCode);
        } else {
            mModel.maybeInitSdkAndFeatures(null, false);
        }
    }

    @Override
    public void onSdkInited() {
        if (mView != null) {
            mView.hideProgressDialog();
            SdkLicenseInfo info = mModel.getLicenseInfo();
            mView.showInstallationAndHardwareIds(info.getInstallationId(), info.getHashOfHardwareId());
            mView.showValidLicenseInfo(info.isClientUserIdRequired(), info.getExpirationDate());
            mView.enableRetryButton();
        }
    }

    @Override
    public void onSdkInitFailed() {
        if (mView != null) {
            mView.hideProgressDialog();
            processError(mModel.getLicenseInfo(), mView);
        }
    }

    private static void processError(@NonNull SdkLicenseInfo info, @NonNull LicenseInfoView view) {
        String errorMessage = info.getErrorMessage();
        if (errorMessage == null) {
            throw new IllegalStateException("Expected filled error message");
        }
        if (info.isInitialActivationFailed()) {
            view.showInstallationAndHardwareIds(info.getInstallationId(), info.getHashOfHardwareId());
            view.showInvalidLicenseInfo(info.isClientUserIdRequired(), info.isServerSideActivationError(), info.getErrorMessage());
        } else {
            view.showSdkGenericInitError(info.getErrorMessage());
        }
        view.enableRetryButton();
    }


    static class LicenseStatusObserverImpl implements LicenseStatusObserver {
        @NonNull private final LicenseInfoView mView;

        LicenseStatusObserverImpl(@NonNull LicenseInfoView view) {
            mView  = view;
        }

        @Override
        public void onSuccess(@NonNull SdkLicenseInfo info) {
            mView.hideProgressDialog();
            mView.showValidLicenseInfo(info.isClientUserIdRequired(), info.getExpirationDate());
            mView.enableRetryButton();
        }

        @Override
        public void onError(@NonNull SdkLicenseInfo info) {
            mView.hideProgressDialog();
            LicenseInfoPresenterImpl.processError(info, mView);
        }

    }
}
