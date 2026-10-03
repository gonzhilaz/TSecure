/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;

import java.util.ArrayList;

public interface UpdateViewState extends BaseViewState {
    @NonNull
    String getUpdateServer();

    @NonNull
    UpdateModelComponentMode getUpdateModelComponent();

    @NonNull
    UpdateModelUpdateServerMode getUpdateModelUpdateServer();

    @NonNull
    ArrayList<String> getUpdateDetails();

    boolean getStartUpdateButtonState();
}