/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.settings;

public interface Settings {
    void saveWebFilterState(boolean value);
    boolean getWebFilterState();
    void saveWifiProxyState(boolean value);
    boolean getWifiProxyState();
    void saveProxyPort(int port);
    int getProxyPort();
    void saveExtCategoriesState(boolean isEnabled);
    boolean getExtCategoriesState();
    void saveIgnorePowerSaveModeState(boolean isEnabled);
    boolean getIgnorePowerSaveModeState();
}