/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model;

public interface MonitorBaseModelObserver {
    void onMonitorStateChanged(boolean enabled);
}
