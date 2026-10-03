/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;

@UiThread
public interface LicenseStatusObserver {
    void onSuccess(@NonNull SdkLicenseInfo info);
    void onError(@NonNull SdkLicenseInfo info);
}
