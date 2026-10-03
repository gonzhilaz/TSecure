/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.anti_virus.impl;

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

public class AntiVirusFragment extends BaseMainTabFragment {
    private static final String CONTENTS_FRAGMENT_ID = "contents";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        processBackPressed();
        View view = initView(inflater, container, TabDescription.TabId.AntivirusProtection, R.layout.av_protection_fragment);
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.av_features_container, new AntiVirusContentsFragment(), CONTENTS_FRAGMENT_ID)
                    .commit();
        }
        return view;
    }

    @Override
    public void showFeatures(@NonNull List<SdkFeature> features) {
    }
}
