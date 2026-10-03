/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.model.BaseModel;
import com.kavsdkexample.model.app.SdkInitializer;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;

import java.util.Collection;
import java.util.List;

public interface AppModel extends BaseModel, SdkInitializer {
    void                   checkAcceptEula();
    void                   checkPermissions();
    boolean                areAllPermissionsGranted();
    boolean                areAllPermissionsChecked();
    void                   setAllPermissionsUnchecked();
    void                   ignoreInsufficientPermissions();
    @NonNull
    String                 getInsufficientPermissions();
    void                   activateLicense(@NonNull String code);
    @NonNull
    SdkLicenseInfo         getLicenseInfo();
    @NonNull
    List<TabDescription>   getTabs();
    @NonNull
    List<SdkFeature>       getFeaturesForTab(@NonNull TabDescription.TabId id);
    @NonNull
    Collection<SdkFeature> getAllFeatures();
    void                   setProxyAuthCredentials(@NonNull String login, @NonNull String password);
    void                   cancelProxyAuth();

    void onDefaultDialerStatusRequested();
    boolean isDefaultDialerStatusRequested();

    void onNotificationAccessPermissionRequested();
    boolean isNotificationAccessPermissionRequested();
}
