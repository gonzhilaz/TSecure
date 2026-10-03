/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.permissions;

import com.kavsdkexample.core.app.view.BaseView;

public interface InsufficientPermissionsView extends BaseView {
    void showInsufficientPermissions(String permissions);
}
