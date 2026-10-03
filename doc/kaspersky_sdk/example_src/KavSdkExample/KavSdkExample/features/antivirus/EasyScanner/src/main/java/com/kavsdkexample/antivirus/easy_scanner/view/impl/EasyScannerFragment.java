/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.view.impl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.antivirus.base.view.impl.ScanUiUtils;
import com.kavsdkexample.antivirus.easy_scanner.R;
import com.kavsdkexample.antivirus.easy_scanner.R2;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScannerPresenter;
import com.kavsdkexample.antivirus.easy_scanner.view.EasyScannerView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class EasyScannerFragment extends    AntivirusBaseFragment<EasyScannerView,
                                                                  BaseViewState,
                                                                  EasyScannerPresenter>
                                 implements EasyScannerView,
                                            OnClickListener,
                                            RadioGroup.OnCheckedChangeListener,
                                            View.OnTouchListener {
    @Inject
    EasyScannerPresenter mPresenter;

    @BindView(R2.id.RadioGroupActionEasyScannerMode)
    RadioGroup mEasyModeRadioGroup;

    @BindView(R2.id.RadioGroupActionIfNotCured)
    RadioGroup mEasyActionRadioGroup;

    @BindView(R2.id.StartScan)
    View mStartScanCommandView;

    @BindView(R2.id.PauseScan)
    View mPauseScanCommandView;

    private Unbinder mUnbinder;

    private TextView mStartScanCommandCaptionView;
    private TextView mPauseScanCommandCaptionView;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.easy_scanner_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        setEasyScannerMode(mPresenter.getEasyScannerMode());
        mEasyModeRadioGroup.setOnCheckedChangeListener(this);
        mEasyActionRadioGroup.setOnCheckedChangeListener(this);

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
    protected EasyScannerPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        UiUtils.silentRadioCheck(mEasyActionRadioGroup, ScanUiUtils.avActionToResId(action), this);
    }

    @Override
    public void setEasyScannerMode(@NonNull EasyScannerMode mode) {
        UiUtils.silentRadioCheck(mEasyModeRadioGroup, scannerModeToResId(mode), this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == mEasyActionRadioGroup) {
            mPresenter.setAvAction(ScanUiUtils.resIdToAvAction(checkedId));
        } else if (group == mEasyModeRadioGroup) {
            mPresenter.setEasyScannerMode(resIdToScannerMode(checkedId));
        }
    }

    @Override
    @OnClick({ R2.id.StartScan,
               R2.id.PauseScan })
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.StartScan) {
            mPresenter.processScanButtonClick();
        } else if (id == R.id.PauseScan) {
            mPresenter.processPauseResumeButtonClick();
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
    public void showError(@NonNull ScanObserver.ScanErrorType error) {
    }

    @Override
    public void showScanResults(@NonNull EasyScanResults scanResults) {
    }

    private static int scannerModeToResId(@NonNull EasyScannerMode mode) {
        switch (mode) {
            case Basic:
                return R.id.easyScannerBasic;
            case Light:
                return R.id.easyScannerLight;
            case LightPlus:
                return R.id.easyScannerLightPlus;
            case Recommended:
                return R.id.easyScannerRecommended;
            case Full:
                return R.id.easyScannerFull;
            default:
                throw new IllegalStateException("Unknown EasyScannerMode: " + mode);
        }
    }

    @NonNull
    private static EasyScannerMode resIdToScannerMode(int id) {
        if (id == R.id.easyScannerBasic) {
            return EasyScannerMode.Basic;
        } else if (id == R.id.easyScannerLight) {
            return EasyScannerMode.Light;
        } else if (id == R.id.easyScannerLightPlus) {
            return EasyScannerMode.LightPlus;
        } else if (id == R.id.easyScannerRecommended) {
            return EasyScannerMode.Recommended;
        } else if (id == R.id.easyScannerFull) {
            return EasyScannerMode.Full;
        } else {
            throw new IllegalStateException("Unknown EasyScannerMode resource id: " + id);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE && view.getParent() != null) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
        }

        view.onTouchEvent(motionEvent);
        return true;
    }

}
