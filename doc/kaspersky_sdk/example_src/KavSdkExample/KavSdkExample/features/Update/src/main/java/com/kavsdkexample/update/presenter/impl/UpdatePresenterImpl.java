/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.update.model.UpdateController;
import com.kavsdkexample.update.model.UpdateModel;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelObserver;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.model.UpdateResults;
import com.kavsdkexample.update.presenter.UpdatePresenter;
import com.kavsdkexample.update.view.UpdateView;
import com.kavsdkexample.update.view.UpdateViewState;

import java.util.ArrayList;

import javax.inject.Inject;

@UiThread
public final class UpdatePresenterImpl extends BasePresenterImpl<UpdateView, UpdateViewState, UpdateModel>
    implements UpdatePresenter, UpdateModelObserver {

    private ArrayList<String> mUpdateDetails;

    @Inject
    UpdatePresenterImpl(@NonNull UpdateModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull UpdateView view, @Nullable UpdateViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(this);
        if (viewCreated) {
            restore(view, state);
        }
    }

    @Override
    @NonNull
    public String getUpdateServer() {
        return mModel.getUpdateServer();
    }

    @NonNull
    public UpdateModelComponentMode getUpdateModelComponent() {
        return mModel.getUpdateModelComponent();
    }

    @NonNull
    public UpdateModelUpdateServerMode getUpdateModelUpdateServer() {
        return mModel.getUpdateModelUpdateServer();
    }

    @Override
    public void updateIsCompleted() {
        if (mView != null) {
            mView.updateIsCompleted();
        }
    }

    @Override
    public void executionDetails(UpdateResults result) {
        if (mView != null) {
            mView.executionDetails(result);
        }
    }

    @Override
    public void executionDetails(UpdateResults result, int code) {
        if (mView != null) {
            mView.executionDetails(result, code);
        }
    }

    @Override
    public void executionDetails(UpdateResults result, final String details) {
        if (mView != null) {
            mView.executionDetails(result, details);
        }
    }

    @Override
    public void updateIsRunning() {
        if (mView != null) {
            mView.updateIsRunning();
        }
    }


    @Override
    public void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer) {
        mModel.setUpdateModelUpdateServer(updateServer);
    }

    @Override
    public void setUpdateModelComponent(@NonNull UpdateModelComponentMode component) {
        mModel.setUpdateModelComponent(component);
    }

    @Override
    public void setUpdateServer(@NonNull String updateServer) {
        mModel.setUpdateServer(updateServer);
    }

    @Override
    public void updateRequest() {
        mModel.updateRequest();
    }

    @Override
    public void setController(UpdateController controller) {
    }

    @Override
    public void clearUpdateDetails() {
        mUpdateDetails.clear();
        if (mView != null) {
            mView.clearUpdateDetails();
        }
    }

    @Override
    public void appendUpdateDetails(@NonNull String details) {
        mUpdateDetails.add(details);
        if (mView != null) {
            mView.appendUpdateDetails(details);
        }
    }

    @NonNull
    @Override
    public ArrayList<String> getUpdateDetails() {
        return mUpdateDetails;
    }

    private void restore(@NonNull UpdateView view, UpdateViewState state) {
        UpdateModelUpdateServerMode updateServer;
        UpdateModelComponentMode component;
        String server;
        if (state != null) {
            updateServer = state.getUpdateModelUpdateServer();
            component = state.getUpdateModelComponent();
            server = state.getUpdateServer();
            mUpdateDetails = state.getUpdateDetails();
            view.enableStartUpdateButton(state.getStartUpdateButtonState());
        } else {
            updateServer = mModel.getUpdateModelUpdateServer();
            component = mModel.getUpdateModelComponent();
            server = mModel.getUpdateServer();
            mUpdateDetails = new ArrayList<>();
        }

        view.setUpdateModelComponent(component);
        view.setUpdateModelUpdateServer(updateServer);
        if (updateServer == UpdateModelUpdateServerMode.Specific) {
            view.setUpdateServer(server);
        }

        for (String details : mUpdateDetails) {
            view.appendUpdateDetails(details);
        }
    }
}
