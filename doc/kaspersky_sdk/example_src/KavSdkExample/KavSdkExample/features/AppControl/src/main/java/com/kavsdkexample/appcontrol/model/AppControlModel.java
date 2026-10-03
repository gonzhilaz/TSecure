/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.model.BaseModel;

@UiThread
public interface AppControlModel extends BaseModel {
    void enableAppControl(boolean isChecked);
    void useWindowManagerForBlocking(boolean isUsed);
    void setMode(AppControlMode mode);
    void saveChanges();
    void openSettings();
    void addItem(final AppControlMode mode, final String packageName, final String category);
    int getItemsCount(final AppControlMode mode);
    String getPackage(final AppControlMode mode, int index);
    String getCategory(final AppControlMode mode, int index);
    void deleteItem(final AppControlMode mode, int index);
    void showDialog();
    boolean isAppcontrolEnabled();
    boolean isWindowManagerForBlockingUsed();
    boolean isAdditionAllowed(@NonNull AppControlMode appControlMode, @Nullable String packageName);
    boolean isRemovalAllowed(@NonNull AppControlMode appControlMode, int index);
}
