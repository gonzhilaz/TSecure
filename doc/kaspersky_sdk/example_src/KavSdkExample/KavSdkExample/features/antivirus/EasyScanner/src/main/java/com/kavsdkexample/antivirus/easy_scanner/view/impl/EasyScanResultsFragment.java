/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.scanner.R2;
import com.kavsdkexample.antivirus.base.view.ScanResultsView;
import com.kavsdkexample.antivirus.base.view.impl.ScanResultsFragment;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScanResultsPresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public final class EasyScanResultsFragment extends ScanResultsFragment<ScanResultsView<EasyScanResults>,
                                                                       BaseViewState,
                                                                       EasyScanResultsPresenter,
                                                                       EasyScanResults> {

    @Inject EasyScanResultsPresenter mPresenter;

    @BindView(R2.id.filesRootedCaption)  TextView  mFilesRootedText;
    @BindView(R2.id.rootScanLayout)      ViewGroup mEasyScanAdditionalFields;

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void showScanResults(@NonNull EasyScanResults scanResults) {
        super.showScanResults(scanResults);
        mEasyScanAdditionalFields.setVisibility(View.VISIBLE);
        mFilesRootedText.setText(String.valueOf(scanResults.isRooted()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    protected EasyScanResultsPresenter getPresenter() {
        return mPresenter;
    }
}
