/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.NonNull;

public interface TabInfoFactoryAndroid extends TabInfoFactory {
    @NonNull
    @Override
    TabInfoAndroid createTabInfo(@NonNull String tabName, @NonNull String tabClassName);
}
