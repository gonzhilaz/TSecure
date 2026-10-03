/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.kavsdkexample.antivirus.self_checker.view.impl.SelfCheckDialogFragment;

public class FeatureClickListener implements View.OnClickListener {
    private final FragmentActivity mActivity;

    FeatureClickListener(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.scan_self_button) {
            SelfCheckDialogFragment.show(mActivity.getSupportFragmentManager());
        }
    }
}
