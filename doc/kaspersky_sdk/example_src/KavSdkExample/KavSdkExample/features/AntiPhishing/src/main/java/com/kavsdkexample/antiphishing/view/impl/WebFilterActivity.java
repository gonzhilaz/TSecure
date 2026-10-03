/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.view.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Button;
import android.os.Build;
import android.text.InputType;
import android.content.res.Configuration;

import com.kavsdkexample.antiphishing.R;
import com.kavsdkexample.antiphishing.R2;
import com.kavsdkexample.antiphishing.presenter.WebFilterPresenter;
import com.kavsdkexample.antiphishing.view.WebFilterView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.antiphishing.utils.Consts;

import java.util.Locale;
import javax.inject.Inject;
import java.net.URL;
import java.net.MalformedURLException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.AndroidInjection;

public class WebFilterActivity extends BaseActivity<WebFilterView,
                                                    BaseViewState,
                                                    WebFilterPresenter>
                               implements WebFilterView,
                                          View.OnClickListener,
                                          CompoundButton.OnCheckedChangeListener {
    private static final String REMOVE_WEBFILTER_EXCLUDE_ITEM_DIALOG_ID = "REMOVE_WEBFILTER_EXCLUDE_ITEM_DIALOG_TAG";
    private static final String CATEGORY_DIALOG_ID = "CATEGORY_DIALOG_ID";
    private static final String SETUP_PROXY_PORT_DIALOG_ID = "SETUP_PROXY_PORT_DIALOG_ID";
    public static final int PORT_NUMBER = 3128;
    private boolean mIsInitialised;
    private WfItemsAdapter mWfItemsAdapter;
    private String mUrlToDleete;

    @Inject WebFilterPresenter mPresenter;
    @BindView(R2.id.wfItemsList) ListView mWfExcludeItemsList;
    @BindView(R2.id.WfEnable) SwitchCompat mWebFilterEnabledCheckBox;
    @BindView(R2.id.WfExtCategories) SwitchCompat mExtCategoriesEnabledCheckBox;
    @BindView(R2.id.WfIgnorePowerSaveMode) SwitchCompat mIgnorePowerSaveModeCheckBox;
    @BindView(R2.id.webFilterProxyPort) EditText mWebFilterPortEditText;
    @BindView(R2.id.switchWifiProxyState) SwitchCompat mWifiProxyStateView;
    @BindView(R2.id.selCategoryButton) Button mSelCategoryButton;
    @BindView(R2.id.accessibilyEnableButton) View mAccessibilyEnableButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webfilter_settings);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);

        mWebFilterEnabledCheckBox.setOnCheckedChangeListener(this);
        mExtCategoriesEnabledCheckBox.setOnCheckedChangeListener(this);
        mIgnorePowerSaveModeCheckBox.setOnCheckedChangeListener(this);
        mSelCategoryButton.setOnClickListener(this);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            mWifiProxyStateView.setEnabled(false);
        } else {
            mWifiProxyStateView.setOnCheckedChangeListener(this);
        }

        mAccessibilyEnableButton.setOnClickListener(this);
        mWfItemsAdapter = new WfItemsAdapter(this, mPresenter);
        mWfExcludeItemsList.setAdapter(mWfItemsAdapter);
        mWfExcludeItemsList.setOnItemClickListener((parent, view, position, id) -> {
            if (!view.isEnabled()) {
                return;
            }
            mUrlToDleete = mPresenter.getExclusionAt(position);
            if (mUrlToDleete != null && !mUrlToDleete.isEmpty()) {
                new RemoveWebFilterExcludeItemDialog().show(getSupportFragmentManager(), REMOVE_WEBFILTER_EXCLUDE_ITEM_DIALOG_ID);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPresenter.onPostCreate(this);
        mIsInitialised = true;
    }

    @Override
    public boolean isInitialised() {
        return mIsInitialised;
    }

    @Override
    public void disableAll() {
        mWebFilterEnabledCheckBox.setEnabled(false);
        disableAllWithoutWebFiltering(true);
    }

    private void disableAllWithoutWebFiltering(boolean isDisabled) {
        mExtCategoriesEnabledCheckBox.setEnabled(!isDisabled);
        mIgnorePowerSaveModeCheckBox.setEnabled(!isDisabled);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mWifiProxyStateView.setEnabled(!isDisabled);
        }
        mSelCategoryButton.setEnabled(!isDisabled);
        mAccessibilyEnableButton.setEnabled(!isDisabled);
        mWfExcludeItemsList.setEnabled(!isDisabled);
        mWebFilterPortEditText.setEnabled(!isDisabled);
    }

    @Override
    protected WebFilterPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    private void setCheckedSilent(CompoundButton button, boolean checked) {
        button.setOnCheckedChangeListener(null);
        button.setChecked(checked);
        button.setOnCheckedChangeListener(this);
    }

    @Override
    public void enableWebFiltering(boolean isEnabled) {
        setCheckedSilent(mWebFilterEnabledCheckBox, isEnabled);
        disableAllWithoutWebFiltering(!isEnabled);
    }

    @Override
    public void enableExtCategories(boolean isEnabled) {
        setCheckedSilent(mExtCategoriesEnabledCheckBox, isEnabled);
    }

    @Override
    public void enableIgnorePowerSaveMode(boolean isEnabled) {
        setCheckedSilent(mIgnorePowerSaveModeCheckBox, isEnabled);
    }

    @Override
    public void enableWifiProxy(boolean isEnabled) {
        setCheckedSilent(mWifiProxyStateView, isEnabled);
        mWifiProxyStateView.setText(isEnabled ?
            getString(R.string.str_webfilter_restore_wifi_proxy_button) :
            getString(R.string.str_webfilter_enable_wifi_proxy_button));
    }

    @Override
    public void setProxyPort(int port) {
        mWebFilterPortEditText.setText(String.valueOf(port));
    }

    @Override
    public void showInitError(@NonNull Exception e) {
        Toast.makeText(this, String.format(Locale.getDefault(), getString(R.string.str_anti_phishing_init_failed_error), e), Toast.LENGTH_LONG).show();
    }

    @Override
    @OnClick({R2.id.selCategoryButton,
        R2.id.accessibilyEnableButton})
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.selCategoryButton) {
            new WebFilterCategoryDialog().show(getSupportFragmentManager(), CATEGORY_DIALOG_ID);
        } else if (id == R.id.accessibilyEnableButton) {
            mPresenter.openAccessibilitySettings();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final int id = buttonView.getId();
        if (id == R.id.WfEnable) {
            mPresenter.enableWebFiltering(isChecked);
            if (isChecked) {
                new WebFilterCategoryDialog().show(getSupportFragmentManager(), CATEGORY_DIALOG_ID);
            }
        } else if (id == R.id.WfExtCategories) {
            mPresenter.enableExtCategories(isChecked);
        } else if (id == R.id.WfIgnorePowerSaveMode) {
            mPresenter.enableIgnorePowerSaveMode(isChecked);
        } else if (id == R.id.switchWifiProxyState) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                String port = mWebFilterPortEditText.getText().toString();
                mPresenter.enableWifiProxy(port.isEmpty() ? PORT_NUMBER : Integer.parseInt(port), isChecked);
            }
        }
    }

    @Override
    public void notificationOfWbFilterNotWorking() {
        Toast.makeText(this, getString(R.string.web_filter_not_working), Toast.LENGTH_LONG).show();
    }

    @Override
    public void notificationOfTaskReputationNotWorking() {
        Toast.makeText(this, getString(R.string.task_reputation_not_working), Toast.LENGTH_LONG).show();
    }

    @Override
    public void changeProxyPort() {
        new SetUpProxyPortDialog().show(getSupportFragmentManager(), SETUP_PROXY_PORT_DIALOG_ID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String urlString = intent.getStringExtra(Consts.EXTRA_URL);
        if (urlString == null) {
            return;
        }

        try {
            URL url = new URL(urlString);
            // Let's add a whole domain into the exceptions list
            String host = url.getHost() + "/*";
            mPresenter.addExclusion(host);
            mPresenter.saveExclusions();
            mWfItemsAdapter.notifyDataSetChanged();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateExclusionList() {
        mWfItemsAdapter.notifyDataSetChanged();
    }


    public static class RemoveWebFilterExcludeItemDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            if (!(activity instanceof WebFilterActivity)) {
                throw new IllegalStateException("Unsupported activity");
            }
            final WebFilterActivity webFilterActivity = (WebFilterActivity)activity;
            setRetainInstance(true);
            return new AlertDialog.Builder(webFilterActivity)
                    .setTitle(R.string.str_remove_web_filter_dialog_title)
                    .setMessage(R.string.str_remove_web_filter_dialog_message)
                    .setPositiveButton(R.string.str_remove_web_filter_dialog_positive, (dialog, which) -> {
                        if (webFilterActivity.mUrlToDleete != null) {
                            // Do no rely on index here, because there is a
                            // possibility that exclusion list could
                            // be changed by some other task
                            // while user was watching on the dialog
                            for (int i = 0; i <  webFilterActivity.mPresenter.getExclusionsCount(); ++i) {
                                if (webFilterActivity.mPresenter.getExclusionAt(i).equals(webFilterActivity.mUrlToDleete)) {
                                    webFilterActivity.mPresenter.removeExclusion(i);
                                    webFilterActivity.mPresenter.saveExclusions();
                                    webFilterActivity.mWfItemsAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.str_remove_web_filter_dialog_negative, null)
                    .create();
        }

        @Override
        public void onDestroyView() {

            if (getDialog() != null && getRetainInstance()) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }

    public static class WebFilterCategoryDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            if (!(activity instanceof WebFilterActivity)) {
                throw new IllegalStateException("Unsupported activity");
            }
            final WebFilterActivity webFilterActivity = (WebFilterActivity)activity;
            setRetainInstance(true);
            return new AlertDialog.Builder(webFilterActivity)
                    .setTitle(R.string.str_web_filter_category_dialog_title)
                    .setMultiChoiceItems(webFilterActivity.mPresenter.getCategoryNames(), webFilterActivity.mPresenter.getCheckedItems(), (dialog, which, isChecked) -> {
                        webFilterActivity.mPresenter.setCategoryEnabled(which, isChecked);
                        webFilterActivity.mPresenter.saveCategories();
                    })
                    .setPositiveButton(R.string.str_web_filter_category_dialog_positive, null)
                    .create();
        }

        @Override
        public void onDestroyView() {

            if (getDialog() != null && getRetainInstance()) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }

    public static class SetUpProxyPortDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            if (!(activity instanceof WebFilterActivity)) {
                throw new IllegalStateException("Unsupported activity");
            }
            final WebFilterActivity webFilterActivity = (WebFilterActivity)activity;
            final EditText input = new EditText(webFilterActivity);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setRawInputType(Configuration.KEYBOARD_12KEY);
            setRetainInstance(true);
            return new AlertDialog.Builder(webFilterActivity)
                    .setTitle(R.string.webfilter_new_proxy_port_dialog_title)
                    .setView(input)
                    .setPositiveButton(getString(R.string.ok_button_text), (dialog, whichButton) -> {
                        String str = input.getText().toString();
                        if ("".equals(str)) {
                            return;
                        }
                        webFilterActivity.mPresenter.enableWifiProxy(Integer.parseInt(str), webFilterActivity.mPresenter.getSavedWifiProxy());
                    })
                    .create();
        }

        @Override
        public void onDestroyView() {

            if (getDialog() != null && getRetainInstance()) {
                getDialog().setOnDismissListener(null);
            }
            super.onDestroyView();
        }
    }
}
