/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface SelfDefenseModelAppSignCheckObserver {
    void onApplicationCheckResult(@NonNull String result);
}
