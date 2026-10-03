/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface EulaModelStatusObserver {
    void onEulaLoaded(@NonNull String eulaText);
}
