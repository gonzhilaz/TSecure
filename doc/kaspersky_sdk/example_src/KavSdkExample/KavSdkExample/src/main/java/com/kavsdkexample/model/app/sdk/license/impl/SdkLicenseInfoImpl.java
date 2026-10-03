/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.license.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseDateTimeException;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.license.SdkLicenseNetworkException;
import com.kavsdkexample.R;
import com.kavsdkexample.model.app.sdk.Sdk;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;

public final class SdkLicenseInfoImpl implements SdkLicenseInfo {
    private final Sdk mSdk;
    @Nullable
    private final String  mErrorMessage;
    private final boolean mInitialActivationFailed;
    private final boolean mNeedNewCode;
    private final boolean mValid;
    private final boolean mClientUserIdRequired;
    private final long    mExpirationDate;

    public SdkLicenseInfoImpl(@NonNull Context context, @NonNull Sdk sdk, @Nullable Exception exception) {
        mSdk = sdk;
        if (exception == null) {
            SdkLicense license       = sdk.getLicense();
            mNeedNewCode             = false;
            mClientUserIdRequired    = license.isClientUserIDRequired();
            mErrorMessage            = null;
            mInitialActivationFailed = true;
            mValid                   = license.isValid();
            mExpirationDate          = license.getLicenseKeyExpireDate();

        } else {
            mInitialActivationFailed = sdk.isInitialized();
            mValid                   = false;
            mExpirationDate          = 0L;
            mClientUserIdRequired    = mInitialActivationFailed && sdk.getLicense().isClientUserIDRequired();
            String exceptionMessage = exception.getMessage();
            if (exception instanceof SdkLicenseNetworkException) {
                mNeedNewCode = false;
                mErrorMessage = context.getString(R.string.str_sdk_license_network_error, exceptionMessage);
            } else if (exception instanceof SdkLicenseDateTimeException) {
                mNeedNewCode = false;
                mErrorMessage = context.getString(R.string.str_sdk_license_date_error, exceptionMessage);
            } else if (exception instanceof SdkLicenseException) {
                mNeedNewCode = true;
                mErrorMessage = context.getString(R.string.str_sdk_license_invalid_error, exceptionMessage);
            } else {
                mNeedNewCode = false;
                mErrorMessage = exceptionMessage;
            }
        }
    }

    @Override
    public boolean isInitialActivationFailed() {
        return mInitialActivationFailed;
    }

    @Override
    public boolean isServerSideActivationError() {
        return mNeedNewCode;
    }

    @Override
    public boolean isClientUserIdRequired() {
        return mClientUserIdRequired;
    }

    @Nullable
    @Override
    public String getErrorMessage() {
        return mErrorMessage;
    }

    @Override
    public boolean isValid() {
        return mValid;
    }

    @Override
    public long getExpirationDate() {
        return mExpirationDate;
    }

    @Override
    @NonNull
    public String getHashOfHardwareId() {
        return mSdk.getHashOfHardwareId();
    }

    @Override
    @NonNull
    public String getInstallationId() {
        return mSdk.getInstallationId();
    }
}
