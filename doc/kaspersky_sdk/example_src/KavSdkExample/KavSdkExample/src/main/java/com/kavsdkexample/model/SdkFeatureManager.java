/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model;


import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.SdkStatusObserver;

public interface SdkFeatureManager extends SdkFeatureProvider,
                                           SdkStatusObserver {
    void combineFeaturesInjectors(@NonNull ExtendableInjector<?> injector);
}
