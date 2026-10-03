/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.model.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.antiphishing.model.WebFilterInitObserver;
import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.antiphishing.repository.sdk.SdkLocalStatusObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public interface TasksFactory {
    @NonNull
    Runnable createWebFilterManagerInitTask(@NonNull ThreadManager         threadManager,
                                            @NonNull WebFilterInitObserver webFilterInitObserver);
    @NonNull
    SdkLocalStatusObserver createSdkLocalStatusObserver(@NonNull WebFilterModel webFilterModel, @NonNull ThreadManager threadManager);
}