/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.presenter;

import androidx.annotation.UiThread;

import com.kavsdkexample.appcontrol.view.AppControlView;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

import java.util.List;

@UiThread
public interface AppControlPresenter extends BasePresenter<AppControlView, BaseViewState> {
    void enableAppControl(boolean isChecked);
    void useWindowManagerForBlocking(boolean isUsed);
    void setBothListsMode();
    void setBlockListMode();
    void setAllowListMode();
    void saveChanges();
    void openSettings();
    List<String> getCategories();
    void addItemToBlockList(final String packageName, final String category);
    void addItemToAllowList(final String packageName, final String category);
    int getBlockListItemsCount();
    int getAllowListItemsCount();
    String getBlockListPackage(int index);
    String getAllowListPackage(int index);
    String getBlockListCategory(int index);
    String getAllowListCategory(int index);
    boolean isAppcontrolEnabled();
    boolean isWindowManagerForBlockingUsed();
    void requestRemovalFromBlockList(int index);
    void requestRemovalFromAllowList(int index);

    interface RemovalConfirmationListener {
        void onRemoveConfirmed();
    }
}
