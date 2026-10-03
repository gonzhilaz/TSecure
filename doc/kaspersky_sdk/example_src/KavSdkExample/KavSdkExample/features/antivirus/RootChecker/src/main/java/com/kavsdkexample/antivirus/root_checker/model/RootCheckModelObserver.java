/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.model;

import androidx.annotation.NonNull;

public interface RootCheckModelObserver {
    void onRootCheckResults(boolean isRooted);
    void onError(@NonNull String message);
    void onBasesUnavailable();
}
