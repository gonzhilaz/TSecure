/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.license;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.view.sdk.license.LicenseInfoView;

public interface LicenseInfoPresenter extends BasePresenter<LicenseInfoView, BaseViewState> {
    void activate(@NonNull String activationCode);
}
