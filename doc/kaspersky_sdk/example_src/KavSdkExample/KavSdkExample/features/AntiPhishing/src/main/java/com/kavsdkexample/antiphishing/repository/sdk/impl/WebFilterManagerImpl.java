/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.webfilter.WebFilterControl;
import com.kavsdk.webfilter.WebFilterControlFactoryImpl;
import com.kavsdkexample.antiphishing.repository.sdk.WebFilterManager;
import com.kavsdkexample.antiphishing.repository.settings.Settings;
import com.kaspersky.components.urlchecker.UrlCategory;
import com.kaspersky.components.urlchecker.UrlCategoryExt;
import com.kavsdk.KavSdk;
import com.kavsdk.accessibility.OpenAccessibilitySettingsException;

import java.util.Set;

public final class WebFilterManagerImpl implements WebFilterManager {
    /* Let's filter web browsers now. You can change it and try different flags combinations */
    private static final int WEB_FILTER_FLAGS = 0x0;
    /* Web filter parameters */
    private static final String LOCAL_HOST    = "127.0.0.1"; //NOPMD
    private static final int PORT_NUMBER      = 3128;

    private final Context  mContext;
    private final Settings mSettings;
    @Nullable
    private volatile WebFilterControl mWebFilterControl;

    static final UrlCategory[] WF_CATEGORIES = new UrlCategory[] {
            UrlCategory.AdultContent,
            UrlCategory.IllegalSoft,
            UrlCategory.AlcoholTobaccoNarcotics,
            UrlCategory.Violence,
            UrlCategory.Profanity,
            UrlCategory.Weapons,
            UrlCategory.Gambling,
            UrlCategory.ChatsForumsAndIM,
            UrlCategory.WebMail,
            UrlCategory.ShopsAndAuctions,
            UrlCategory.SocialNet,
            UrlCategory.Recruitment,
            UrlCategory.Anonymizers,
            UrlCategory.Payments,
            UrlCategory.CasualGames,
            UrlCategory.Counterfeit,
            UrlCategory.SoftwareAudioVideo,
            UrlCategory.GamblingLotteriesSweepstakes,
            UrlCategory.InternetCommunicationMedia,
            UrlCategory.ElectronicCommerce,
            UrlCategory.ComputerGames,
            UrlCategory.ReligionsAndReligiousAssociations,
            UrlCategory.NewsMedia,
            UrlCategory.PornoAndErotic,
            UrlCategory.Nudism,
            UrlCategory.Lingerie,
            UrlCategory.SexEducation,
            UrlCategory.Dating18plus,
            UrlCategory.LGBT,
            UrlCategory.SexShops,
            UrlCategory.Narcotics,
            UrlCategory.Alcohol,
            UrlCategory.Tobacco,
            UrlCategory.CultureAndSociety,
            UrlCategory.GovernmentPoliticsLaws,
            UrlCategory.HomeAndFamily,
            UrlCategory.Military,
            UrlCategory.RestaurantsCafeFood,
            UrlCategory.AstrologyAndEsoterica,
            UrlCategory.Torrents,
            UrlCategory.FileSharing,
            UrlCategory.AudioAndVideo,
            UrlCategory.InformationTechnologies,
            UrlCategory.SearchEnginesAndServices,
            UrlCategory.HostingAndDomains,
            UrlCategory.Ads,
            UrlCategory.Banks,
            UrlCategory.RentRealEstateServices,
            UrlCategory.Phishing,
            UrlCategory.Malware
    };

    public WebFilterManagerImpl(@NonNull Context context, @NonNull Settings settings) {
        mContext  = context;
        mSettings = settings;
    }

    @WorkerThread
    void initWebFilter() throws SdkLicenseViolationException {
        boolean isWifiProxyEnabled = mSettings.getWifiProxyState();
        WebFilterControl webFilterControl = mWebFilterControl;

        if (webFilterControl != null && webFilterControl.isEnabled()) {
            webFilterControl.enable(false);
        }
        int flags = WEB_FILTER_FLAGS;
        if (!isWifiProxyEnabled) {
            flags |= WebFilterControl.DISABLE_PROXY;
        } else {
            flags &= ~WebFilterControl.DISABLE_PROXY;
        }

        int newPort  = mSettings.getProxyPort();
        webFilterControl = new WebFilterControlFactoryImpl().
                create(
                    new UrlFilterHandlerImpl(mContext),
                    mContext,
                    flags,
                    LOCAL_HOST,
                    newPort,
                    LOCAL_HOST,
                    newPort
                );
        if (mSettings.getWebFilterState())  {
            webFilterControl.enable(true);
        }
        mWebFilterControl = webFilterControl;
    }

    @Override
    public boolean isWebFilterInitialised() {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl == null) {
            return false;
        }
        return true;
    }

    @Override
    public void enableWebFiltering(boolean isEnabled) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null && mSettings.getWebFilterState() != isEnabled) {
            webFilterControl.enable(isEnabled);
        }
    }

    @Override
    public void enableExtCategories(boolean isEnabled) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.enableExtendedCategories(isEnabled);
        }
    }

    @Override
    public void enableIgnorePowerSaveMode(boolean isEnabled) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.setPowerSaveModeIgnored(isEnabled);
        }
    }

    @Override
    public int getExclusionsCount() {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            return webFilterControl.getExclusionsCount();
        }
        return 0;
    }

    @Override
    public String getExclusionAt(int index) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            return webFilterControl.getExclusion(index);
        }
        return null;
    }

    @Override
    public CharSequence[] getCategoryNames() {
        CharSequence[] categoryNames;
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl == null) {
            return null;
        }

        if (webFilterControl.isExtendedCategoriesEnabled()) {
            categoryNames = new CharSequence[UrlCategoryExt.values().length];
            for (int i = 0; i < UrlCategoryExt.values().length; ++i) {
                categoryNames[i] = UrlCategoryExt.values()[i].toString();
            }
        } else {
            categoryNames = new CharSequence[WF_CATEGORIES.length];
            for (int i = 0; i < WF_CATEGORIES.length; ++i) {
                categoryNames[i] = WF_CATEGORIES[i].toString();
            }
        }
        return categoryNames;
    }

    @Override
    public boolean[] getCheckedItems() {
        boolean[] checkedItems;
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl == null) {
            return null;
        }

        if (webFilterControl.isExtendedCategoriesEnabled()) {
            checkedItems = new boolean[UrlCategoryExt.values().length];
            Set<UrlCategoryExt> enabledCategories = webFilterControl.getEnabledCategoriesExt();
            for (int i = 0; i < UrlCategoryExt.values().length; ++i) {
                checkedItems[i] = enabledCategories.contains(UrlCategoryExt.values()[i]);
            }
        } else {
            checkedItems = new boolean[WF_CATEGORIES.length];
            Set<UrlCategory> enabledCategories = webFilterControl.getEnabledCategories();
            for (int i = 0; i < WF_CATEGORIES.length; ++i) {
                checkedItems[i] = enabledCategories.contains(WF_CATEGORIES[i]);
            }
        }
        return checkedItems;
    }

    @Override
    public void setCategoryEnabled(int which, boolean isChecked) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl == null) {
            return;
        }

        if (webFilterControl.isExtendedCategoriesEnabled()) {
            if (isChecked) {
                webFilterControl.setCategoryEnabled(UrlCategoryExt.values()[which]);
            } else {
                webFilterControl.setCategoryDisabled(UrlCategoryExt.values()[which]);
            }
        } else {
            if (isChecked) {
                webFilterControl.setCategoryEnabled(WF_CATEGORIES[which]);
            } else {
                webFilterControl.setCategoryDisabled(WF_CATEGORIES[which]);
            }
        }
    }

    @Override
    public void saveCategories() {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.saveCategories();
        }
    }

    @Override
    public void openAccessibilitySettings() {
        try {
            KavSdk.getAccessibility().openSettings();
        } catch (OpenAccessibilitySettingsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restoreWifiProxySettings() {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.restoreWifiProxySettings();
        }
    }


    @Override
    public void addExclusion(String url) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.addExclusion(url);
        }
    }

    @Override
    public void saveExclusions() {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.saveExclusions();
        }
    }

    @Override
    public void removeExclusion(int index) {
        WebFilterControl webFilterControl = mWebFilterControl;
        if (webFilterControl != null) {
            webFilterControl.removeExclusion(index);
        }
    }
}