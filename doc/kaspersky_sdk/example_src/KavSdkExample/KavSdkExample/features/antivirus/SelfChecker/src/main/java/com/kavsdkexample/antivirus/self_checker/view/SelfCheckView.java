/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.view.AntivirusBaseView;
import com.kavsdkexample.core.app.view.BaseView;

public interface SelfCheckView extends BaseView, AntivirusBaseView {
    void showSelfCheckResult(boolean isCompromised);
    void showSelfCheckFailed(@NonNull String message);
}
