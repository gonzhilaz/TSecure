/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.view.impl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.kavsdkexample.antivirus.pua_monitor.R;
import com.kavsdkexample.antivirus.pua_monitor.R2;
import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.antivirus.pua_monitor.presenter.PuaMonitorPresenter;
import com.kavsdkexample.antivirus.pua_monitor.view.PuaMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class PuaMonitorFragment extends    AntivirusBaseFragment<PuaMonitorView, BaseViewState, PuaMonitorPresenter>
                                implements PuaMonitorView,
                                           CompoundButton.OnCheckedChangeListener {
    @Inject
    PuaMonitorPresenter mPresenter;
    @BindView(R2.id.EnableApkInstallation)
    SwitchCompat mEnableMonitorSwitch;

    private Unbinder mUnbinder;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.pua_monitor_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public boolean onBackPressed() {
        requireFragmentManager().popBackStack();
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void setMonitorEnabledState(boolean enabled) {
        UiUtils.setCheckedSilent(mEnableMonitorSwitch, enabled, this);
    }

    @Override
    @OnCheckedChanged({ R2.id.EnableApkInstallation })
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.EnableApkInstallation) {
            mEnableMonitorSwitch.setEnabled(false);
            mPresenter.enableMonitor(isChecked);
        }
    }

    @Override
    public void onMonitorStateChanged(boolean enabled) {
        mEnableMonitorSwitch.setEnabled(true);
        UiUtils.setCheckedSilent(mEnableMonitorSwitch, enabled, this);
    }

    @NonNull
    @Override
    protected PuaMonitorPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void setAllowCloudCheckState(boolean enabled) {}

    @Override
    public void setCloudOnlyCheckState(boolean enabled) {}

    @Override
    public void setRiskwareCheckState(boolean enabled) {}

    @Override
    public void setMaxFileCheckSize(long size) {}

    @Override
    public void setProcessMissedState(boolean enabled) {}
}
