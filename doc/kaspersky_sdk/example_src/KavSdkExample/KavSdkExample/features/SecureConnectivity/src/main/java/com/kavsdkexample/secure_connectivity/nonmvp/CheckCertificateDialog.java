/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;

import com.kavsdk.certificatechecker.CertificateCheckResult;
import com.kavsdk.certificatechecker.CertificateCheckService;
import com.kavsdkexample.core.app.view.impl.nonmvp.BaseSimpleDialog;
import com.kavsdkexample.secure_connectivity.R;

/**
 * This dialog checks whether a certificate of an entered website is valid
 * and shows the result in a toast.
 */
public class CheckCertificateDialog extends BaseSimpleDialog {
    private static final String CHECK_CERTIFICATE_DIALOG_TAG = "CheckCertificateDialog";
    private String mResultStr;
    private String mNullResultStr;
    private String mErrorStr;
    private String mCertificateUrl;

    public static void show(@NonNull FragmentManager manager) {
        CheckCertificateDialog fragment = new CheckCertificateDialog();
        fragment.show(manager, CHECK_CERTIFICATE_DIALOG_TAG);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mResultStr     = getString(R.string.str_check_certificate_dialog_result);
        mNullResultStr = getString(R.string.str_check_certificate_dialog_result_null);
        mErrorStr      = getString(R.string.str_check_certificate_dialog_result_error);

    }

    @Override
    protected void checkAndShowResult(View v) {
        mCertificateUrl = mTextView.getText().toString();
        super.checkAndShowResult(v);
    }

    /*
     * This method checks the URL entered into the edit box.
     * In order to verify a certificate, SDK asks a KSN server via a network.
     * Therefore, this method has to be invoked from a non-GUI thread
     */
    @Override
    protected String check() {
        try {
            final CertificateCheckService service = new CertificateCheckService();

            CertificateCheckResult result = service.checkCertificate(mCertificateUrl);

            if (result != null) {
                return mResultStr + result.toString();
            } else {
                return mNullResultStr;
            }

        } catch (Exception e) {
            return mErrorStr + e.getClass().getSimpleName() + " " + e.getMessage();
        }
    }

    @Override
    protected int getHintTextId() {
        return R.string.str_check_certificate_dialog_enter_url;
    }

    @Override
    protected ArrayAdapter<?> getAdapter() {
        return null;
    }

    @Override
    protected int getTitleId() {
        return R.string.str_secure_connectivity_check_certificate_url;
    }

    @Override
    protected int getTextId() {
        return R.string.str_check_certificate_default_url;
    }
}
