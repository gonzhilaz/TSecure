/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.secureconnection.ConnectionMode;
import com.kavsdk.secureconnection.SecureConnectionException;
import com.kavsdk.secureconnection.SecureHttpClient;
import com.kavsdk.secureconnection.SecureURL;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.secure_connectivity.R;
import com.kavsdkexample.secure_connectivity.presenter.SecureConnectionPresenter;
import com.kavsdkexample.secure_connectivity.repository.settings.Settings;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import dagger.android.AndroidInjection;

/**
 * This is a sample activity to demonstrate the functionality of Secure Connection SDK component.
 * It performs a request to a URL entered by the user (via secure channel provided
 * by the component) and shows the result.
 * In case of errors, such as wrong certificate or DNS checking fail, an error message will be shown.
 * Otherwise, the webpage will be downloaded and its content or a message with response code will be displayed.
 */
@SuppressWarnings("deprecation")
public class SecureConnectionActivity extends BaseActivity {
    /*
     * We use a secure HTTP client and secure URL implementations
     * to provide a secure connection
     */
    private static final int SECURE_URL_METHOD_INDEX            = 0;
    private static final int SECURE_HTTP_CLIENT_METHOD_INDEX    = 1;

    /* A list of sample URLs */
    private static final String[] AUTO_COMPLETE_URLS = new String[]{
        "https://click.alfabank.ru/",                   // Multiple redirects
        "https://testjmb.alfabank.ru",
        "https://yandex.ru/",
        "https://www.google.com",
        "http://mail.ru/",                              // Unsupported protocol
        "https://login.11st.co.kr",                     // Fake bank system
        "https://testjmb.alfabank.ru/ALFAIBSR_FT4/"     // Bad URL reputation
    };

    static final int SET_TRUSTED_IP_REQUEST_CODE = 32768;
    @Inject SecureConnectionPresenter mPresenter;
    @Inject Settings                  mSettings;

    private ArrayList<String> mTrustedUrls;

    /*
     * GUI controls
     */
    private Spinner mSpinSecureTool;
    private AutoCompleteTextView mAutoCompleteUrl;
    private CheckBox mOnlyInTrustedListCheckBox;
    private CheckBox mStrictModeCheckBox;
    private CheckBox mUseOnlyFirstIpCheckBox;
    private TextView mTextViewResult;
    private EditText mUrlCheckTimeout;
    private EditText mCertificateCheckTimeout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.secure_connection_activity);
    }

    @Override
    public void onSdkInited() {

        // GUI initialization
        ArrayAdapter<String> aaMethods;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            aaMethods = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{
                    SecureURL.class.getSimpleName(),
                    getString(R.string.str_secureconnection_deprecated_http_client)});
        } else {
            aaMethods = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, new String[]{
                    SecureURL.class.getSimpleName(),
                    SecureHttpClient.class.getSimpleName()});
        }
        aaMethods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinSecureTool = findViewById(R.id.spinSecureTool);
        mSpinSecureTool.setAdapter(aaMethods);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mSpinSecureTool.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    if (position == SECURE_HTTP_CLIENT_METHOD_INDEX) {
                        DeprecatedFeatureDialogFragment.show(getSupportFragmentManager());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

        ArrayAdapter<String> aaAutoComplete = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, AUTO_COMPLETE_URLS);
        mAutoCompleteUrl = findViewById(R.id.acUrl);
        mAutoCompleteUrl.setAdapter(aaAutoComplete);

        findViewById(R.id.btnGo).setOnClickListener(v -> checkUrlAndShowResult());

        findViewById(R.id.btnList).setOnClickListener(v -> showTrustedIpList());

        mTrustedUrls = SecureConnectionTrustIpListActivity.loadTrustedUrls();

        mOnlyInTrustedListCheckBox = findViewById(R.id.checkOnlyInTrustedList);
        mOnlyInTrustedListCheckBox.setEnabled(mTrustedUrls.size() > 0);
        mOnlyInTrustedListCheckBox.setChecked(mSettings.isLoadOnlyTrustedUrlsEnabled());

        mStrictModeCheckBox = findViewById(R.id.strictMode);
        mUseOnlyFirstIpCheckBox = findViewById(R.id.useOnlyFirstIp);

        mTextViewResult = findViewById(R.id.tvResult);
        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());

        mUrlCheckTimeout = findViewById(R.id.urlCheckTimeout);
        mCertificateCheckTimeout = findViewById(R.id.certificateCheckTimeout);
    }


    protected void showTrustedIpList() {
        Intent i = SecureConnectionTrustIpListActivity.getIntent(this, mTrustedUrls);
        startActivityForResult(i, SET_TRUSTED_IP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        if (SET_TRUSTED_IP_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode && extras != null) {
            mTrustedUrls = extras.getStringArrayList(
                    SecureConnectionTrustIpListActivity.EXTRA_TRUSTED_URLS);

            boolean loadOnlyTrustedUrlsEnabled = (mTrustedUrls != null && mTrustedUrls.size() > 0);
            mOnlyInTrustedListCheckBox.setChecked(loadOnlyTrustedUrlsEnabled);
            mOnlyInTrustedListCheckBox.setEnabled(loadOnlyTrustedUrlsEnabled);
            mSettings.setLoadOnlyTrustedUrlsEnabled(loadOnlyTrustedUrlsEnabled);
        }
    }

    public void showResult(String message) {
        mTextViewResult.setText(message);
    }

    protected void checkUrlAndShowResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            mSpinSecureTool.getSelectedItemPosition() == SECURE_HTTP_CLIENT_METHOD_INDEX) {
            DeprecatedFeatureDialogFragment.show(getSupportFragmentManager());
            return;
        }

        final String strURL = mAutoCompleteUrl.getText().toString();
        if (!TextUtils.isEmpty(strURL)) {

            AsyncExecHTTP.TaskParams params = new AsyncExecHTTP.TaskParams();
            params.mUrl = strURL;
            params.mTrustedUrls = mOnlyInTrustedListCheckBox.isChecked() ? mTrustedUrls : null;
            params.mStrictMode = mStrictModeCheckBox.isChecked();
            params.mConnectionMode =
                    mUseOnlyFirstIpCheckBox.isChecked() ? ConnectionMode.FirstIpOnly : ConnectionMode.AllIps;

            final String certificateCheckTimeout = mCertificateCheckTimeout.getText().toString();
            if (!TextUtils.isEmpty(certificateCheckTimeout)) {
                try {
                    params.mCertificateCheckTimeout = Integer.parseInt(certificateCheckTimeout);
                } catch(NumberFormatException e) {
                    showResult(getString(R.string.str_secureconnection_too_big_timeout));
                    return;
                }
            }
            final String urlCheckTimeout = mUrlCheckTimeout.getText().toString();
            if (!TextUtils.isEmpty(urlCheckTimeout)) {
                try {
                    params.mUrlCheckTimeout = Integer.parseInt(urlCheckTimeout);
                } catch(NumberFormatException e) {
                    showResult(getString(R.string.str_secureconnection_too_big_timeout));
                    return;
                }
            }

            new AsyncExecHTTP(this, mSpinSecureTool.getSelectedItemPosition(), params).execute();

        } else {
            showResult(getString(R.string.str_secureconnection_url_not_specified));
        }
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected BaseViewState getViewState() {
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        mSettings.setLoadOnlyTrustedUrlsEnabled(mOnlyInTrustedListCheckBox.isChecked());
    }

    /**
     * A class for performing connection in background
     */
    @SuppressWarnings("deprecation")
    private static class AsyncExecHTTP extends AsyncTask<Void, Void, String> {

        private final WeakReference<ProgressDialog> mDlg;
        private final int mIndexMethod;
        private final TaskParams mParams;

        AsyncExecHTTP(Activity activity, int indexMethod, TaskParams params) {
            ProgressDialog dlg = new ProgressDialog(activity);
            dlg.setOwnerActivity(activity);
            dlg.setMessage(activity.getString(R.string.str_secureconnection_please_wait));
            dlg.setIndeterminate(true);
            dlg.setCancelable(false);
            mDlg = new WeakReference<>(dlg);

            mIndexMethod = indexMethod;
            mParams = params;
        }

        @Override
        protected void onPreExecute() {
            // Showing the progress dialog before the execution
            super.onPreExecute();
            ProgressDialog dlg = mDlg.get();
            if (dlg != null) {
                dlg.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            try {
                switch (mIndexMethod) {
                    case SECURE_HTTP_CLIENT_METHOD_INDEX:
                        SecureHttpClient httpClient = SecureHttpClient.newInstance();
                        try {
                            if (mParams.mTrustedUrls != null) {
                                httpClient.setTrustedIpv4Addresses(mParams.mTrustedUrls);
                            }
                            if (mParams.mStrictMode) {
                                httpClient.setStrictModeEnabled(true);
                            }
                            httpClient.setConnectionMode(mParams.mConnectionMode);

                            if (mParams.mUrlCheckTimeout != -1) {
                                httpClient.setUrlCheckTimeout(mParams.mUrlCheckTimeout);
                            }
                            if (mParams.mCertificateCheckTimeout != -1) {
                                httpClient.setCertificateCheckTimeout(mParams.mCertificateCheckTimeout);
                            }

                            result = getUrlDataHttpGet(httpClient, mParams.mUrl);
                        } finally {
                            httpClient.close();
                        }
                        break;

                    case SECURE_URL_METHOD_INDEX:
                        SecureURL secureURL = new SecureURL(mDlg.get().getOwnerActivity(), mParams.mUrl);
                        if (mParams.mTrustedUrls != null) {
                            secureURL.setTrustedIpv4Addresses(mParams.mTrustedUrls);
                        }
                        if (mParams.mStrictMode) {
                            secureURL.setStrictModeEnabled(true);
                        }
                        if (mParams.mUrlCheckTimeout != -1) {
                            secureURL.setUrlCheckTimeout(mParams.mUrlCheckTimeout);
                        }
                        if (mParams.mCertificateCheckTimeout != -1) {
                            secureURL.setCertificateCheckTimeout(mParams.mCertificateCheckTimeout);
                        }
                        secureURL.setConnectionMode(mParams.mConnectionMode);
                        result = getUrlData(secureURL);
                        break;
                    default:
                        break;
                }

            } catch (SdkLicenseViolationException e) {
                result = formatError(e);
            } catch (ClientProtocolException e) {
                result = formatError(e);
            } catch (URISyntaxException e) {
                result = formatError(e);
            } catch (SecureConnectionException e) {
                result = formatError(e);
            } catch (IOException e) {
                result = formatError(e);
            } catch (IllegalArgumentException e) {
                result = formatError(e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ProgressDialog dlg = mDlg.get();
            if (dlg != null) {
                SecureConnectionActivity activity = (SecureConnectionActivity) dlg
                        .getOwnerActivity();
                // Checking that the activity is still alive
                // Perhaps there is no need to show the result
                if (activity != null && !activity.isFinishing()) {
                    activity.showResult(result);
                }

                if (dlg.isShowing()) {
                    dlg.dismiss();
                }
            }
        }

        private String formatError(Throwable tr) {
            tr.printStackTrace();
            return String.format("%s\n%s", tr.getClass().getSimpleName(), tr.getMessage());
        }

        /**
         * This method retrieves a webpage content using Secure HTTP Client
         */
        private String getUrlDataHttpGet(SecureHttpClient httpClient, String url)
                throws URISyntaxException, IOException {

            StringBuilder sb = new StringBuilder();

            HttpGet request = new HttpGet();
            request.setURI(new URI(url));

            HttpResponse response = httpClient.execute(request);
            // SecureHttpClient does not check response codes
            // Process the codes here
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK /* 200 */) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()))) {
                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line).append(nl);
                    }
                }
            } else {
                Activity act = mDlg.get().getOwnerActivity();
                sb = new StringBuilder(String.format(act.getString(R.string.str_secureconnection_response_status_code),
                        statusCode));
            }
            return sb.toString();
        }

        /**
         * This method retrieves a webpage content using Secure URL
         */
        private String getUrlData(SecureURL secureUrl) throws IOException {

            StringBuilder sb = new StringBuilder();
            HttpsURLConnection connection = secureUrl.openConnection();
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.defaultCharset()))) {
                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line).append(nl);
                    }
                }
            } else {
                Activity act = mDlg.get().getOwnerActivity();
                sb.append(String.format(act.getString(R.string.str_secureconnection_response_status_code),
                        connection.getResponseCode()));
            }

            return sb.toString();
        }

        private static class TaskParams {
            String          mUrl;
            List<String>    mTrustedUrls;
            boolean         mStrictMode;
            ConnectionMode  mConnectionMode;
            int             mUrlCheckTimeout = -1;
            int             mCertificateCheckTimeout = -1;
        }

    }
}
