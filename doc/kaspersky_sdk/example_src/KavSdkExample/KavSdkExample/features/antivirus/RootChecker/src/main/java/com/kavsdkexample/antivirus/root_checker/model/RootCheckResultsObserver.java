/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.model;

import androidx.annotation.NonNull;

public interface RootCheckResultsObserver {
    void onSuccess(boolean isRooted);
    void onFailed(@NonNull Exception e);
    void onBasesUnavailable();
}
