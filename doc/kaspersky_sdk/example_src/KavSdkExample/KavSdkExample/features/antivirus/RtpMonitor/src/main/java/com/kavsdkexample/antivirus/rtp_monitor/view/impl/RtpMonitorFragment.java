/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kavsdk.antivirus.Antivirus;
import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.view.impl.AntivirusBaseFragment;
import com.kavsdkexample.antivirus.rtp_monitor.BuildConfig;
import com.kavsdkexample.antivirus.rtp_monitor.R;
import com.kavsdkexample.antivirus.rtp_monitor.R2;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RtpMonitorPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.view.RtpMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import java.text.MessageFormat;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class RtpMonitorFragment extends    AntivirusBaseFragment<RtpMonitorView, BaseViewState, RtpMonitorPresenter>
                                implements RtpMonitorView,
                                           OnClickListener,
                                           CompoundButton.OnCheckedChangeListener,
                                           RadioGroup.OnCheckedChangeListener,
                                           TextWatcher {
    private static final String TAG = RtpMonitorFragment.class.getSimpleName();

    @Inject                                         RtpMonitorPresenter mPresenter;
    @BindView(R2.id.RadioGroupRtpAction)            RadioGroup          mAvActionsGroup;
    @BindView(R2.id.openRtpMonitorDirectoryManager) View                mFoldersCommandView;
    @BindView(R2.id.RtpEnable)                      SwitchCompat        mEnableMonitorSwitch;
    @BindView(R2.id.RtpScanUds)                     SwitchCompat        mAllowUdsSwitch;
    @BindView(R2.id.RtpScanUdsOnly)                 SwitchCompat        mOnlyUdsSwitch;
    @BindView(R2.id.RtpDetectRiskwareAdware)        SwitchCompat        mCheckRiskwareSwitch;
    @BindView(R2.id.RtpScanSuspicious)              SwitchCompat        mDetectSuspicious;
    @BindView(R2.id.rtpMaxFileSize)                 EditText            mMaxFileCheckSize;

    private Unbinder mUnbinder;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.rtp_monitor_fragment, container, false);

        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);
        EditText editText = view.findViewById(R.id.rtpMaxFileSize);
        editText.addTextChangedListener(this);
        mAvActionsGroup.setOnCheckedChangeListener(this);


        mFoldersCommandView.setOnClickListener(this);
        mFoldersCommandView.setEnabled(true);
        TextView buttonCaption = mFoldersCommandView.findViewById(R.id.commandCaption);
        buttonCaption.setText(R.string.str_av_protection_open_rtp_monitor_directory_manager);

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
    @NonNull
    protected RtpMonitorPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    @Nullable
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void onMonitorStateChanged(boolean enabled) {
        mEnableMonitorSwitch.setEnabled(true);
        UiUtils.setCheckedSilent(mEnableMonitorSwitch, enabled, this);
    }

    @Override
    @OnClick({ R2.id.openRtpMonitorDirectoryManager })
    public void onClick(@NonNull View view) {
        int id = view.getId();
        if (id == R.id.openRtpMonitorDirectoryManager) {
            if (mEnableMonitorSwitch.isChecked()) {
                Toast.makeText(requireContext(),  R.string.str_av_protection_rtp_disable_monitor_to_edit_folders,
                        Toast.LENGTH_LONG).show();
                return;
            }
            Fragment parentFragment = getParentFragment();
            @SuppressWarnings("ConstantConditions")
            View containerView = parentFragment.getView();
            DirectoryManagerFragment fragment = new DirectoryManagerFragment();
            //noinspection ConstantConditions
            parentFragment
                    .getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(containerView.getId(), fragment, fragment.getFragmentTag()).commit();

        }
    }

    @Override
    @OnCheckedChanged({ R2.id.RtpEnable,
                        R2.id.RtpScanUdsOnly,
                        R2.id.RtpScanUds,
                        R2.id.RtpDetectRiskwareAdware,
                        R2.id.RtpScanSuspicious })
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.RtpEnable) {
            mEnableMonitorSwitch.setEnabled(false);
            mPresenter.enableMonitor(isChecked);
        } else if (id == R.id.RtpScanUdsOnly) {
            mPresenter.enableCloudOnlyCheck(isChecked);
        } else if (id == R.id.RtpScanUds) {
            mPresenter.enableCloudCheck(isChecked);
        } else if (id == R.id.RtpDetectRiskwareAdware) {
            mPresenter.enableRiskwareCheck(isChecked);
        } else if (id == R.id.RtpScanSuspicious) {
            mPresenter.enableSuspicious(isChecked);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rtpDelete) {
            mPresenter.setAvAction(AvAction.DeteteThreat);
        } else if (checkedId == R.id.rtpQuarantine) {
            mPresenter.setAvAction(AvAction.QuarantineThreat);
        } else if (checkedId == R.id.rtpSkip) {
            mPresenter.setAvAction(AvAction.SkipThreat);
        }
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        switch (action) {
            case DeteteThreat:
                UiUtils.silentRadioCheck(mAvActionsGroup, R.id.rtpDelete, this);
                break;
            case QuarantineThreat:
                UiUtils.silentRadioCheck(mAvActionsGroup, R.id.rtpQuarantine, this);
                break;
            case SkipThreat:
                UiUtils.silentRadioCheck(mAvActionsGroup, R.id.rtpSkip, this);
                break;
            default:
                throw new IllegalStateException("Unsupported av action " + action);
        }
    }

    @Override
    public void setMonitorEnabledState(boolean enabled) {
        UiUtils.setCheckedSilent(mEnableMonitorSwitch, enabled, this);
    }

    @Override
    public void setAllowCloudCheckState(boolean enabled) {
        UiUtils.setCheckedSilent(mAllowUdsSwitch, enabled, this);
    }

    @Override
    public void setDetectSuspicious(boolean enabled) {
        UiUtils.setCheckedSilent(mDetectSuspicious, enabled, this);
    }

    @Override
    public void setCloudOnlyCheckState(boolean enabled) {
        UiUtils.setCheckedSilent(mOnlyUdsSwitch, enabled, this);
    }

    @Override
    public void setRiskwareCheckState(boolean enabled) {
        UiUtils.setCheckedSilent(mCheckRiskwareSwitch, enabled, this);

    }

    @Override
    public void setMaxFileCheckSize(long size) {
        UiUtils.setTextSilent(mMaxFileCheckSize, String.valueOf(size), this);
    }

    //// TextWatcher begin -->>
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // do nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
        long maxFileSize = getLong(editable);
        mPresenter.setMaxFileCheckSize(maxFileSize);
    }
    //// <<-- TextWatcher end

    private static long getLong(Editable editable) {
        try {
            long l = Long.parseLong(editable.toString());
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.valueOf(l));
            }
            return l;
        } catch (NumberFormatException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, MessageFormat.format("Cannot parse {0}. Using {1}", editable, Antivirus.MONITOR_MAX_FILE_SIZE_NOT_SET), e);
            }
            return Antivirus.MONITOR_MAX_FILE_SIZE_NOT_SET;
        }
    }
}
