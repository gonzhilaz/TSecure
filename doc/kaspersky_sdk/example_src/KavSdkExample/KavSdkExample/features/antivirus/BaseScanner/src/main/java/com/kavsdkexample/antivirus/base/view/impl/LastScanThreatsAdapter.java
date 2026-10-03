/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapper;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.scanner.R;

class LastScanThreatsAdapter extends CursorAdapter {
    private final Context mContext;
    private final ThreatInfoProvider mThreatInfoProvider;
    private final ThreatInfoWrapperFactory mThreatInfoWrapperFactory;

    LastScanThreatsAdapter(@NonNull Context                  context,
                           @NonNull Cursor cursor,
                           @NonNull ThreatInfoProvider       threatInfoProvider,
                           @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory) {
        super(context, cursor, 0);
        mContext                  = context;
        mThreatInfoProvider       = threatInfoProvider;
        mThreatInfoWrapperFactory = threatInfoWrapperFactory;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.last_scan_threats_fragment_threat_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvThreatName    = view.findViewById(R.id.threat_name);
        TextView tvThreatObjPath = view.findViewById(R.id.threat_object_path);
        TextView tvPkgName       = view.findViewById(R.id.threat_pkg_name);

        ThreatInfoWrapper threatInfo = mThreatInfoProvider.getStoredThreat(cursor, mThreatInfoWrapperFactory);
        tvThreatName.setText(threatInfo.getThreatName());
        tvThreatObjPath.setText(threatInfo.getObjectPath());
        String pkgName = threatInfo.getPackageName();
        tvPkgName.setText(TextUtils.isEmpty(pkgName) ?
                mContext.getString(R.string.str_last_scan_activity_threats_item_file) :
                mContext.getString(R.string.str_last_scan_activity_threats_item_pkg_name, pkgName));
    }

    void closeCursor() {
        getCursor().close();
    }
}
