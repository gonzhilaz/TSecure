/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk.anti_virus.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kavsdkexample.R;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.SdkFeatureAndroid;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.view.utils.UiUtils;
import com.kavsdkexample.view.sdk.impl.BaseMainTabFragment;

import java.util.List;

public class AntiVirusContentsFragment extends BaseMainTabFragment {
    private boolean mOasTitleInjected;
    private boolean mPuaProtectionTitleInjected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView(inflater, container, TabDescription.TabId.AntivirusProtection, R.layout.av_protection_contents_fragment);
    }

    @Override
    public void showFeatures(@NonNull List<SdkFeature> features) {
        backToFalseFeatureInjectedFlags();
        showFeatures(features, R.id.av_protection_layout);
    }

    @Override
    protected void showFeatures(@NonNull List<SdkFeature> features, int layoutId) {
        Fragment parent = getParentFragment();
        if (parent == null) {
            throw new IllegalStateException("Expected not null parent fragment");
        }
        ViewGroup layoutForInject = mRootViewGroup.findViewById(layoutId);
        for (SdkFeature feature : features) {
            if (feature instanceof SdkFeatureAndroid) {
                tryOasTittleInject(feature, layoutForInject);
                tryPuaProtectionTittleInject(feature, layoutForInject);
                ((SdkFeatureAndroid) feature).initUiView(parent, layoutForInject);
            }
        }
    }

    private void tryOasTittleInject(SdkFeature feature, ViewGroup layoutForInject) {
        if (!mOasTitleInjected &&
                (SdkFeature.ANTIVIRUS_APP_MONITOR.equals(feature.getName()) ||
                 SdkFeature.ANTIVIRUS_FOLDER_MONITOR.equals(feature.getName()) ||
                 SdkFeature.ANTIVIRUS_RTP_MONITOR.equals(feature.getName()))) {
            UiUtils.injectUi(requireActivity(), layoutForInject, R.layout.av_protection_fragment_oas_title);
            mOasTitleInjected = true;
        }
    }

    private void tryPuaProtectionTittleInject(SdkFeature feature, ViewGroup layoutForInject) {
        if (!mPuaProtectionTitleInjected &&
                (SdkFeature.ANTIVIRUS_PUA_SCANNER.equals(feature.getName()) ||
                 SdkFeature.ANTIVIRUS_PUA_MONITOR.equals(feature.getName()))) {
            UiUtils.injectUi(requireActivity(), layoutForInject, R.layout.av_protection_pua_protection_title);
            mPuaProtectionTitleInjected = true;
        }
    }

    private void backToFalseFeatureInjectedFlags() {
        mOasTitleInjected = false;
        mPuaProtectionTitleInjected = false;
    }
}
