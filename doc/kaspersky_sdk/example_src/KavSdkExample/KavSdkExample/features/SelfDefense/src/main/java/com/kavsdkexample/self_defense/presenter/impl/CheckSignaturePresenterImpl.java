/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.self_defense.model.SelfDefenseModel;
import com.kavsdkexample.self_defense.model.SelfDefenseModelAppSignCheckObserver;
import com.kavsdkexample.self_defense.presenter.CheckSignaturePresenter;
import com.kavsdkexample.self_defense.view.CheckSignatureView;
import com.kavsdkexample.self_defense.view.CheckSignatureViewState;

import javax.inject.Inject;

@UiThread
public final class CheckSignaturePresenterImpl extends    BasePresenterImpl<CheckSignatureView, CheckSignatureViewState, SelfDefenseModel>
                                               implements CheckSignaturePresenter {
    @Inject
    CheckSignaturePresenterImpl(@NonNull SelfDefenseModel model) {
        super(model);
    }


    @Override
    public void switchToSelfDefenseFeatureFragment() {
        mModel.switchToSelfDefenseFeatureView();
    }

    @Override
    public void checkAppSignature() {
        mModel.checkApplicationSignature();
    }

    @Override
    public void subscribe(@NonNull CheckSignatureView view, @Nullable CheckSignatureViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new SelfDefenseModelAppSignCheckObserverImpl(view));
    }

    private static class SelfDefenseModelAppSignCheckObserverImpl implements SelfDefenseModelAppSignCheckObserver {
        @NonNull
        private final CheckSignatureView mView;

        SelfDefenseModelAppSignCheckObserverImpl(@NonNull CheckSignatureView view) {
            mView = view;
        }


        @Override
        public void onApplicationCheckResult(@NonNull String result) {
            mView.showApplicationCheckResult(result);
        }
    }
}
