/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.view.AntivirusBaseView;
import com.kavsdkexample.core.app.view.BaseView;

public interface RootCheckView extends BaseView, AntivirusBaseView {
    void showRootCheckResult(boolean isRooted);
    void showRootCheckFailed(@NonNull String message);
}
