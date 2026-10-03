/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseView;

public interface CheckSignatureView extends BaseView {
    void showApplicationCheckResult(@NonNull String result);
}
