/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.license.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kavsdkexample.R;
import com.kavsdkexample.R2;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseFragment;
import com.kavsdkexample.presenter.license.LicenseInfoPresenter;
import com.kavsdkexample.view.sdk.license.LicenseInfoView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 *  Shows different SDK initialization and license activation errors or license info for successful activation
 */
public class LicenseInfoFragment extends    BaseFragment<LicenseInfoView, BaseViewState, LicenseInfoPresenter>
                                 implements LicenseInfoView,
                                            View.OnClickListener {

    @Inject                           LicenseInfoPresenter mPresenter;
    @BindView(R2.id.activate_button)  Button               mButton;
    @BindView(R2.id.textCode)         TextView             mCodeTextView;
    @BindView(R2.id.editCode)         EditText             mCodeEditText;
    @BindView(R2.id.errorMessage)     TextView             mErrorMessageTextView;
    @BindView(R2.id.expired)          TextView             mExpiredDateTextView;
    @BindView(R2.id.hashOfHardwareId) TextView             mHashOfHardwareIdTextView;
    @BindView(R2.id.installationId)   TextView             mInstallationIdTextView;
    @BindView(R2.id.topLayout)        RelativeLayout       mTopLayout;

    private final List<View> mRemovedViews = new ArrayList<>();
    private       Unbinder   mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.license_info_fragment, container, false);
        AndroidSupportInjection.inject(this);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @NonNull
    @Override
    protected LicenseInfoPresenter getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Override
    protected BaseViewState getViewState() {
        return null;
    }


    @Override
    @OnClick(R2.id.activate_button)
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.activate_button && view.isEnabled()) {
            view.setEnabled(false);
            mPresenter.activate(mCodeEditText.getText().toString());
            mCodeEditText.setText("");
        }
    }

    @Override
    public void showValidLicenseInfo(boolean clientUserIdRequired, long expireDate) {
        restoreLicenseViews();
        long expiredTimestamp = TimeUnit.SECONDS.toMillis(expireDate);
        if (expiredTimestamp == 0L) {
            mExpiredDateTextView.setText(R.string.str_license_not_available);
        } else {
            mExpiredDateTextView.setText(DateFormat.getDateTimeInstance().format(new Date(expiredTimestamp)));
        }
        if (clientUserIdRequired) {
            showClientUserIdControls();
        } else {
            mCodeTextView.setVisibility(View.GONE);
            mCodeEditText.setVisibility(View.GONE);
            mButton.setVisibility(View.GONE);
        }
        updateResultInfoLabel(getString(R.string.str_license_successful_activation), true);
    }

    @Override
    public void showInvalidLicenseInfo(boolean clientUserIdRequired, boolean needNewCode, @NonNull String errorMessage) {
        restoreLicenseViews();
        mExpiredDateTextView.setText(R.string.str_license_not_available);
        mButton.setVisibility(View.VISIBLE);
        updateResultInfoLabel(errorMessage, false);
        if (needNewCode) {
            mCodeTextView.setVisibility(View.VISIBLE);
            mCodeEditText.setVisibility(View.VISIBLE);
            if (clientUserIdRequired) {
                showClientUserIdControls();
            } else {
                mCodeTextView.setText(R.string.str_enter_code_activation_code_label);
                mCodeEditText.setHint(R.string.str_enter_code_activation_code_hint);
                mButton.setText(R.string.str_enter_code_activate);
            }
        } else {
            mCodeTextView.setVisibility(View.GONE);
            mCodeEditText.setVisibility(View.GONE);
            mButton.setText(R.string.str_license_retry);
        }
    }

    private void showClientUserIdControls() {
        mCodeTextView.setText(R.string.str_enter_code_user_id_label);
        mCodeEditText.setHint(R.string.str_enter_code_user_id_hint);
        mButton.setText(R.string.str_enter_code_send);
    }

    @Override
    public void showInstallationAndHardwareIds(@NonNull String installationId, @NonNull String hardwareId) {
        mInstallationIdTextView.setText(installationId);
        mHashOfHardwareIdTextView.setText(hardwareId);
    }

    @Override
    @SuppressWarnings("ResourceType")
    public void showSdkGenericInitError(@NonNull String errorMessage) {
        final int errorMsgViewId = 1;
        removeLicenseViews();
        TextView errorView = mTopLayout.findViewById(errorMsgViewId);
        final boolean errorViewNotFound = errorView == null;
        if (errorViewNotFound) {
            errorView = new TextView(getContext());
        }
        final String errorStr = String.format(Locale.getDefault(),
                getString(R.string.str_sdk_initialization_error),
                errorMessage);
        errorView.setText(errorStr);
        errorView.setId(errorMsgViewId);
        final RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.CENTER_IN_PARENT, errorMsgViewId);
        errorView.setLayoutParams(params);

        if (errorViewNotFound) {
            mTopLayout.addView(errorView, params);
        }
        mButton.setText(R.string.str_license_retry);
    }

    private void removeLicenseViews() {
        if (mRemovedViews.isEmpty()) {
            int viewCount = mTopLayout.getChildCount();
            for (int i = 0; i < viewCount; ++i) {
                mRemovedViews.add(mTopLayout.getChildAt(i));
            }
            mTopLayout.removeAllViews();
        }
    }
    private void restoreLicenseViews() {
        if (!mRemovedViews.isEmpty()) {
            mTopLayout.removeAllViews();
            for (View view : mRemovedViews) {
                mTopLayout.addView(view);
            }
            mRemovedViews.clear();
        }
    }

    @Override
    public void enableRetryButton() {
        mButton.setEnabled(true);
    }

    private void updateResultInfoLabel(final CharSequence text, boolean success) {
        mErrorMessageTextView.setText(text);
        mErrorMessageTextView.setTextColor(getResources().getColor(success? R.color.success: R.color.error));
    }
}
