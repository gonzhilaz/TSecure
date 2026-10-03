/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabDescription;

final class TabDescriptionImpl implements TabDescription {
    @NonNull  private final TabDescription.TabId   mTabId;
    @Nullable private final SdkFeature             mFeature;

    TabDescriptionImpl(@NonNull TabDescription.TabId tabId,
                       @Nullable SdkFeature          feature) {
        mTabId   = tabId;
        mFeature = feature;
    }

    @Override
    @NonNull
    public TabId getTabId() {
        return mTabId;
    }

    @Override
    @Nullable
    public SdkFeature getFeature() {
        return mFeature;
    }
}
