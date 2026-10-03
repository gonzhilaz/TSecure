/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.sdk;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.view.BaseView;

import java.util.List;

public interface MainTabFragmentView extends BaseView {
    void showFeatures(@NonNull List<SdkFeature> features);
}
