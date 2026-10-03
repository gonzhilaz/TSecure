/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.model.UpdateResults;

public interface UpdateView extends BaseView {
    void updateIsCompleted();
    void updateIsRunning();
    void executionDetails(UpdateResults result);
    void executionDetails(UpdateResults result, int code);
    void executionDetails(UpdateResults result, final String details);
    void setUpdateServer(@NonNull String updateServer);
    void setUpdateModelComponent(@NonNull UpdateModelComponentMode component);
    void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer);
    void clearUpdateDetails();
    void appendUpdateDetails(@NonNull String details);
    void enableStartUpdateButton(boolean enable);
}
