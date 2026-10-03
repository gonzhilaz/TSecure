/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.view.impl;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.fingerprintmonitor.R;
import com.kavsdkexample.fingerprintmonitor.presenter.FingerprintMonitorPresenter;
import com.kavsdkexample.fingerprintmonitor.view.FingerprintMonitorView;

import com.kavsdkexample.fingerprintmonitor.R2;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class FingerprintMonitorActivity extends BaseActivity<FingerprintMonitorView, BaseViewState, FingerprintMonitorPresenter>
        implements FingerprintMonitorView, OnCheckedChangeListener {
    private static final String LOG_TAG = FingerprintMonitorActivity.class.getSimpleName();

    @Inject
    FingerprintMonitorPresenter mPresenter;

    @BindView(R2.id.tv_monitor_state) TextView mMonitorStateTextView;
    @BindView(R2.id.switch_enable_monitor) SwitchCompat mMonitorEnabledSwitch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_monitor_activity);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);

        mMonitorEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPresenter.setMonitorEnabled(isChecked);
        });

        if (mPresenter.getMonitorEnabled()) {
            mMonitorStateTextView.setText(R.string.str_fingerprint_monitor_state_enabled);
        } else {
            mMonitorStateTextView.setText(R.string.str_fingerprint_monitor_state_disabled);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMonitorEnabledSwitch.setChecked(mPresenter.getMonitorEnabled());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final int id = buttonView.getId();
        if (id == R.id.switch_enable_monitor) {
            mPresenter.setMonitorEnabled(isChecked);
        }
    }

    @Override
    public void displayMonitorState(boolean enabled) {
        if (mPresenter.getMonitorEnabled()) {
            mMonitorStateTextView.setText(R.string.str_fingerprint_monitor_state_enabled);
        } else {
            if (enabled) {
                mMonitorStateTextView.setText(R.string.str_fingerprint_monitor_state_failed);
            } else {
                mMonitorStateTextView.setText(R.string.str_fingerprint_monitor_state_disabled);
            }
        }
    }

    @Override
    protected FingerprintMonitorPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected BaseViewState getViewState() {
        return null;
    }
}
