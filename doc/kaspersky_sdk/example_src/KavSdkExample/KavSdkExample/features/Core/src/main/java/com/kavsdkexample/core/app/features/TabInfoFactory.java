/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface TabInfoFactory {
    @NonNull
    TabInfo        createTabInfo(@NonNull String tabName, @NonNull String tabClassName);
    @NonNull
    TabDescription createTabDescription(@NonNull  TabDescription.TabId   tabId,
                                        @Nullable SdkFeature             feature);
}
