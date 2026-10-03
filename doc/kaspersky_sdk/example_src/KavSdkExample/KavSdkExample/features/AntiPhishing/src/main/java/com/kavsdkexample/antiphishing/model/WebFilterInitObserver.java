/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface WebFilterInitObserver {
    void onWebFilterInitSuccess();
    void onWebFilterInitFailed(@NonNull Exception e);

}
