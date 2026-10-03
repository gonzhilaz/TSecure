/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.view.impl;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;

import com.kavsdkexample.antiphishing.R;

public class FeatureClickListener implements View.OnClickListener {
    private final Activity mActivity;

    public FeatureClickListener(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.anti_phishing_button) {
            mActivity.startActivity(new Intent(mActivity, WebFilterActivity.class));
        }
    }
}
