/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view.impl;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.utils.UiUtils;
import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.self_defense.R;
import com.kavsdkexample.self_defense.R2;
import com.kavsdkexample.self_defense.model.sdk.SdkIntentProvider;
import com.kavsdkexample.self_defense.presenter.SelfDefenseFeaturePresenter;
import com.kavsdkexample.self_defense.view.SelfDefenseFeatureView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;
import butterknife.ViewCollections;
import dagger.android.support.AndroidSupportInjection;

public class SelfDefenseFeatureFragment extends    BaseFragment<SelfDefenseFeatureView,
                                                                BaseViewState,
                                                                SelfDefenseFeaturePresenter>
                                        implements SelfDefenseFeatureView,
                                                   CompoundButton.OnCheckedChangeListener,
                                                   View.OnClickListener {

    private static final int NOTIFICATION_ACCESS_REQUEST = 1;

    @Inject
    SelfDefenseFeaturePresenter mPresenter;
    @Inject SdkIntentProvider    mIntentProvider;

    @BindView(R2.id.notification_switch)  SwitchCompat mNotificationAccessSwitch;
    @BindView(R2.id.autorestart_switch)   SwitchCompat mAutorestartSwitch;
    @BindView(R2.id.foreground_switch)    SwitchCompat mForegroundEnabledSwitch;

    @BindViews({R2.id.notification_switch,
                R2.id.autorestart_switch,
                R2.id.foreground_switch}) List<View>   mUnsavedViews;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.self_defense_feature_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);

        ViewCollections.run(mUnsavedViews, (unsavedView, index) -> unsavedView.setSaveEnabled(false));
        UiUtils.initCommandView(view, R.id.check_app_signature_button, R.string.str_self_defense_check_app_signature_button, this);
        mNotificationAccessSwitch.setEnabled(true);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIFICATION_ACCESS_REQUEST) {
            mPresenter.refreshNotificationAccessState();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.check_app_signature_button) {
            mPresenter.switchToCheckSignatureFragment();
        }
    }

    @NonNull
    @Override
    protected SelfDefenseFeaturePresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void setAutorestartState(boolean enabled) {
        UiUtils.setCheckedSilent(mAutorestartSwitch, enabled, this);
    }

    @Override
    public void setForegroundServiceState(boolean enabled) {
        UiUtils.setCheckedSilent(mForegroundEnabledSwitch, enabled, this);
    }

    @Override
    public void setNotificationAccessState(boolean enabled) {
        UiUtils.setCheckedSilent(mNotificationAccessSwitch, enabled, this);
    }

    @Override
    @OnCheckedChanged({ R2.id.notification_switch,
                        R2.id.autorestart_switch,
                        R2.id.foreground_switch})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.foreground_switch) {
            if (isChecked) {
                mPresenter.enableForegroundService();
            } else {
                mPresenter.disableForegroundService();
            }
        } else if (id == R.id.notification_switch) {
            startActivityForResult(mIntentProvider.getNotificationAccessIntent(), NOTIFICATION_ACCESS_REQUEST);
        } else if (id == R.id.autorestart_switch) {
            mPresenter.setSdkAutoRestartEnabled(isChecked);
        }
    }
}
