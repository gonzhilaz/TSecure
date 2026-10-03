/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.impl;

import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.telecom.TelecomManager;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DefaultDialerRequestActivity extends AppCompatActivity {

    private boolean mBeenHere;

    @Override
    protected void onStart() {
        super.onStart();
        startActivity(
                new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                        .putExtra(
                                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                                getPackageName()
                        )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBeenHere) {
            finish();
        }
        mBeenHere = true;
    }
}
