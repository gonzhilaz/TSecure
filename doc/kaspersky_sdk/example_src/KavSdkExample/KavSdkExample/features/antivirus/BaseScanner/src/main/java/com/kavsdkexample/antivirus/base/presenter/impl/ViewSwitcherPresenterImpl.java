/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.model.ViewSwitchModel;
import com.kavsdkexample.antivirus.base.model.ViewSwitchModelObserver;
import com.kavsdkexample.antivirus.base.presenter.ViewSwitcherPresenter;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.antivirus.base.view.ViewSwitcherView;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

@UiThread
public final class ViewSwitcherPresenterImpl extends    BasePresenterImpl<ViewSwitcherView,
                                                                          BaseViewState,
                                                                          ViewSwitchModel>
                                             implements ViewSwitcherPresenter {
    @Inject
    ViewSwitcherPresenterImpl(@NonNull ViewSwitchModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull ViewSwitcherView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new ViewSwitchModelObserverImpl(view));
        if (mModel.needDisplayScanResults()) {
            view.showScanResultsView();
            mModel.resetDisplayScanResults();
        }
    }

    private static class ViewSwitchModelObserverImpl implements ViewSwitchModelObserver {
        @NonNull private final ViewSwitcherView mView;

        ViewSwitchModelObserverImpl(@NonNull ViewSwitcherView view) {
            mView = view;
        }

        @Override
        public void onChangeViewRequest(@NonNull ViewType viewType, @Nullable Object params) {
            switch (viewType) {
                case ScanView:
                    mView.showScanView();
                    break;
                case ScanResultsView:
                    mView.showScanResultsView();
                    break;
                case ThreatsInfoView:
                    if (params instanceof ThreatType) {
                        mView.showThreatsInfoView((ThreatType) params);
                    } else {
                        if (params == null) {
                            throw new IllegalStateException("Expected ThreatInfo parameter");
                        } else {
                            throw new IllegalStateException("Unexpected params type for ThreatInfoView: " + params.getClass().getName());
                        }
                    }
                    break;
                case Applications:
                    boolean odsApplications;
                    if (params != null) {
                        odsApplications = (Boolean)params;
                    } else {
                        odsApplications = false;
                    }
                    mView.showApplications(odsApplications);
                    break;
                default:
                    throw new IllegalStateException("Unknown view type: " + viewType);
            }
        }
    }
}
