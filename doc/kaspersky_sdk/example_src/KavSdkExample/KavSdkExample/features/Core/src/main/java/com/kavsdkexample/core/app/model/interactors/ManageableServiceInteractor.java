/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.model.interactors;

import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.SdkFeature;

public interface ManageableServiceInteractor extends ServiceInteractor {
    void setServiceManagementFeature(@Nullable SdkFeature feature);
}
