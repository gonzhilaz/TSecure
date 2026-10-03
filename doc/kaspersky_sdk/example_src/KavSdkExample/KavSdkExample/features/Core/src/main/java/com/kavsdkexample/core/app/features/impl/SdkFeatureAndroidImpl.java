/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features.impl;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.kavsdkexample.core.app.features.SdkFeatureAndroid;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;

import dagger.android.AndroidInjector;

public abstract class SdkFeatureAndroidImpl extends SdkFeatureImpl implements SdkFeatureAndroid {
    @NonNull
    protected final Context                mContext;

    protected boolean dependsOnSdkInit() {
        return true;
    }

    protected SdkFeatureAndroidImpl(@NonNull Application            application,
                                    @NonNull ServiceInteractor      serviceInteractor,
                                    @NonNull NotificationInteractor notificationInteractor) {
        super(serviceInteractor, notificationInteractor);
        mContext                = application.getApplicationContext();
    }

    @Override
    public void initUiView(@NonNull Fragment fragment, @NonNull ViewGroup viewGroup) {
        throw new UnsupportedOperationException("Feature " + getName() + " does not support view injection for fragment");
    }

    @Override
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        throw new UnsupportedOperationException("Feature " + getName() + " does not support view injection for activity");
    }

    @Override
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        throw new UnsupportedOperationException("Feature " + getName() + " does not support tab creation");
    }
}
