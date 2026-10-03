/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.model;

import androidx.annotation.NonNull;

public interface SelfCheckModelObserver {
    void onSelfCheckResults(boolean isCompromised);
    void onError(@NonNull String message);
    void onBasesUnavailable();
}
