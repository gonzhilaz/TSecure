/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.model.BaseModel;
import com.kavsdkexample.self_defense.model.sdk.NotificationStateObserver;

@UiThread
public interface SelfDefenseModel extends BaseModel {
    void    setSdkAutoRestartEnabled(boolean value);
    boolean isSdkAutorestartEnabled();

    void    enableForegroundService();
    void    disableForegroundService();
    boolean isForegroundServiceEnabled();

    boolean isNotificationAccessEnabled();

    void    setNotificationStateObserver(@Nullable NotificationStateObserver observer);

    void    switchToCheckSignatureView();
    void    switchToSelfDefenseFeatureView();

    void    checkApplicationSignature();
}
