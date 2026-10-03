/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.model;

import com.kavsdkexample.antivirus.base.model.AntivirusModel;

public interface SelfCheckModel extends AntivirusModel {
    void    checkSelf();
    boolean isCheckCompleted();
    boolean isCompromised();
}
