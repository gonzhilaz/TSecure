/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;

public final class TabInfoFactoryImpl implements TabInfoFactoryAndroid {
    @Override
    @NonNull
    public TabInfoAndroid createTabInfo(@NonNull String tabName, @NonNull String tabClassName) {
        return new TabInfoImpl(tabName, tabClassName);
    }

    @Override
    @NonNull
    public TabDescription createTabDescription(@NonNull TabDescription.TabId   tabId,
                                               @Nullable SdkFeature            feature) {
        return new TabDescriptionImpl(tabId, feature);
    }
}
