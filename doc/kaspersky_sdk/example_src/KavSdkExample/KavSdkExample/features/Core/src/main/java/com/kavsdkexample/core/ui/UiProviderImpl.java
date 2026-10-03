/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.Toast;

public class UiProviderImpl implements UiProvider {
    private final Context mContext;

    public UiProviderImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public void showToast(@NonNull String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }
}
