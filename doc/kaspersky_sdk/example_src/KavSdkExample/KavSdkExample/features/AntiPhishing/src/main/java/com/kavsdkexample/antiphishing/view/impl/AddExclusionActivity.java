/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.view.impl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.kavsdkexample.antiphishing.utils.Consts;

/**
 * This is a sample activity used for adding URL exclusions.
 *      The activity serves a {@link android.content.Intent#CATEGORY_BROWSABLE}
 *      action (specified in Manifest)
 */
public class AddExclusionActivity extends Activity {
    /*
     * This method is invoked by Android when URLs start to open.
     * The method forwards the URLs to the main activity
     * for further handling
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newIntent = new Intent(this, WebFilterActivity.class);
        String url = getIntent().getStringExtra(Consts.EXTRA_URL);
        if (url != null) {
            newIntent.putExtra(Consts.EXTRA_URL, url);
        }
        startActivity(newIntent);
        finish();
    }
}
