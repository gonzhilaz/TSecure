/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.BuildConfig;
import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.features.TabInfo;
import com.kavsdkexample.core.app.features.TabInfoFactory;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.model.AppModelStatusObserver;
import com.kavsdkexample.model.LicenseStatusObserver;
import com.kavsdkexample.model.SdkFeatureProvider;
import com.kavsdkexample.model.app.permissions.PermissionRepository;
import com.kavsdkexample.model.app.sdk.SdkWrappersFactory;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;
import com.kavsdkexample.model.app.settings.Settings;
import com.kavsdkexample.model.app.tasks.ActivateLicenseResultsObserver;
import com.kavsdkexample.model.app.tasks.InitSdkResultsObserver;
import com.kavsdkexample.model.app.tasks.TasksFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

@UiThread
public final class AppModelImpl extends    BaseModelImpl
                                implements AppModel,
                                           SdkFeature.TabCompletionCallback,
                                           InitSdkResultsObserver,
                                           ActivateLicenseResultsObserver {

    private static final int PERMISSIONS_TYPES_COUNT              = 9;
    private static final int GENERIC_PERMISSION_CODE              = 0;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE  = 1;
    private static final int OVERLAY_PERMISSION_CODE              = 2;
    private static final int SDCARD_PERMISSION_CODE               = 3;
    private static final int ALL_FILES_PERMISSION_CODE            = 4;
    private static final int CALL_SCREENING_SERVICE_ROLE_CODE     = 5;
    private static final int DEFAULT_DIALER_STATUS_CODE           = 6;
    private static final int IGNORE_POWER_SAVE_CODE               = 7;
    private static final int NOTIFICATION_ACCESS_PERMISSION_CODE  = 8;

    private static final Map<TabDescription.TabId, TabMetaInfo> TAB_META_INFO;
    private final Settings                mSettings;
    private final SdkFeatureProvider      mFeatureProvider;
    private final PermissionRepository    mPermissionRepository;
    private final TasksFactory            mTasksFactory;
    private final ThreadManager           mThreadManager;
    private final ExecutorService         mExecutor;
    private final SdkWrappersFactory      mSdkWrappersFactory;
    private final TabInfoFactory          mTabInfoFactory;
    private final ServiceInteractor       mServiceInteractor;
    private final List<TabDescription>    mTabsInfo;
    private       boolean                 mLoading;
    private       boolean                 mInitSdkTaskCalled;
    @Nullable
    private       SdkLicenseInfo          mLicenseInfo;
    private final boolean[]               mCheckedPermissions = new boolean[PERMISSIONS_TYPES_COUNT];

    static {
        TAB_META_INFO = new TreeMap<>();

        TAB_META_INFO.put(
            TabDescription.TabId.ApAndFakeAppsProtection,
            new TabMetaInfo(
                TabDescription.TabId.ApAndFakeAppsProtection,
                new String[] {
                    SdkFeature.ANTIPHISHING,
                    SdkFeature.APP_CATEGORY,
                    SdkFeature.APP_CONTROL
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.EasyScanner,
            new TabMetaInfo(
                TabDescription.TabId.EasyScanner,
                new String[] {
                    SdkFeature.ANTIVIRUS_EASY_SCANNER
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.SecurityScanner,
            new TabMetaInfo(
                TabDescription.TabId.SecurityScanner,
                new String[] {
                    SdkFeature.ANTIVIRUS_SECURITY_SCANNER
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.AntivirusProtection,
            new TabMetaInfo(
                TabDescription.TabId.AntivirusProtection,
                new String[] {
                    SdkFeature.ANTIVIRUS_SINGLE_THREAD_SCANNER,
                    SdkFeature.ANTIVIRUS_MULTI_THREAD_SCANNER,
                    SdkFeature.ANTIVIRUS_PUA_SCANNER,
                    SdkFeature.ANTIVIRUS_PUA_MONITOR,
                    SdkFeature.ANTIVIRUS_RTP_MONITOR,
                    SdkFeature.ANTIVIRUS_APP_MONITOR,
                    SdkFeature.ANTIVIRUS_FOLDER_MONITOR,
                    SdkFeature.ANTIVIRUS_QUARANTINE,
                    SdkFeature.ANTIVIRUS_OTHER
                }
            )
        );

        if (BuildConfig.COMPROMISED_ACCOUNTS_ENABLED || BuildConfig.COMPROMISED_PASSWORDS_ENABLED) {
            TAB_META_INFO.put(
                TabDescription.TabId.Privacy,
                new TabMetaInfo(
                    TabDescription.TabId.Privacy,
                    new String[]{
                        SdkFeature.PRIVACY_COMPROMISED_ACCOUNTS,
                        SdkFeature.PRIVACY_COMPROMISED_PASSWORDS
                    }
                )
            );
        }

        TAB_META_INFO.put(
            TabDescription.TabId.SecureConnectivity,
            new TabMetaInfo(
                TabDescription.TabId.SecureConnectivity,
                new String[] {
                    SdkFeature.SECURE_CONNECTION,
                    SdkFeature.WIFI_SAFETY,
                    SdkFeature.URL_CHECK,
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.DeviceReputation,
            new TabMetaInfo(
                TabDescription.TabId.DeviceReputation,
                new String[] {
                    SdkFeature.ANTIVIRUS_ROOT_CHECKER,
                    SdkFeature.ANTIVIRUS_SELF_CHECKER,
                    SdkFeature.DEVICE_CONFIGURATION,
                    SdkFeature.SIM_WATCH,
                    SdkFeature.DEVICE_FINGERPRINT
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.DataProtection,
            new TabMetaInfo(
                TabDescription.TabId.DataProtection,
                new String[] {
                    SdkFeature.SAFE_INPUT,
                    SdkFeature.SECURE_STORAGE,
                    SdkFeature.SECURE_SMS
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.OtherFeatures,
            new TabMetaInfo(
                TabDescription.TabId.OtherFeatures,
                new String[] {
                    SdkFeature.ANTISPAM,
                    SdkFeature.FINGERPRINT_MONITOR
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.SelfDefense,
            new TabMetaInfo(
                TabDescription.TabId.SelfDefense,
                new String[] {
                    SdkFeature.SELF_DEFENSE
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.Agreements,
            new TabMetaInfo(
                TabDescription.TabId.Agreements,
                new String[] {
                    SdkFeature.AGREEMENTS
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.Updates,
            new TabMetaInfo(
                TabDescription.TabId.Updates,
                new String[] {
                    SdkFeature.UPDATE
                }
            )
        );

        TAB_META_INFO.put(
            TabDescription.TabId.Statistics,
            new TabMetaInfo(
                TabDescription.TabId.Statistics,
                new String[] {
                    SdkFeature.STATISTIC
                }
            )
        );

        TAB_META_INFO.put(
                TabDescription.TabId.WhoCalls,
                new TabMetaInfo(
                        TabDescription.TabId.WhoCalls,
                        new String[]{
                                SdkFeature.WHO_CALLS
                        }
                )
        );

        TAB_META_INFO.put(
                TabDescription.TabId.SimpleUrlReputation,
                new TabMetaInfo(
                        TabDescription.TabId.SimpleUrlReputation,
                        new String[]{
                                SdkFeature.SIMPLE_URL_REPUTATION
                        }
                )
        );

        TAB_META_INFO.put(
                TabDescription.TabId.LicenseInfo,
                new TabMetaInfo(
                        TabDescription.TabId.LicenseInfo,
                        null
                )
        );
    }

    private boolean mDefaultDialerStatusRequested;
    private boolean mNotificationAccessPermissionRequested;

    public AppModelImpl(@NonNull Settings               settings,
                        @NonNull SdkFeatureProvider     featureProvider,
                        @NonNull PermissionRepository   permissionRepository,
                        @NonNull ThreadManager          threadManager,
                        @NonNull ExecutorService        executor,
                        @NonNull TasksFactory           tasksFactory,
                        @NonNull SdkWrappersFactory     sdkWrappersFactory,
                        @NonNull TabInfoFactory         tabInfoFactory,
                        @NonNull ServiceInteractor      serviceInteractor,
                        @NonNull NotificationInteractor notificationInteractor) {
        mSettings               = settings;
        mFeatureProvider        = featureProvider;
        mPermissionRepository   = permissionRepository;
        mThreadManager          = threadManager;
        mExecutor               = executor;
        mTasksFactory           = tasksFactory;
        mSdkWrappersFactory     = sdkWrappersFactory;
        mTabInfoFactory         = tabInfoFactory;
        mServiceInteractor      = serviceInteractor;
        mTabsInfo               = new ArrayList<>();

        notificationInteractor.createNotificationChannel();
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public void checkAcceptEula() {
        SdkFeature eulaFeature = mFeatureProvider.findSdkFeature(SdkFeature.EULA);
        if (eulaFeature == null) {
            notifyObservers(AppModelStatusObserver::onSkipEula, AppModelStatusObserver.class);
        } else {
            TabInfo tabInfo = eulaFeature.provideFeatureTab(this);
            if (tabInfo == null) {
                notifyObservers(AppModelStatusObserver::onSkipEula, AppModelStatusObserver.class);
            } else {
                notifyObservers(observer -> observer.onEulaAcceptRequired(tabInfo), AppModelStatusObserver.class);
            }
        }
    }

    private void setWizardCompleted() {
        if (!mSettings.getWizardCompleted()) {
            mSettings.setWizardCompleted(true);
        }
    }

    private boolean doesPermissionNeedToBeChecked(int permissionCode) {
        if (mCheckedPermissions[permissionCode]) {
            return false;
        }
        mCheckedPermissions[permissionCode] = true;
        return true;
    }

    @Override
    public void checkPermissions() {
        if (mSettings.getWorkWithoutPermissions() || areAllPermissionsGranted()) {
            setWizardCompleted();
            mDefaultDialerStatusRequested = false;
            mNotificationAccessPermissionRequested = false;
            notifyObservers(AppModelStatusObserver::onPermissionsDone, AppModelStatusObserver.class);
        } else {
            if (doesPermissionNeedToBeChecked(GENERIC_PERMISSION_CODE)) {
                List<String> requiredPermission = mPermissionRepository.getRequiredGenericPermissions();
                if (!requiredPermission.isEmpty()) {
                    String[] permissions = requiredPermission.toArray(new String[0]);
                    notifyObservers(observer -> observer.onRequestGenericPermissions(permissions), AppModelStatusObserver.class);
                    return;
                }
            }
            if (doesPermissionNeedToBeChecked(BACKGROUND_LOCATION_PERMISSION_CODE)
                    && !mPermissionRepository.checkBackgroundLocationPermission()) {
                notifyObservers(AppModelStatusObserver::onRequestBackgroundLocationPermission, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(OVERLAY_PERMISSION_CODE)
                    && !mPermissionRepository.checkOverlayPermission()) {
                notifyObservers(AppModelStatusObserver::onRequestOverlayPermission, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(SDCARD_PERMISSION_CODE)) {
                final List<String> paths = mPermissionRepository.getSdCardPathsWithMissingPermissions();
                if (!paths.isEmpty()) {
                    notifyObservers(observer -> observer.onRequestStorageAccessPermissions(paths), AppModelStatusObserver.class);
                    return;
                }
            }
            if (doesPermissionNeedToBeChecked(ALL_FILES_PERMISSION_CODE)
                    && !mPermissionRepository.checkAllFilesPermission()) {
                notifyObservers(AppModelStatusObserver::onRequestAllFilesPermission, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(CALL_SCREENING_SERVICE_ROLE_CODE)
                    && !mPermissionRepository.checkCallScreeningServiceRole()) {
                notifyObservers(AppModelStatusObserver::onRequestCallScreeningRole, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(DEFAULT_DIALER_STATUS_CODE)
                    && !mPermissionRepository.checkDefaultDialerStatus()) {
                notifyObservers(AppModelStatusObserver::onRequestDefaultDialerStatus, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(IGNORE_POWER_SAVE_CODE)
                    && !mPermissionRepository.checkIgnoreBatterySaving()) {
                notifyObservers(AppModelStatusObserver::onRequestIgnoreBatterySaving, AppModelStatusObserver.class);
                return;
            }
            if (doesPermissionNeedToBeChecked(NOTIFICATION_ACCESS_PERMISSION_CODE)
                    && !mPermissionRepository.checkNotificationAccessPermission()) {
                notifyObservers(AppModelStatusObserver::onRequestNotificationAccessPermission, AppModelStatusObserver.class);
                return;
            }
            notifyObservers(AppModelStatusObserver::onAllPermissionsChecked, AppModelStatusObserver.class);
        }
    }

    @Override
    public void ignoreInsufficientPermissions() {
        mSettings.setWorkWithoutPermissions(true);
        checkPermissions();
    }

    @Override
    @NonNull
    public String getInsufficientPermissions() {
        List<String> requiredPermission = mPermissionRepository.getRequiredGenericPermissions();
        List<String> additionalPermissions = mPermissionRepository.getRequiredAdditionalPermissions();
        List<String> storageAccessPermissions = mPermissionRepository.getSdCardPathsWithMissingPermissions();
        final int capacity = 25 * (requiredPermission.size() + additionalPermissions.size() + storageAccessPermissions.size());
        StringBuilder sb = new StringBuilder(capacity);
        for (String permission : requiredPermission) {
            sb.append(permission).append('\n');
        }
        for (String permission : additionalPermissions) {
            sb.append(permission).append('\n');
        }
        for (String permission : storageAccessPermissions) {
            sb.append("Storage access permission: ").append(permission).append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean areAllPermissionsGranted() {
        return (mPermissionRepository.getRequiredGenericPermissions().isEmpty()
                && mPermissionRepository.checkOverlayPermission()
                && mPermissionRepository.getSdCardPathsWithMissingPermissions().isEmpty()
                && mPermissionRepository.checkAllFilesPermission()
                && mPermissionRepository.checkBackgroundLocationPermission())
                && mPermissionRepository.checkCallScreeningServiceRole()
                && mPermissionRepository.checkDefaultDialerStatus()
                && mPermissionRepository.checkIgnoreBatterySaving()
                && mPermissionRepository.checkNotificationAccessPermission();
    }

    @Override
    public boolean areAllPermissionsChecked() {
        for (boolean permissionChecked : mCheckedPermissions) {
            if (!permissionChecked) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setAllPermissionsUnchecked() {
        Arrays.fill(mCheckedPermissions, false);
    }

    @Override
    public void onTabResult(@NonNull SdkFeature.TabResult result) {
        switch (result) {
            case Accept:
                notifyObservers(AppModelStatusObserver::onAcceptEula, AppModelStatusObserver.class);
                break;
            case Reject:
                notifyObservers(AppModelStatusObserver::onRejectEula, AppModelStatusObserver.class);
                break;
            default:
                throw new IllegalStateException("Unexpected result: " + result);
        }
    }

    @Override
    public void onSdkInited(final long initTimeMs) {
        mLoading     = false;
        mLicenseInfo = mSdkWrappersFactory.createLicenseInfo(null);
        notifyObservers(observer -> observer.onSdkLoadingCompleted(initTimeMs), AppModelStatusObserver.class);
        onSdkInited();
    }

    @Override
    public void onInitException(@NonNull Exception e) {
        mLoading           = false;
        mInitSdkTaskCalled = false;
        mLicenseInfo       = mSdkWrappersFactory.createLicenseInfo(e);
        notifyObservers(observer -> observer.onSdkInitException(mLicenseInfo), AppModelStatusObserver.class);
        onSdkInitFailed();
    }

    @Override
    public boolean maybeInitSdkAndFeatures(@Nullable SdkStatusObserver observer, boolean ignoreWizard) {
        if (observer != null) {
            addObserver(observer);
        }
        if (!mInitSdkTaskCalled && (ignoreWizard || mSettings.getWizardCompleted())) {
            mExecutor.execute(mTasksFactory.createInitSdkTask(mThreadManager, mServiceInteractor, this));
            mInitSdkTaskCalled = true;
            mLoading           = true;
            notifyObservers(AppModelStatusObserver::onSdkLoading, AppModelStatusObserver.class);
            return true;
        }
        return false;
    }

    @Override
    public void activateLicense(@NonNull String code) {
        mExecutor.execute(mTasksFactory.createActivateLicenseTask(mThreadManager, this, code));
    }

    @Override
    public void setProxyAuthCredentials(@NonNull String login, @NonNull String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelProxyAuth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDefaultDialerStatusRequested() {
        mDefaultDialerStatusRequested = true;
    }

    @Override
    public boolean isDefaultDialerStatusRequested() {
        return mDefaultDialerStatusRequested;
    }

    @Override
    public void onNotificationAccessPermissionRequested() {
        mNotificationAccessPermissionRequested = true;
        mPermissionRepository.requestNotificationAccessPermission();
    }

    @Override
    public boolean isNotificationAccessPermissionRequested() {
        return mNotificationAccessPermissionRequested;
    }

    @Override
    @NonNull
    public SdkLicenseInfo getLicenseInfo() {
        if (mLicenseInfo == null) {
            throw new IllegalStateException("License info is not available yet");
        }
        return mLicenseInfo;
    }

    @Override
    @NonNull
    public List<TabDescription> getTabs() {
        if (mTabsInfo.isEmpty()) {
            for (TabMetaInfo meta : TAB_META_INFO.values()) {
                if (meta.mFeatureNames == null) {
                    mTabsInfo.add(mTabInfoFactory.createTabDescription(meta.mTabId, null));
                } else {
                    for (String featureName : meta.mFeatureNames) {
                        SdkFeature feature = mFeatureProvider.findSdkFeature(featureName);
                        if (feature != null) {
                            mTabsInfo.add(mTabInfoFactory.createTabDescription(meta.mTabId, feature));
                            break;
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableList(mTabsInfo);
    }

    @NonNull
    @Override
    public List<SdkFeature> getFeaturesForTab(@NonNull TabDescription.TabId id) {
        TabMetaInfo metaInfo      = TAB_META_INFO.get(id);
        if (metaInfo == null || metaInfo.mFeatureNames == null || metaInfo.mFeatureNames.length == 0) {
            throw new IllegalStateException("No features found for tab id: " + id);
        }

        List<SdkFeature> features = new ArrayList<>();
        for (String featureName : metaInfo.mFeatureNames) {
            SdkFeature feature = mFeatureProvider.findSdkFeature(featureName);
            if (feature != null) {
                features.add(feature);
            }
        }

        return features;
    }

    @NonNull
    @Override
    public Collection<SdkFeature> getAllFeatures() {
        return mFeatureProvider.allFeatures();
    }

    @Override
    public void onSuccess(long expirationDate) {
        mLicenseInfo = mSdkWrappersFactory.createLicenseInfo(null);
        notifyObservers(observer -> observer.onSuccess(mLicenseInfo), LicenseStatusObserver.class);
        onSdkInited();
    }

    @Override
    public void onFailed(@NonNull Exception exception) {
        mLicenseInfo = mSdkWrappersFactory.createLicenseInfo(exception);
        notifyObservers(observer -> observer.onError(mLicenseInfo), LicenseStatusObserver.class);
        onSdkInitFailed();
    }

    private static final class TabMetaInfo {
        @NonNull
        final TabDescription.TabId mTabId;
        @Nullable
        final String[]             mFeatureNames;

        TabMetaInfo(@NonNull TabDescription.TabId tabId, @Nullable String[] featureNames) {
            mTabId        = tabId;
            mFeatureNames = featureNames;
        }
    }
}
