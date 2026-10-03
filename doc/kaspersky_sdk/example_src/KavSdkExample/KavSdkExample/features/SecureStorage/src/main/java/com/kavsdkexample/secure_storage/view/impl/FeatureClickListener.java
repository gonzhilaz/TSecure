/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.impl;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.View;

import com.kavsdkexample.secure_storage.R;

public class FeatureClickListener implements View.OnClickListener {
    private final Activity mActivity;

    public FeatureClickListener(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        // Can't use switch because R.id variables are not final for library project
        if (id == R.id.test_database_button) {
            mActivity.startActivity(new Intent(mActivity, TestDatabaseActivity.class));
        } else if (id == R.id.test_secure_file_button) {
            mActivity.startActivity(new Intent(mActivity, TestSecureFileActivity.class));
        } else {
            throw new IllegalStateException("Unexpected viewId: " + view.getId());
        }
    }
}
