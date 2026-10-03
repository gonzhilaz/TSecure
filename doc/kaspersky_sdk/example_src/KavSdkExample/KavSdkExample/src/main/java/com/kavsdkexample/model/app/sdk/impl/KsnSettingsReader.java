/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.impl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.kavsdk.network.KasperskySecurityNetworkSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class KsnSettingsReader {
    private static final String TAG = KsnSettingsReader.class.getSimpleName();
    private static final String KSN_PROPERTIES_FILE_NAME     = "ksn.properties";
    private static final String PING_CONNECT_TIMEOUT_PROP    = "pingConnectTimeoutMs";
    private static final String PING_WAIT_TIMEOUT_PROP       = "pingWaitTimeoutMs";
    private static final String MAX_FAILED_PING_RETRY_PROP   = "maxFailedPingRetryPeriodSec";
    private static final String CONNECTION_WAIT_TIMEOUT_PROP = "connectionWaitTimeoutSec";
    private static final String SKIP_UDP_ROUTES_PROP         = "skipUdpRoutes";

    private KsnSettingsReader() {
    }

    public static KasperskySecurityNetworkSettings readSettings(final Context context) {
        try {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(KSN_PROPERTIES_FILE_NAME);
            try {
                InputStream input = fileDescriptor.createInputStream();
                try {
                    KasperskySecurityNetworkSettings settings = new KasperskySecurityNetworkSettings();

                    Properties prop = new Properties();
                    prop.load(input);

                    settings.pingConnectTimeoutMs        = Integer.parseInt(prop.getProperty(PING_CONNECT_TIMEOUT_PROP));
                    settings.pingWaitTimeoutMs           = Integer.parseInt(prop.getProperty(PING_WAIT_TIMEOUT_PROP));
                    settings.maxFailedPingRetryPeriodSec = Integer.parseInt(prop.getProperty(MAX_FAILED_PING_RETRY_PROP));
                    settings.connectionWaitTimeoutSec    = Integer.parseInt(prop.getProperty(CONNECTION_WAIT_TIMEOUT_PROP));
                    settings.skipUdpRoutes               = Boolean.parseBoolean(prop.getProperty(SKIP_UDP_ROUTES_PROP));

                    return settings;
                } finally {
                    input.close();
                }
            } finally {
                fileDescriptor.close();
            }
        } catch (IOException e) {
            Log.i(TAG, KSN_PROPERTIES_FILE_NAME + " failed to read ("+ e.getMessage() +"). Using default KSN settings");
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            Log.i(TAG, KSN_PROPERTIES_FILE_NAME + " corrupted. Using default KSN settings");
            return null;
        }
    }
}
