/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface TabDescription {

    /**
     * Get unique id of the tab
     * @return Unique id of this tab
     */
    @NonNull TabId   getTabId();

    /**
     * Get feature for FeatureProvided tabs
     * @return SdkFeature for feature provided tab and null for tab created by main app
     */
    @Nullable SdkFeature getFeature();

    enum TabId {
        ApAndFakeAppsProtection,
        Privacy,
        EasyScanner,
        SecurityScanner,
        AntivirusProtection,
        SecureConnectivity,
        DeviceReputation,
        DataProtection,
        OtherFeatures,
        SelfDefense,
        Updates,
        Statistics,
        Agreements,
        WhoCalls,
        SimpleUrlReputation,
        LicenseInfo,
    }
}
