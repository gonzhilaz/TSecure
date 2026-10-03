/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.view.impl;

import androidx.annotation.NonNull;
import com.kavsdkexample.update.R;
import com.kavsdkexample.update.model.UpdateModelComponentMode;

public final class UpdateFragmentHelper {

    public static int getComponentId(@NonNull UpdateModelComponentMode component) {
        if (component == UpdateModelComponentMode.All) {
            return R.id.updateComponentAll;
        } else if (component == UpdateModelComponentMode.Antivirus) {
            return R.id.updateComponentAntivirusProtection;
        } else {
            return R.id.updateComponentAll;
        }
    }

    public static UpdateModelComponentMode getModelComponentMode(int id) {
        if (id == R.id.updateComponentAll) {
            return UpdateModelComponentMode.All;
        } else if (id == R.id.updateComponentAntivirusProtection) {
            return UpdateModelComponentMode.Antivirus;
        } else {
            return UpdateModelComponentMode.All;
        }
    }

}
