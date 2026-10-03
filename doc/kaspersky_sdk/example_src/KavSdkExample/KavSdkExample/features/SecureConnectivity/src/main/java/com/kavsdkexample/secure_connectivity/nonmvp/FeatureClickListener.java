/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.nonmvp;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.kavsdkexample.secure_connectivity.R;

public class FeatureClickListener implements View.OnClickListener {
    private final FragmentActivity mActivity;

    public FeatureClickListener(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.dns_checker_button) {
            mActivity.startActivity(new Intent(mActivity, DnsCheckerActivity.class));
        } else if (id == R.id.test_secure_connection_button) {
            mActivity.startActivity(new Intent(mActivity, SecureConnectionActivity.class));
        } else if (id == R.id.cert_validator_button) {
            CheckCertificateDialog.show(mActivity.getSupportFragmentManager());
        }
    }
}
