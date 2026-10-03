/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.sdk.impl;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import android.content.Context;
import android.content.Intent;

import com.kavsdkexample.appcontrol.model.AppControlManager;
import com.kavsdkexample.appcontrol.model.AppControlModelFactory;
import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.appcontrol.model.Settings;
import com.kavsdkexample.appcontrol.model.impl.AppControlModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.List;

import javax.inject.Provider;

@UiThread
public class AppControlModelFactoryImpl implements AppControlModelFactory {
    @NonNull
    public AppControlModel createAppControlModel(@NonNull final Context context,
                                                 @NonNull final ThreadManager threadManager,
                                                 @NonNull final Intent blockingIntent,
                                                 @NonNull final Settings settings,
                                                 @NonNull final List<String> mandatoryWhitelistedPackageNames) {
        final Provider<AppControlManager> appControlManagerProvider =
                () -> new AppControlManagerImpl(threadManager, context, blockingIntent);
        return new AppControlModelImpl(
                settings, mandatoryWhitelistedPackageNames, appControlManagerProvider
        );
    }
}