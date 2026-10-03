/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;

public interface ManageableSelfDefenseModel extends SelfDefenseModel {
    void setInteractors(@NonNull ServiceInteractor      serviceInteractor,
                        @NonNull NotificationInteractor notificationInteractor);
}
