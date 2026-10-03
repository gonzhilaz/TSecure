/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check.nonmvp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.view.View;

import com.kavsdkexample.url_check.R;


public class FeatureClickListener implements View.OnClickListener {
    private final FragmentActivity mActivity;

    public FeatureClickListener(@NonNull FragmentActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.check_url_reputation_button) {
            UrlReputationDialog.show(mActivity.getSupportFragmentManager(), false);
        } else if (id == R.id.check_url_reputation_ext_button) {
            UrlReputationDialog.show(mActivity.getSupportFragmentManager(), true);
        } else if (id == R.id.check_financial_category_button) {
            FinancialCategoryDialog.show(mActivity.getSupportFragmentManager());
        }
    }
}
