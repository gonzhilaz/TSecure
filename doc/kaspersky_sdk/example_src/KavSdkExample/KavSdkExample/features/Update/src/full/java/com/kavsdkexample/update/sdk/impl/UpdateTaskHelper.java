/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.sdk.impl;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.updater.UpdateEventListener;
import com.kavsdk.updater.Updater;

import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;

public final class UpdateTaskHelper {

    public static boolean update(Updater updater, UpdateModelComponentMode componentMode, UpdateModelUpdateServerMode serverMode, String url, UpdateEventListener listener) throws SdkLicenseViolationException {
        if (componentMode == UpdateModelComponentMode.FinancialCategorizer) {
            if (serverMode == UpdateModelUpdateServerMode.Random) {
                updater.updateFinancialBases(listener);
            } else {
                updater.updateFinancialBases(url, listener);
            }
        } else if (componentMode == UpdateModelComponentMode.Antivirus) {
            if (serverMode == UpdateModelUpdateServerMode.Random) {
                updater.updateAntivirusBases(listener);
            } else {
                updater.updateAntivirusBases(url, listener);
            }
        } else if (componentMode == UpdateModelComponentMode.All) {
            if (serverMode == UpdateModelUpdateServerMode.Random) {
                updater.updateAllBases(listener);
            } else {
                updater.updateAllBases(url, listener);
            }
        } else {
            return false;
        }
        return true;
    }

}