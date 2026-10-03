/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;

import dagger.Module;
import dagger.Provides;

@Module
class FeatureModule {
    @Provides
    @NonNull
    static Context provideContext(@NonNull Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }
}

