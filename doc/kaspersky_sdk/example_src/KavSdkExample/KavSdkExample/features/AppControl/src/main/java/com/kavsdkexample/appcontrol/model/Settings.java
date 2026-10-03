/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model;

import androidx.annotation.UiThread;

@UiThread
public interface Settings {
    void saveAppcontrolButtonStatus(boolean status);
    void saveWindowManagerForBlockingButton(boolean status);
    boolean getAppcontrolButtonStatus();
    boolean getWindowManagerForBlockingButton();
}