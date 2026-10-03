/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view.impl;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.Settings;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.Window;
import android.widget.Toast;

import com.kavsdkexample.R;
import com.kavsdkexample.R2;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.SdkFeatureAndroid;
import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.features.TabInfo;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.ui.BaseActivity;
import com.kavsdkexample.presenter.MainPresenter;
import com.kavsdkexample.view.MainView;
import com.kavsdkexample.view.MainViewState;
import com.kavsdkexample.view.permissions.impl.InsufficientPermissionsFragment;
import com.kavsdkexample.view.sdk.anti_phishing.impl.AntiPhishingFragment;
import com.kavsdkexample.view.sdk.anti_virus.impl.AntiVirusFragment;
import com.kavsdkexample.view.sdk.data_protection.impl.DataProtectionFragment;
import com.kavsdkexample.view.sdk.device_reputation.impl.DeviceReputationFragment;
import com.kavsdkexample.view.sdk.license.impl.LicenseInfoFragment;
import com.kavsdkexample.view.sdk.other_features.impl.OtherFeaturesFragment;
import com.kavsdkexample.view.sdk.privacy.impl.PrivacyFragment;
import com.kavsdkexample.view.sdk.secure_connectivity.impl.SecureConnectivityFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class MainActivity extends    BaseActivity<MainView, MainViewState, MainPresenter>
                          implements MainView {
    private static final int PERMISSION_REQUEST_CODE       = 1;
    private static final int OVERLAY_PERMISSION_REQ_CODE   = 2;
    private static final int OPEN_DIRECTORY_REQ_CODE       = 3;
    private static final int ALL_FILES_PERMISSION_REQ_CODE = 4;
    private static final int BACKGROUND_LOCATION_REQ_CODE  = 5;
    private static final int CALL_SCREENING_ROLE_REQ_CODE  = 6;
    private static final int DATA_DIR_PERMISSION_REQ_CODE  = 7;
    private static final int OBB_DIR_PERMISSION_REQ_CODE   = 8;
    private static final int IGNORE_POWER_SAVING_REQ_CODE  = 9;

    @Inject                MainPresenter         mPresenter;
    @Inject                TabInfoFactoryAndroid mTabsFactory;
    @BindView(R2.id.pager) ViewPager             mViewPager;

    private               MainViewState         mViewState;
    private               TabsAdapter           mTabsAdapter;
    private               boolean               mReportOverlayResult;
    private               boolean               mReportAccessStorageResult;
    private               boolean               mReportAllFilesPermissionResult;
    private               boolean               mReportCallScreeningRoleResult;
    private               int                   mSdCardPathsCount;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdk_main_activity);
        AndroidInjection.inject(this);
        ButterKnife.bind(this);

        mTabsAdapter = new TabsAdapter(this, getSupportFragmentManager());
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.options_pager_separator_margin));

        if (savedInstanceState == null) {
            mViewPager.setAdapter(mTabsAdapter);
            mViewState = null;
        } else {
            mViewState = MainViewHelper.fromBundle(savedInstanceState);
        }

        mPresenter.viewCreated(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mTabsAdapter.onRestoreState(savedInstanceState);
            mViewPager.setAdapter(mTabsAdapter);
        }
    }

    @Override
    protected MainPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected MainViewState getViewState() {
        return mViewState;
    }

    @Override
    public void showTab(@NonNull TabInfo tabInfo) {
        if (tabInfo instanceof TabInfoAndroid) {
            mTabsAdapter.setTab((TabInfoAndroid) tabInfo);
        } else {
            throw new IllegalStateException("Unsupported type");
        }
    }

    @Override
    public void showInsufficientPermissionTab() {
        String tabName = getString(R.string.str_tab_insufficient_permissions_title);
        mTabsAdapter.setTab(mTabsFactory.createTabInfo(tabName, InsufficientPermissionsFragment.class.getName()));
    }

    @NonNull
    private SdkFeatureAndroid checkFeatureNotNull(TabDescription tabDescription) {
        SdkFeature feature = tabDescription.getFeature();
        if (feature == null) {
            throw new IllegalStateException("Expected not null feature for " + tabDescription.getTabId());
        }
        if (feature instanceof SdkFeatureAndroid) {
            return (SdkFeatureAndroid) feature;
        } else {
            throw new IllegalStateException("Unsupported feature type");
        }
    }

    @Override
    public void showFeatureTabs(@NonNull List<TabDescription> tabs) {
        List<TabInfoAndroid> tabsInfo = new ArrayList<>();
        for (TabDescription tabDescription : tabs) {
            TabDescription.TabId id = tabDescription.getTabId();
            String tabName;
            switch (id) {
                case ApAndFakeAppsProtection:
                    tabName = getString(R.string.str_tab_anti_phishing_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, AntiPhishingFragment.class.getName()));
                    break;
                case DataProtection:
                    tabName = getString(R.string.str_tab_data_protection_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, DataProtectionFragment.class.getName()));
                    break;
                case DeviceReputation:
                    tabName = getString(R.string.str_tab_device_reputation_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, DeviceReputationFragment.class.getName()));
                    break;
                case AntivirusProtection:
                    tabName = getString(R.string.str_tab_av_protection_tab);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, AntiVirusFragment.class.getName()));
                    break;
                case SecureConnectivity:
                    tabName = getString(R.string.str_tab_secure_connectivity_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, SecureConnectivityFragment.class.getName()));
                    break;
                case OtherFeatures:
                    tabName = getString(R.string.str_tab_other_features_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, OtherFeaturesFragment.class.getName()));
                    break;
                case LicenseInfo:
                    tabName = getString(R.string.str_tab_license_info_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, LicenseInfoFragment.class.getName()));
                    break;
                case Privacy:
                    tabName = getString(R.string.str_tab_privacy_title);
                    tabsInfo.add(mTabsFactory.createTabInfo(tabName, PrivacyFragment.class.getName()));
                    break;
                case Agreements:
                case EasyScanner:
                case SecurityScanner:
                case SelfDefense:
                case SimpleUrlReputation:
                case Statistics:
                case Updates:
                case WhoCalls:
                    tabsInfo.add(checkFeatureNotNull(tabDescription).provideFeatureTab(null));
                    break;
                default:
                    throw new IllegalStateException("Unsupported tab id: " + id);
            }

        }
        mTabsAdapter.setTabs(tabsInfo);
    }

    @Override
    public void showInitErrorTab(boolean licenseError) {
        String tabName = getString(licenseError ? R.string.str_tab_license_info_title : R.string.str_sdk_init_error_tab_name);
        mTabsAdapter.setTab(mTabsFactory.createTabInfo(tabName, LicenseInfoFragment.class.getName()));
    }

    @Override
    public void clearTabs() {
        mTabsAdapter.setTabs(Collections.emptyList());
    }

    @Override
    public void requestGenericPermissions(@NonNull String[] permissions) {
        if (permissions.length == 0) {
            throw new IllegalStateException("Permissions list can't be empty or null");
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @TargetApi(30)
    @Override
    public void requestAllFilesAccessPermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ALL_FILES_PERMISSION_REQ_CODE);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @TargetApi(23)
    @Override
    public void requestIgnoreBatterySaving() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, IGNORE_POWER_SAVING_REQ_CODE);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    @Override
    public void requestDataDirPermission() {
        requestDocumentPermission("data", DATA_DIR_PERMISSION_REQ_CODE);
    }

    @TargetApi(Build.VERSION_CODES.R)
    @Override
    public void requestObbDirPermission() {
        requestDocumentPermission("obb", OBB_DIR_PERMISSION_REQ_CODE);
    }

    @TargetApi(Build.VERSION_CODES.R)
    public void requestDocumentPermission(String data, int code) {
        final StorageManager storageManager = (StorageManager) getApplication().getSystemService(Context.STORAGE_SERVICE);
        final Intent intent = storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        final String targetDirectory = "Android%2F" + data;
        Uri uri = (Uri) intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
        String scheme = uri.toString();
        scheme = scheme.replace("/root/", "/document/") + "%3A" + targetDirectory;
        uri = Uri.parse(scheme);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        startActivityForResult(intent, code);
    }

    @TargetApi(30)
    @Override
    public void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                BACKGROUND_LOCATION_REQ_CODE);
    }

    @TargetApi(29) // lint does not understand VERSION_CODE constant for some reason
    @Override
    public void requestCallScreeningRole() {
        startActivityForResult(((RoleManager)getSystemService(ROLE_SERVICE)).createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), CALL_SCREENING_ROLE_REQ_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void requestDefaultDialerStatus() {
        mPresenter.onDefaultDialerStatusRequested();
        startActivity(new Intent(this, DefaultDialerRequestActivity.class));
    }

    @Override
    public void requestNotificationAccessPermission() {
        mPresenter.onNotificationAccessPermissionRequested();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> grantedPermissions = new ArrayList<>(permissions.length);
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                }
            }
            if (mPresenter.isSubscribed()) {
                mPresenter.processGenericPermissionsResults(grantedPermissions);
            } else {
                mPresenter.runOnSubscription(() -> mPresenter.processGenericPermissionsResults(grantedPermissions));
            }
        } else if (requestCode == BACKGROUND_LOCATION_REQ_CODE) {
            if (mPresenter.isSubscribed()) {
                mPresenter.processBackgroundLocationPermissionResult();
            } else {
                mPresenter.runOnSubscription(() -> mPresenter.processBackgroundLocationPermissionResult());
            }
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE); //wait for onActivityResult()
    }

    @SuppressWarnings("PMD.AvoidCatchingNPE")
    @TargetApi(29)
    @Override
    public void requestStorageAccessPermissions(@NonNull List<String> sdCardPaths) {
        mSdCardPathsCount += sdCardPaths.size();
        for (int i = 0; i < mSdCardPathsCount; ++i) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                StorageManager sm = getSystemService(StorageManager.class);

                try {
                    StorageVolume volume = sm.getStorageVolume(new File(sdCardPaths.get(i)));
                    if (volume != null) {
                        Intent intent = volume.createOpenDocumentTreeIntent();
                        startActivityForResult(intent, OPEN_DIRECTORY_REQ_CODE);
                        continue;
                    }
                } catch (NullPointerException ignored) {
                    // do nothing
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Uri uri = Uri.fromFile(new File(sdCardPaths.get(i)));
                    if (uri != null) {
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                    }
                }
                startActivityForResult(intent, OPEN_DIRECTORY_REQ_CODE);
            }
        }
    }

    @Override
    public void showSdkLoadingTime(long loadingTimeMs) {
        Toast.makeText(this, getString(R.string.str_sdk_loading_time, String.valueOf(loadingTimeMs)), Toast.LENGTH_LONG).show();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            // onActivityResult can be called before onStart on some OS versions
            if (mPresenter.isSubscribed()) {
                mPresenter.processOverlayPermissionResult();
            } else {
                mReportOverlayResult = true;
            }
            return;
        } else if (requestCode == OPEN_DIRECTORY_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            mSdCardPathsCount--;
            if (mSdCardPathsCount == 0) {
                if (mPresenter.isSubscribed()) {
                    mPresenter.processStorageAccessPermissionResult();
                } else {
                    mReportAccessStorageResult = true;
                }
            }
        }
        if (requestCode == ALL_FILES_PERMISSION_REQ_CODE) {
            if (mPresenter.isSubscribed()) {
                mPresenter.processAllFilesPermissionResult();
            } else {
                mReportAllFilesPermissionResult = true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <Build.VERSION_CODES.TIRAMISU) {
                requestDataDirPermission();
            }
            return;
        }
        if (requestCode == CALL_SCREENING_ROLE_REQ_CODE) {
            if (mPresenter.isSubscribed()) {
                mPresenter.processCallScreeningRoleResult();
            } else {
                mReportCallScreeningRoleResult = true;
            }
            return;
        }
        if (requestCode == DATA_DIR_PERMISSION_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                getContentResolver().takePersistableUriPermission((Uri) data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            requestObbDirPermission();
            return;
        }
        if (requestCode == OBB_DIR_PERMISSION_REQ_CODE && resultCode == RESULT_OK) {
            getContentResolver().takePersistableUriPermission((Uri) data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return;
        }
        if (requestCode == IGNORE_POWER_SAVING_REQ_CODE) {
            if (mPresenter.isSubscribed()) {
                mPresenter.processIgnoreBatterySavingResult();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mReportOverlayResult) {
            mPresenter.processOverlayPermissionResult();
            mReportOverlayResult = false;
        }

        if (mReportAllFilesPermissionResult) {
            mPresenter.processAllFilesPermissionResult();
            mReportAllFilesPermissionResult = false;
        }

        if (mReportAccessStorageResult) {
            mPresenter.processStorageAccessPermissionResult();
            mReportAccessStorageResult = false;
        }

        if (mReportCallScreeningRoleResult) {
            mPresenter.processCallScreeningRoleResult();
            mReportCallScreeningRoleResult = false;
        }

        if (mPresenter.isDefaultDialerStatusRequested()) {
            mPresenter.processDefaultDialerStatusResult();
        }

        if (mPresenter.isNotificationAccessPermissionRequested()) {
            mPresenter.processNotificationAccessPermissionResult();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.viewDestroyed(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTabsAdapter.onSaveState(outState);
        MainViewHelper.toBundle(outState, mViewState);

    }
}
