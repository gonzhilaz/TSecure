/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view.impl;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.kavsdkexample.antivirus.rtp_monitor.R;

public class FeatureClickListener implements View.OnClickListener {
    private final Fragment mFragment;

    public FeatureClickListener(@NonNull Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.rtp_monitor_button) {
            View containerView = mFragment.getView();
            if (containerView == null) {
                throw new IllegalStateException("View group for parent fragment is not provided");
            }

            mFragment
                    .getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(containerView.getId(), new RtpMonitorFragment()).commit();
        }
    }
}
