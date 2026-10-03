/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check.nonmvp;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import android.widget.ArrayAdapter;

import com.kaspersky.components.urlchecker.UrlCategory;
import com.kaspersky.components.urlchecker.UrlCategoryExt;
import com.kaspersky.components.urlchecker.UrlInfo;
import com.kavsdk.KavSdk;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.urlchecker.UrlCheckService;
import com.kavsdkexample.core.app.view.impl.nonmvp.BaseSimpleDialog;
import com.kavsdkexample.url_check.R;

import java.util.List;

/**
 * This dialog checks a reputation of an entered URL and
 * shows the result in a toast.
 */
public class UrlReputationDialog extends BaseSimpleDialog {
    private static final String URL_CHECK_DIALOG_TAG          = "url_check_dialog";
    private static final String USE_EXTENDED_CATEGORIES_PARAM = "ext_categories";
    private boolean mExtendedCategoriesEnabled;


    public static void show(@NonNull FragmentManager manager, boolean extendedCategories) {
        UrlReputationDialog fragment = new UrlReputationDialog();
        Bundle args = new Bundle();
        args.putBoolean(USE_EXTENDED_CATEGORIES_PARAM, extendedCategories);
        fragment.setArguments(args);
        fragment.show(manager, URL_CHECK_DIALOG_TAG);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Expected arguments");
        }
        mExtendedCategoriesEnabled = arguments.getBoolean(USE_EXTENDED_CATEGORIES_PARAM, false);
        return super.onCreateDialog(savedInstanceState);
    }

    /**
     * This method checks the URL reputation of an URL entered into the edit box.
     * In order to verify a URL, SDK asks a KSN server via a network.
     * Therefore, this method has to be invoked from a non-GUI thread
     *
     * @return a verdict about a "goodness" of the URL
     */
    @Override
    public String check() {
        if (!KavSdk.isInitialized()) {
            return mContext.getString(R.string.str_url_check_not_inited);
        }

        try {
            final UrlCheckService service = new UrlCheckService(mContext);
            String url = mTextView.getText().toString();

            UrlInfo result;
            if (mExtendedCategoriesEnabled) {
                result = service.checkUrlExt(url);
            } else {
                result = service.checkUrl(url);
            }

            if (result != null) {
                return mContext.getString(R.string.str_url_reputation_dialog_result) + getInfo(result);
            } else {
                return mContext.getString(R.string.str_url_reputation_dialog_result_null);
            }

        } catch (SdkLicenseViolationException e) {
            return mContext.getString(R.string.str_url_reputation_dialog_result_exception) + e.toString();
        } catch (Throwable e) {
            return mContext.getString(R.string.str_url_reputation_dialog_result_exception) + e.toString();
        }
    }

    /**
     * Returns the string representation of a URL reputation
     */
    private static String getInfo(UrlInfo urlInfo) {
        StringBuilder result = new StringBuilder(30); // preallocate
        result.append("Verdict: ");
        switch (urlInfo.mVerdict) {
            case UrlInfo.VERDICT_UNKNOWN:
                result.append("UNKNOWN");
                break;
            case UrlInfo.VERDICT_GOOD:
                result.append("GOOD");
                break;
            case UrlInfo.VERDICT_BAD:
                result.append("BAD");
                break;
            default:
                throw new IllegalArgumentException("Illegal verdict");
        }
        result.append(", categories: ");

        if (urlInfo.mCategoriesExt == null) {
            List<UrlCategory> categories = UrlCategory.getCategoriesByMask(urlInfo.mCategories);
            for (UrlCategory category : categories) {
                result.append(' ');
                result.append(category.name());
            }
        } else {
            for (UrlCategoryExt category : urlInfo.mCategoriesExt) {
                result.append(' ');
                result.append(category.name());
            }
        }
        return result.toString();
    }

    @Override
    protected int getHintTextId() {
        return R.string.str_url_reputation_dialog_enter_url;
    }

    @Override
    protected ArrayAdapter<?> getAdapter() {
        return null;
    }

    @Override
    protected int getTitleId() {
        return R.string.str_url_check_check_url_button;
    }
}