/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.scanner.R;

final class LastScanThreatsErrorAdapter extends BaseAdapter {
    private final Context mContext;

    LastScanThreatsErrorAdapter(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int i) {
        return mContext.getString(R.string.str_last_scan_activity_threats_error_message);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(mContext);
        textView.setText(R.string.str_last_scan_activity_threats_error_message);
        return textView;
    }
}
