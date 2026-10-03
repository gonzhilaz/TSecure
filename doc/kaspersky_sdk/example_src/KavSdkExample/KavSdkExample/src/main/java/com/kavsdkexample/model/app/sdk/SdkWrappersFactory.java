/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;

public interface SdkWrappersFactory {
    @NonNull
    Sdk createSdk(boolean initialized);

    @NonNull
    SdkLicenseInfo createLicenseInfo(@Nullable Exception exception);
}
