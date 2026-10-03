/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.view.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.view.UpdateViewState;

import java.util.ArrayList;

public class UpdateViewStateImpl implements UpdateViewState {
    private String mUpdateServer;
    private UpdateModelComponentMode mUpdateModelComponent;
    private UpdateModelUpdateServerMode mUpdateModelUpdateServer;
    private ArrayList<String> mUpdateDetails;
    private boolean mStartUpdateButtonState;

    UpdateViewStateImpl(
        String            updateServer,
        int               updateModelComponent,
        int               updateModelUpdateServer,
        ArrayList<String> updateDetails,
        boolean           startUpdateButtonState) {
        mUpdateServer = updateServer;
        mUpdateModelComponent = UpdateModelComponentMode.values()[updateModelComponent];
        mUpdateModelUpdateServer = UpdateModelUpdateServerMode.values()[updateModelUpdateServer];
        mUpdateDetails = updateDetails;
        mStartUpdateButtonState = startUpdateButtonState;
    }

    @NonNull
    public String getUpdateServer() {
        return mUpdateServer;
    }

    @NonNull
    public UpdateModelComponentMode getUpdateModelComponent() {
        return mUpdateModelComponent;
    }

    @NonNull
    public UpdateModelUpdateServerMode getUpdateModelUpdateServer() {
        return mUpdateModelUpdateServer;
    }

    @NonNull
    public ArrayList<String>  getUpdateDetails() {
        return mUpdateDetails;
    }

    public boolean getStartUpdateButtonState() {
        return mStartUpdateButtonState;
    }
}