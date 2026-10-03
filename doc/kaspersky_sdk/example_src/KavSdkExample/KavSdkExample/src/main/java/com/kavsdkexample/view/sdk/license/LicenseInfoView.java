/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.license;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.view.BaseView;

@UiThread
public interface LicenseInfoView extends BaseView {
    void showInstallationAndHardwareIds(@NonNull String installationId, @NonNull String hardwareId);
    void showValidLicenseInfo(boolean clientUserIdRequired, long expireDate);
    void showInvalidLicenseInfo(boolean clientUserIdRequired, boolean needNewCode, @NonNull String errorMessage);
    void showSdkGenericInitError(@NonNull String errorMessage);
    void enableRetryButton();
}
