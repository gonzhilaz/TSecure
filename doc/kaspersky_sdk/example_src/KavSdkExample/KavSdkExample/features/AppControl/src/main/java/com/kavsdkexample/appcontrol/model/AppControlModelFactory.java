/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.List;

@UiThread
public interface AppControlModelFactory {
    @NonNull
    AppControlModel createAppControlModel(@NonNull Context context,
                                          @NonNull ThreadManager threadManager,
                                          @NonNull Intent blockingIntent,
                                          @NonNull Settings settings,
                                          @NonNull List<String> mandatoryWhitelistedPackageNames);
}