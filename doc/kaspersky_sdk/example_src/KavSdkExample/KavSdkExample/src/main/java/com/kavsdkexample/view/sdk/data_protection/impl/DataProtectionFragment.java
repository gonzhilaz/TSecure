/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.data_protection.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kavsdkexample.R;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.view.sdk.impl.BaseMainTabFragment;

import java.util.List;

public class DataProtectionFragment extends BaseMainTabFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView(inflater, container, TabDescription.TabId.DataProtection, R.layout.data_protection_fragment);
    }

    @Override
    public void showFeatures(@NonNull List<SdkFeature> features) {
        showFeatures(features, R.id.data_protection_layout);
    }
}
