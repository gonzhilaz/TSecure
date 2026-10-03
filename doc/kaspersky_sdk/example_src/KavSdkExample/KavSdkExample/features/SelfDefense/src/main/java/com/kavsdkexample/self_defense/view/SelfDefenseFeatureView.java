/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.view;

import com.kavsdkexample.core.app.view.BaseView;

public interface SelfDefenseFeatureView extends BaseView {
    void setAutorestartState(boolean enabled);
    void setForegroundServiceState(boolean enabled);
    void setNotificationAccessState(boolean enabled);
}