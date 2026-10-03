/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

@Keep
public interface SdkFeatureAndroid extends SdkFeature {

    @UiThread
    @Override
    TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback);

    @UiThread
    void initUiView(@NonNull Fragment fragment, @NonNull ViewGroup viewGroup);

    @UiThread
    void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup);

    @UiThread
    boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent);

}
