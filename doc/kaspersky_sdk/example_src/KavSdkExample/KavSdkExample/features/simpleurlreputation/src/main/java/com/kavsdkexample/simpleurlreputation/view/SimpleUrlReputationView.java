/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseView;

public interface SimpleUrlReputationView extends BaseView {
    void onResult(@NonNull String result);
}
