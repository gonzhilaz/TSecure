/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.SdkFeature;

import java.util.Collection;

public interface SdkFeatureProvider {
    @Nullable
    <T extends SdkFeature>T findSdkFeature(@NonNull String name);

    @NonNull
    Collection<SdkFeature> allFeatures();
}
