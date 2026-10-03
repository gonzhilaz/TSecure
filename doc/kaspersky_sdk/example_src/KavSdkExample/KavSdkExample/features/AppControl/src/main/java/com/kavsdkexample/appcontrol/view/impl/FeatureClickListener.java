/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view.impl;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.kavsdkexample.appcontrol.R;

public class FeatureClickListener implements View.OnClickListener {
    private final FragmentActivity mActivity;

    public FeatureClickListener(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.app_control_button) {
            mActivity.startActivity(new Intent(mActivity, AppControlActivity.class));
        }
    }
}
