/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.appcontrol.model.AppControlObserver;
import com.kavsdkexample.appcontrol.view.WindowManagerBlockView;

public class AppControlObserverImpl implements AppControlObserver {

    private final WindowManagerBlockView mView;

    public AppControlObserverImpl(@NonNull final WindowManagerBlockView view) {
        mView = view;
    }

    @Override
    public void showDialog() {
        mView.showDialog();
    }

}
