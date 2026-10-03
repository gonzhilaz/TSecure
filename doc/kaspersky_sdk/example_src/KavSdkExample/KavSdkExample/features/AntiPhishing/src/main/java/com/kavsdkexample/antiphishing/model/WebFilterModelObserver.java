/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.model;

import androidx.annotation.NonNull;

public interface WebFilterModelObserver {
    void onWebFilterInitSuccess();
    void onWebFilterInitFailed(@NonNull Exception e);
    void notificationOfWbFilterNotWorking();
    void notificationOfTaskReputationNotWorking();
    void changeProxyPort();
}
