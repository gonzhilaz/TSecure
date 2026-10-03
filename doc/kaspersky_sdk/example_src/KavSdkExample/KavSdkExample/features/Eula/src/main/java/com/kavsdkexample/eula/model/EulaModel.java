/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model;

import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.model.BaseModel;

@UiThread
public interface EulaModel extends BaseModel {
    void loadEula();
    void acceptEula();
    void rejectEula();
}
