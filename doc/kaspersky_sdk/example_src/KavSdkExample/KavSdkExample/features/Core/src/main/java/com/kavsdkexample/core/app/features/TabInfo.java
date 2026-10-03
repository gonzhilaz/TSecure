/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface TabInfo {
    @NonNull  String   getTabName();
    @NonNull  String   getTabClassName();
}
