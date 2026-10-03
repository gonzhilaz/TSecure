/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.eula.model.EulaModel;
import com.kavsdkexample.eula.model.EulaModelStatusObserver;
import com.kavsdkexample.eula.presenter.EulaPresenter;
import com.kavsdkexample.eula.view.EulaView;
import com.kavsdkexample.eula.view.EulaViewState;

import javax.inject.Inject;

@UiThread
public final class EulaPresenterImpl extends    BasePresenterImpl<EulaView, EulaViewState, EulaModel>
                                     implements EulaPresenter {
    @Inject
    EulaPresenterImpl(@NonNull EulaModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull EulaView view, @Nullable EulaViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new ModelStatusObserver(view));
        if (viewCreated) {
            if (state == null || !state.isEulaShown()) {
                mModel.loadEula();
            }
        }
    }

    @Override
    public void acceptEula() {
        mModel.acceptEula();
    }

    @Override
    public void declineEula() {
        mModel.rejectEula();
    }

    static class ModelStatusObserver implements EulaModelStatusObserver {
        private final EulaView mView;
        ModelStatusObserver(@NonNull EulaView view) {
           mView = view;
        }

        @Override
        public void onEulaLoaded(@NonNull String eulaText) {
            mView.showEula(eulaText);
        }
    }
}
