/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface EulaLoadObserver {
    void onEulaLoaded(@NonNull String eulaText);
}
