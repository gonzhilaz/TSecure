/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.view.impl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.view.impl.DetectedApplicationsActivity;
import com.kavsdkexample.antivirus.base.view.impl.ScanUiUtils;
import com.kavsdkexample.antivirus.pua_scanner.R;
import com.kavsdkexample.antivirus.pua_scanner.R2;
import com.kavsdkexample.antivirus.pua_scanner.presenter.PuaScanPresenter;
import com.kavsdkexample.antivirus.pua_scanner.view.PuaScanView;
import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class PuaScannerFragment extends    AntivirusBaseFragment<PuaScanView,
                                                                 BaseViewState,
                                                                 PuaScanPresenter>
                                implements PuaScanView, View.OnClickListener {

    @Inject
    PuaScanPresenter mPuaScanPresenter;

    @BindView(R2.id.StartScan)
    View mStartScanCommandView;

    @BindView(R2.id.PauseScan)
    View mPauseScanCommandView;

    private Unbinder mUnbinder;

    private TextView mStartScanCommandCaptionView;
    private TextView mPauseScanCommandCaptionView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.pua_scanner_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        mStartScanCommandCaptionView = mStartScanCommandView.findViewById(R.id.commandCaption);
        mPauseScanCommandCaptionView = mPauseScanCommandView.findViewById(R.id.commandCaption);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    protected PuaScanPresenter getPresenter() {
        return mPuaScanPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    @OnClick({ R2.id.StartScan, R2.id.PauseScan })
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.StartScan) {
            mPuaScanPresenter.processScanButtonClick();
        } else if (id == R.id.PauseScan) {
            mPuaScanPresenter.processPauseResumeButtonClick();
        }
    }

    @Override
    public void setScanButtonState(@NonNull ScanButtonState state) {
        if (mStartScanCommandView != null) {
            ScanUiUtils.setScanButtonState(state, mStartScanCommandCaptionView, mStartScanCommandView, mPauseScanCommandView);
        }
    }

    @Override
    public void setPauseButtonState(@NonNull PauseButtonState state) {
        if (mPauseScanCommandCaptionView != null) {
            ScanUiUtils.setPauseButtonState(state, mPauseScanCommandCaptionView, mPauseScanCommandView);
        }
    }

    @Override
    public boolean onBackPressed() {
        requireFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {}

    @Override
    public void showScanResults(@NonNull ScanResults scanResults) {
        DetectedApplicationsActivity.launch(requireContext(), null, DetectedApplicationsActivity.TabType.Ods);
    }

    @Override
    public void showError(@NonNull ScanObserver.ScanErrorType error) {}
}
