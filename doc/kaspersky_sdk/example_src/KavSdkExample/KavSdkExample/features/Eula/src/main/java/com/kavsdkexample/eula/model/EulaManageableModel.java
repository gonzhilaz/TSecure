/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.features.SdkFeature;

@UiThread
public interface EulaManageableModel extends EulaModel {
    void setTabCompletionCallback(@Nullable SdkFeature.TabCompletionCallback callback);
}
