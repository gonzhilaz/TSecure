/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.model.app.sdk.Sdk;
import com.kavsdkexample.model.app.sdk.SdkWrappersFactory;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;
import com.kavsdkexample.model.app.sdk.license.impl.SdkLicenseInfoImpl;

public final class SdkWrappersFactoryImpl implements SdkWrappersFactory {
    private final Context mContext;

    public SdkWrappersFactoryImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    @NonNull
    public Sdk createSdk(final boolean initialized) {
        return new WcSdk(initialized);
    }

    @Override
    @NonNull
    public SdkLicenseInfo createLicenseInfo(@Nullable final Exception exception) {
        return new SdkLicenseInfoImpl(mContext, createSdk(exception == null), exception);
    }
}
