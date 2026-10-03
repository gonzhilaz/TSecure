/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view.impl;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kavsdkexample.appcontrol.R;
import com.kavsdkexample.appcontrol.view.AppControlUtils;

public class AppBlockActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.block_app_screen);

        final Button button = (Button) findViewById(R.id.block_app_button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AppControlUtils.launchHomeScreen(AppBlockActivity.this, true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AppControlUtils.launchHomeScreen(this, false);
    }

    @Override
    protected void onStop() {
        AppControlUtils.launchHomeScreen(this, false);
        super.onStop();
    }
}
