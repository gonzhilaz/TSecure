/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.model;

import com.kavsdkexample.antivirus.base.model.AntivirusModel;

public interface RootCheckModel extends AntivirusModel {
    void    checkRoot();
    boolean isCheckCompleted();
    boolean isRooted();
}
