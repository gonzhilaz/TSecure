/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.view.UpdateView;
import com.kavsdkexample.update.view.UpdateViewState;

import java.util.ArrayList;

@UiThread
public interface UpdatePresenter extends BasePresenter<UpdateView, UpdateViewState> {
    void updateRequest();
    void setUpdateModelComponent(@NonNull UpdateModelComponentMode component);
    void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer);
    void setUpdateServer(@NonNull String updateServer);
    @NonNull
    String getUpdateServer();
    @NonNull
    UpdateModelComponentMode getUpdateModelComponent();
    @NonNull
    UpdateModelUpdateServerMode getUpdateModelUpdateServer();
    void clearUpdateDetails();
    void appendUpdateDetails(@NonNull String details);
    @NonNull
    ArrayList<String> getUpdateDetails();
}
