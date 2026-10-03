/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model.sdk.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.protection.Protection;
import com.kavsdk.shared.iface.KavError;
import com.kavsdkexample.core.app.utils.IoUtils;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.self_defense.BuildConfig;
import com.kavsdkexample.self_defense.R;
import com.kavsdkexample.self_defense.model.sdk.SdkManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

final class CheckAppSignatureTask implements Runnable {
    @NonNull private final Context                              mContext;
    @NonNull private final String                               mFileName;
    @NonNull private final ThreadManager                        mThreadManager;
    @NonNull private final SdkManager.AppSignatureCheckObserver mObserver;

    CheckAppSignatureTask(@NonNull Context context,
                          @NonNull ThreadManager threadManager,
                          @NonNull String fileName,
                          @NonNull SdkManager.AppSignatureCheckObserver observer) {
        mContext       = context;
        mFileName      = fileName;
        mThreadManager = threadManager;
        mObserver      = observer;
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }

        String result;
        try {
            // Getting the certificate from assets
            X509Certificate supposedCert = readCertificateFromAssets(mContext, mFileName);

            Protection service = new Protection(mContext);

            // Comparing the application signature to the certificate
            result = convertErrorToString(mContext, service.verifyAppSignature(supposedCert));
        } catch (SdkLicenseViolationException e) {
            result = mContext.getString(R.string.str_kav_error_license_expired);

        } catch (CertificateException e) {
            result = mContext.getString(R.string.str_self_defense_error_wrong_certificate);

        } catch (IOException e) {
            result = mContext.getString(R.string.str_self_defense_error_reading_certificate);
        }

        final String signatureCheckResult = result;
        mThreadManager.runOnUiThread(() -> mObserver.onAppSignatureCheckResult(signatureCheckResult));
    }

    private static String convertErrorToString(@NonNull Context context, int error) {
        switch (error) {
            case KavError.KAV_OK:
                return context.getString(R.string.str_kav_error_ok);
            case KavError.KAV_PROTECTION_CERTIFICATE_NOT_MATCH:
                return context.getString(R.string.str_self_defense_error_protection_certificate_not_match);
            case KavError.KAV_PROTECTION_INTEGRITY_CHECK_FAILED:
                return context.getString(R.string.str_self_defense_error_kav_protection_integrity_check_failed);
            default:
                throw new IllegalArgumentException("Unknown error code");
        }
    }

    /**
    * Reads the certificate from assets
    **/
    private static X509Certificate readCertificateFromAssets(@NonNull Context context, @NonNull String filename)
            throws IOException, CertificateException {

        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(context.getAssets().open(filename));
            CertificateFactory factory = CertificateFactory.getInstance("X509");
            return (X509Certificate) factory.generateCertificate(in);
        }
        finally {
            IoUtils.closeQuietly(in);
        }
    }
}
