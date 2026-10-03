/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view.impl;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.scanner.R;
import com.kavsdkexample.antivirus.base.view.ScannerBaseView;

public final class ScanUiUtils {
    private ScanUiUtils() {
    }

    public static int avActionToResId(@NonNull AvAction action) {
        switch (action) {
            case DeteteThreat:
                return R.id.deleteIfNotCured;
            case QuarantineThreat:
                return R.id.quarantineIfNotCured;
            case SkipThreat:
                return R.id.skipIfNotCured;
            default:
                throw new IllegalStateException("Unknown AvAction: " + action);
        }
    }

    public static AvAction resIdToAvAction(int id) {
        if (id == R.id.deleteIfNotCured) {
            return AvAction.DeteteThreat;
        } else if (id == R.id.quarantineIfNotCured) {
            return AvAction.QuarantineThreat;
        } else if (id == R.id.skipIfNotCured) {
            return AvAction.SkipThreat;
        } else {
            throw new IllegalStateException("Unknown AvAction resource id: " + id);
        }
    }

    public static void setScanButtonState(@NonNull ScannerBaseView.ScanButtonState state,
                                          @NonNull TextView startButtonText,
                                          @NonNull View startView,
                                          @NonNull View pauseView) {
        switch (state) {
            case Start:
                startButtonText.setText(R.string.str_antivirus_start_scan_button);
                startView.setEnabled(true);
                pauseView.setEnabled(false);
                break;
            case Stop:
                startButtonText.setText(R.string.str_antivirus_cancel_scan_button);
                startView.setEnabled(true);
                pauseView.setEnabled(false);
                break;
            case Stopping:
                startButtonText.setText(R.string.str_antivirus_cancelling_scan_button);
                startView.setEnabled(false);
                pauseView.setEnabled(false);
                break;
            default:
                throw new IllegalStateException("Unknown button state: " + state);
        }
    }

    public static void setPauseButtonState(@NonNull ScannerBaseView.PauseButtonState state,
                                           @NonNull TextView pauseButtonText,
                                           @NonNull View pauseView) {
        switch (state) {
            case Disabled:
                pauseButtonText.setEnabled(false);
                pauseView.setEnabled(false);
                pauseButtonText.setText(R.string.str_antivirus_pause_scan_button);
                break;
            case Pause:
                pauseButtonText.setEnabled(true);
                pauseView.setEnabled(true);
                pauseButtonText.setText(R.string.str_antivirus_pause_scan_button);
                break;
            case Resume:
                pauseButtonText.setEnabled(true);
                pauseView.setEnabled(true);
                pauseButtonText.setText(R.string.str_antivirus_resume_scan_button);
                break;
            default:
                throw new IllegalStateException("Unknown button state: " + state);
        }
    }


}
