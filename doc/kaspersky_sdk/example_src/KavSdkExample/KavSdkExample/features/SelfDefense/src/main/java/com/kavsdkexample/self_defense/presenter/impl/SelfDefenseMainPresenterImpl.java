/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.self_defense.model.SelfDefenseModel;
import com.kavsdkexample.self_defense.model.SelfDefenseModelObserver;
import com.kavsdkexample.self_defense.presenter.SelfDefenseMainPresenter;
import com.kavsdkexample.self_defense.view.SelfDefenseMainView;
import com.kavsdkexample.self_defense.view.SelfDefenseMainViewState;

import javax.inject.Inject;

@UiThread
public final class SelfDefenseMainPresenterImpl extends    BasePresenterImpl<SelfDefenseMainView, SelfDefenseMainViewState, SelfDefenseModel>
                                                implements SelfDefenseMainPresenter {
    @Inject
    SelfDefenseMainPresenterImpl(@NonNull SelfDefenseModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull SelfDefenseMainView view, @Nullable SelfDefenseMainViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new SelfDefenseModelObserverImpl(view));
    }

    private static class SelfDefenseModelObserverImpl implements SelfDefenseModelObserver {
        @NonNull private final SelfDefenseMainView mView;

        SelfDefenseModelObserverImpl(@NonNull SelfDefenseMainView view) {
            mView = view;
        }

        @Override
        public void onChangeViewRequest(@NonNull ViewType viewType) {
            switch (viewType) {
                case CheckSignatureFragment:
                    mView.showCheckSignatureFragment();
                    break;
                case SelfDefenseFeatureFragment:
                    mView.showFeatureFragment();
                    break;
                default:
                    throw new IllegalStateException("Unknown view type: " + viewType);
            }
        }
    }
}
