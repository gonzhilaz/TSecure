/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check.nonmvp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.widget.ArrayAdapter;

import com.kaspersky.components.financialcategorizer.FinanceCategory;
import com.kavsdk.KavSdk;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.urlchecker.UrlCheckService;
import com.kavsdkexample.core.app.view.impl.nonmvp.BaseSimpleDialog;
import com.kavsdkexample.url_check.R;

/**
 * This is a dialog for showing the functionality of the URL checker.
 * The dialog asks the user to enter a URL, checks it and shows the result
 * in a toast.
 */
public class FinancialCategoryDialog extends BaseSimpleDialog {

    private static final String FINANCIAL_URL_CHECK_DIALOG_TAG = "financial_url_check_dialog";

    public static void show(@NonNull FragmentManager manager) {
        FinancialCategoryDialog fragment = new FinancialCategoryDialog();
        fragment.show(manager, FINANCIAL_URL_CHECK_DIALOG_TAG);
    }

    @Override
    protected String check() {
        if (!KavSdk.isInitialized()) {
            return mContext.getString(R.string.str_url_check_not_inited);
        }

        try {
            final UrlCheckService service = new UrlCheckService(mContext);
            String url = mTextView.getText().toString();
            try {
                FinanceCategory result = service.checkBankUrl(url);
                if (result != null) {
                    return mContext.getString(R.string.str_financial_category_dialog_result) + result.toString();
                } else {
                    return mContext.getString(R.string.str_financial_category_dialog_result_null);
                }
            } catch (Exception e) {
                return mContext.getString(R.string.str_financial_category_dialog_exception) + e.toString();
            }
        } catch (SdkLicenseViolationException e) {
            return mContext.getString(R.string.str_financial_category_dialog_exception) + e.toString();
        } catch (Throwable e) {
            return mContext.getString(R.string.str_financial_category_dialog_exception) + e.toString();
        }
    }

    @Override
    protected int getHintTextId() {
        return R.string.str_financial_url_check_textview;
    }

    @Override
    protected ArrayAdapter<?> getAdapter() {
        return null;
    }

    @Override
    protected int getTitleId() {
        return R.string.str_url_check_check_bank_url_button;
    }
}
