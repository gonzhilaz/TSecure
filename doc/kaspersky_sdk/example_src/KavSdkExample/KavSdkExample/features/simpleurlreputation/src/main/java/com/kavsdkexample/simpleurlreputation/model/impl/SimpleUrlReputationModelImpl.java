/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.model.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.kavsdkexample.simpleurlreputation.BuildConfig;
import com.kavsdkexample.simpleurlreputation.model.UrlReputationListener;
import com.kavsdkexample.simpleurlreputation.model.SimpleUrlReputationModel;
import com.kaspersky.components.urlchecker.UrlInfo;
import com.kavsdk.urlchecker.UrlCheckServiceFactory;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class SimpleUrlReputationModelImpl extends BaseModelImpl implements SimpleUrlReputationModel {

    private static final String TAG = SimpleUrlReputationModelImpl.class.getSimpleName();

    private final Context mContext;
    private final ThreadManager mThreadManager;
    private final ExecutorService mExecutorService;

    public SimpleUrlReputationModelImpl(@NonNull final Context context,
                                        @NonNull final ThreadManager threadManager,
                                        @NonNull final ExecutorService executorService) {
        mContext = context;
        mThreadManager = threadManager;
        mExecutorService = executorService;
    }

    @Override
    public void checkUrl(@NonNull String url, @Nullable final UrlReputationListener listener) {
        mExecutorService.submit(() -> {
            try {
                UrlInfo result = UrlCheckServiceFactory.createUrlCheckServiceInstance(mContext).checkUrlExt(new java.net.URL(url));
                mThreadManager.runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onSuccess(result);
                    }
                });
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Error requesting URL reputation", e);
                }
                mThreadManager.runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
            } catch (SdkLicenseViolationException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Error requesting URL reputation", e);
                }
                mThreadManager.runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onFailure(e.getLocalizedMessage());
                    }
                });
            }
        });
    }

}
