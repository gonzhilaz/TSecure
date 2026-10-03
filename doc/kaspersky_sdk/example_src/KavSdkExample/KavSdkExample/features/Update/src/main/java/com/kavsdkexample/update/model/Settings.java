/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface Settings {
    @NonNull
    UpdateModelComponentMode getUpdateModelComponent();

    void setUpdateModelComponent(@NonNull UpdateModelComponentMode component);

    @NonNull
    UpdateModelUpdateServerMode getUpdateModelUpdateServer();

    void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer);

    @NonNull
    String getUpdateServer();

    void setUpdateServer(@NonNull String updateServer);
}