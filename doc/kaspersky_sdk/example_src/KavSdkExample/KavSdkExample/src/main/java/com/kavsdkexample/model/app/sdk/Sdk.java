/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk;

import com.kavsdk.license.SdkLicense;

public interface Sdk {
    boolean isInitialized();
    SdkLicense getLicense();
    String getHashOfHardwareId();
    String getInstallationId();
    void setProxyAuthCredentials(String login, String password);
    void cancelProxyAuth();
}
