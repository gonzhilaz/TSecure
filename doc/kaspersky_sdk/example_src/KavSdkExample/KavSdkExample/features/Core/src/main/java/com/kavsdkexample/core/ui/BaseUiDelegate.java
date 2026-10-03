/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kavsdkexample.core.R;
import com.kavsdkexample.core.app.utils.ExceptionUtils;

import java.util.Locale;

public class BaseUiDelegate {
    private ViewGroup mRootView;

    public void setRootView(@NonNull View view) {
        if (view instanceof ViewGroup) {
            mRootView = (ViewGroup) view;
        }
    }

    public void showInitFailed(@NonNull Context context, @NonNull String msg, @NonNull Exception e) {
        if (mRootView != null) {
            mRootView.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //noinspection ConstantConditions
            View errorView = inflater.inflate(R.layout.feature_init_failed, mRootView);
            TextView errorTextView = errorView.findViewById(R.id.error);
            errorTextView.setText(String.format(Locale.getDefault(), msg, ExceptionUtils.stackTraceToString(e)));
        }
    }

}
