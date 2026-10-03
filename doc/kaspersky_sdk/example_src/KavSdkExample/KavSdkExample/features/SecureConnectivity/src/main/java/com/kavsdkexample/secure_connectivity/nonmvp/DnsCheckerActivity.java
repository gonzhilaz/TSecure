/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kavsdk.dnschecker.DnsCheckResult;
import com.kavsdk.dnschecker.DnsCheckVerdict;
import com.kavsdk.dnschecker.DnsChecker;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.secure_connectivity.R;
import com.kavsdkexample.secure_connectivity.presenter.SecureConnectionPresenter;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * This is a sample activity for showing the DNS checking functionality.
 * The activity contains a list of trusted IPs (initially empty) and allows
 * the user to add new IPs into it and check whether an entered URL is in the list.
 */
public class DnsCheckerActivity extends    BaseActivity<BaseView, BaseViewState, SecureConnectionPresenter>
                                implements View.OnClickListener,
                                           OnCheckedChangeListener {

    private static final int CONTEXT_MENU_DELETE_COMMAND_ID = 0;
    @Inject SecureConnectionPresenter mPresenter;

    /* GUI controls */
    private Button mCheckButton;
    private Button mAddIpButton;
    private CheckBox mGetExtendedVerdict;
    private ListView mIpsListView;
    private EditText mUrlToCheckText;
    private EditText mAddIpText;
    private TextView mResultText;

    /* A list of IPs and its adapter */
    private ArrayAdapter<String> mListAdapter;
    private final List<String> mIpList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.dns_checker_activity);
        // Finding GUI controls and initializing them
        mCheckButton        = findViewById(R.id.checkButton);
        mAddIpButton        = findViewById(R.id.addIpButton);
        mGetExtendedVerdict = findViewById(R.id.getExtendedVerdict);
        mIpsListView        = findViewById(R.id.listView1);
        mUrlToCheckText     = findViewById(R.id.urlToCheckText);
        mAddIpText          = findViewById(R.id.newIpEditText);
        mResultText         = findViewById(R.id.resultText);

        mListAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mIpList);

        mIpsListView.setEmptyView(findViewById(R.id.emptyListView));
        mIpsListView.setAdapter(mListAdapter);
        registerForContextMenu(mIpsListView);
    }

    @Override
    public void onSdkInited() {
        mCheckButton.setOnClickListener(this);
        mAddIpButton.setOnClickListener(this);
        mGetExtendedVerdict.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.checkButton) {
            /* DNS checking procedure can be rather time consuming.
             * Therefore, the task has to be processed in a separate thread.
             * We also disable the check button during the execution
             */
            mCheckButton.setEnabled(false);

            try {
                CheckDnsTask task = new CheckDnsTask(mUrlToCheckText.getText().toString(), mIpList, this);
                task.execute(mGetExtendedVerdict.isChecked());

            } catch (SdkLicenseViolationException e) {
                showDnsCheckResult(getString(R.string.str_dns_error_license_absent_or_expired));
            }

        } else if (v.getId() == R.id.addIpButton) {
            String ip = mAddIpText.getText().toString();
            if (NetUtils.isValidIP(ip)) {
                // Adding a new IP address into the list and clear the edit text
                mListAdapter.add(ip);
                mAddIpText.setText("");
            } else {
                Toast.makeText(this, R.string.str_msg_bad_ip_address, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Show only one context menu item for removing IPs from the list
        menu.add(0, CONTEXT_MENU_DELETE_COMMAND_ID, 0, R.string.str_dns_checker_remove_ip);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CONTEXT_MENU_DELETE_COMMAND_ID) {
            // Identifying which item has to be removed and deleting it
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();
            mListAdapter.remove(mListAdapter.getItem(acmi.position));
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == mGetExtendedVerdict.getId()) {
            mIpsListView.setEnabled(!isChecked);
            mAddIpButton.setEnabled(!isChecked);
            mAddIpText.setEnabled(!isChecked);
        }
    }

    private void showDnsCheckResult(String result) {

        mResultText.setText(result);
        // After the process execution, the button becomes enabled again
        mCheckButton.setEnabled(true);
    }

    @Override
    protected SecureConnectionPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    /**
     * A class for processing DNS checking in background
     */
    private static class CheckDnsTask extends AsyncTask<Boolean, Void, String> {
        /* A URL to check */
        private final String mUrl;
        /* Items from the listView */
        private final Collection<String> mTrustedIps;
        /* A weak reference to the activity to not force it to be alive */
        private final WeakReference<DnsCheckerActivity> mActivityRef;
        private final DnsChecker mDnsChecker;

        CheckDnsTask(String url, Collection<String> trustedIps, DnsCheckerActivity activity)
                throws SdkLicenseViolationException {
            mUrl = url;
            mTrustedIps = trustedIps;
            mActivityRef = new WeakReference<>(activity);
            mDnsChecker = new DnsChecker();
        }

        @Override
        protected String doInBackground(Boolean... params) {

            Context context = mActivityRef.get();
            if (context == null) {
                cancel(true);
                return null;
            }

            boolean getExtendedVerdict = params[0];

            StringBuilder resultText = new StringBuilder();
            resultText.append(context.getString(R.string.str_dns_check_activity_result_label));

            try {
                if (getExtendedVerdict) {
                    DnsCheckResult result = mDnsChecker.checkURL(mUrl);

                    resultText.append(result.getVerdict().toString()).append('\n');

                    if (!result.getTrustedIpList().isEmpty()) {
                        resultText.append(context.getString(R.string.str_dns_check_trusted_list_caption));
                        for (String ip : result.getTrustedIpList()) {
                            resultText.append(ip).append('\n');
                        }

                    } else {
                        resultText.append(context.getString(R.string.str_dns_check_trusted_list_empty));
                    }

                } else {
                    if (mDnsChecker.checkURL(mUrl, mTrustedIps)) {
                        resultText.append(DnsCheckVerdict.Trusted.toString());
                    } else {
                        resultText.append(DnsCheckVerdict.Untrusted.toString());
                    }
                    resultText.append('\n');
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return context.getString(R.string.str_dns_checker_wrong_url_format);
            } catch (UnknownHostException e) {
                return context.getString(R.string.str_dns_checker_error);
            }

            return resultText.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            DnsCheckerActivity activity = mActivityRef.get();
            if (activity != null) {
                activity.showDnsCheckResult(result);
            }
        }
    }
}
