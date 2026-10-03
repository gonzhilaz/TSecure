/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.model.BaseModel;

public interface SimpleUrlReputationModel extends BaseModel {
    void checkUrl(@NonNull String url, @Nullable UrlReputationListener listener);
}
