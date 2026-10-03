/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.sdk.impl;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import android.content.Context;
import android.util.Log;

import com.kavsdk.KavSdk;
import com.kavsdk.accessibility.OpenAccessibilitySettingsException;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.appcontrol.model.AppControlManager;
import com.kavsdkexample.appcontrol.model.AppControlCategory;
import com.kavsdk.appcontrol.AppControl;
import com.kavsdk.appcontrol.AppControlAction;
import com.kavsdk.appcontrol.AppControlItem;
import com.kavsdk.appcontrol.AppControlFactory;
import com.kavsdk.appcontrol.AppControlExtendedListener;
import com.kavsdk.appcontrol.AppControlApplicationsInfo;
import com.kavsdk.appcategorizer.AppCategory;
import com.kavsdkexample.appcontrol.model.AppControlMode;
import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.io.IOException;

@UiThread
public class AppControlManagerImpl implements AppControlManager, AppControlExtendedListener {

    private static final String TAG = AppControlManagerImpl.class.getSimpleName();
    private final AppControl mAppControl;
    private AppControlModel mAppControlModel;
    private final ThreadManager mThreadManager;

    AppControlManagerImpl(@NonNull ThreadManager threadManager,
                          @NonNull Context context,
                          @NonNull Intent blockingIntent) {
        AppControl appControl = null;
        try {
            appControl = AppControlFactory.getInstance(context);
            appControl.setBlockingIntent(blockingIntent);
        } catch (SdkLicenseViolationException e) {
            e.printStackTrace();
        }

        mThreadManager = threadManager;
        mAppControl = appControl;
    }

    @Override
    public void enableAppControl(boolean isChecked) {
        if (isChecked) {
            mAppControl.start();
        } else {
            mAppControl.stop();
        }
    }

    @Override
    public void useWindowManagerForBlocking(boolean isUsed) {
        mAppControl.setExtendedListener(isUsed ? this : null);
    }

    @Override
    public void setMode(AppControlMode mode) {
        if (mode == AppControlMode.BothLists) {
            mAppControl.setMode(com.kavsdk.appcontrol.AppControlMode.BothLists);
        } else if (mode == AppControlMode.BlockList) {
            mAppControl.setMode(com.kavsdk.appcontrol.AppControlMode.BlockList);
        } else if (mode == AppControlMode.AllowList) {
            mAppControl.setMode(com.kavsdk.appcontrol.AppControlMode.AllowList);
        }
    }

    @Override
    public void saveChanges() {
        try {
            mAppControl.saveChanges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AppControlAction onAppsOpened(AppControlApplicationsInfo info) {
        Log.d(TAG, "Apps opened");
        Log.d(TAG, "Current apps: " + info.getCurrentAppsInfo().keySet());
        Log.d(TAG, "Previous apps: " + info.getPreviousAppsInfo().keySet());
        return AppControlAction.Default;
    }

    @Override
    public AppControlAction onBlock(AppControlApplicationsInfo info) {
        Log.i(TAG, "Blocking at SdkExample's side");
        Log.d(TAG, "Current apps: " + info.getCurrentAppsInfo().keySet());
        Log.d(TAG, "Previous apps: " + info.getPreviousAppsInfo().keySet());
        mThreadManager.runOnUiThread(() -> {
            mAppControlModel.showDialog();
        });
        return AppControlAction.Allow;
    }

    @Override
    public AppControlAction onAbsentInBothLists(AppControlApplicationsInfo info) {
        Log.i(TAG, "Apps is absent in both lists");
        Log.d(TAG, "Current apps: " + info.getCurrentAppsInfo().keySet());
        Log.d(TAG, "Previous apps: " + info.getPreviousAppsInfo().keySet());
        return AppControlAction.Default;
    }

    @Override
    public void openSettings() {
        try {
            KavSdk.getAccessibility().openSettings();
        } catch (OpenAccessibilitySettingsException e) {
            e.printStackTrace();
        }

        KavSdk.getAccessibility().setStateListener(
                enabled -> Log.i(TAG, String.format("Accessibility is %s", enabled ? "enabled" : "disabled"))
        );

        boolean enabled = KavSdk.getAccessibility().isSettingsOn();
        Log.i(TAG, String.format("Accessibility is %s", enabled ? "enabled" : "disabled"));
    }

    @Override
    public void addItem(final AppControlMode mode, final String packageName, final String category) {
        AppControlItem item = AppControlFactory.createItem(packageName, stringToCategory(category));
        if (mode == AppControlMode.BlockList) {
            mAppControl.getBlockList().addItem(item);
        } else if (mode == AppControlMode.AllowList) {
            mAppControl.getAllowList().addItem(item);
        }
    }

    @Override
    public int getItemsCount(final AppControlMode mode) {
        if (mode == AppControlMode.BlockList) {
            return mAppControl.getBlockList().getItemsCount();
        } else if (mode == AppControlMode.AllowList) {
            return mAppControl.getAllowList().getItemsCount();
        }
        return 0;
    }

    @Override
    public String getPackage(final AppControlMode mode, int index) {
        if (mode == AppControlMode.BlockList) {
            return mAppControl.getBlockList().getItem(index).getPackage();
        } else if (mode == AppControlMode.AllowList) {
            return mAppControl.getAllowList().getItem(index).getPackage();
        }
        return null;
    }

    @Override
    public String getCategory(final AppControlMode mode, int index) {
        AppCategory category = null;
        if (mode == AppControlMode.BlockList) {
            category = mAppControl.getBlockList().getItem(index).getCategory();
        } else if (mode == AppControlMode.AllowList) {
            category = mAppControl.getAllowList().getItem(index).getCategory();
        }
        if (category != null) {
            return category.toString();
        }
        return null;
    }

    @Override
    public void deleteItem(final AppControlMode mode, int index) {
        if (mode == AppControlMode.BlockList) {
            mAppControl.getBlockList().deleteItem(index);
        } else if (mode == AppControlMode.AllowList) {
            mAppControl.getAllowList().deleteItem(index);
        }
    }

    @Override
    public void setAppControlModel(@NonNull final AppControlModel appControlModel) {
        mAppControlModel = appControlModel;
    }

    private AppCategory stringToCategory(final String category) {
        if (category == null) {
            return null;
        }

        if (category.compareTo(AppControlCategory.BusinessSoftware.getName()) == 0) {
            return AppCategory.BusinessSoftware;
        } else if (category.compareTo(AppControlCategory.EducationalSoftware.getName()) == 0) {
            return AppCategory.EducationalSoftware;
        } else if (category.compareTo(AppControlCategory.Entertainment.getName()) == 0) {
            return AppCategory.Entertainment;
        } else if (category.compareTo(AppControlCategory.Entertainment_Games.getName()) == 0) {
            return AppCategory.Entertainment_Games;
        } else if (category.compareTo(AppControlCategory.Entertainment_HomeFamilyHobbiesHealth.getName()) == 0) {
            return AppCategory.Entertainment_HomeFamilyHobbiesHealth;
        } else if (category.compareTo(AppControlCategory.Entertainment_OnlineShopping.getName()) == 0) {
            return AppCategory.Entertainment_OnlineShopping;
        } else if (category.compareTo(AppControlCategory.Entertainment_SocialNetworks.getName()) == 0) {
            return AppCategory.Entertainment_SocialNetworks;
        } else if (category.compareTo(AppControlCategory.GraphicDesignSoftware.getName()) == 0) {
            return AppCategory.GraphicDesignSoftware;
        } else if (category.compareTo(AppControlCategory.Information.getName()) == 0) {
            return AppCategory.Information;
        } else if (category.compareTo(AppControlCategory.Information_MappingApplications.getName()) == 0) {
            return AppCategory.Information_MappingApplications;
        } else if (category.compareTo(AppControlCategory.Information_Medical.getName()) == 0) {
            return AppCategory.Information_Medical;
        } else if (category.compareTo(AppControlCategory.Information_Weather.getName()) == 0) {
            return AppCategory.Information_Weather;
        } else if (category.compareTo(AppControlCategory.Information_Transport.getName()) == 0) {
            return AppCategory.Information_Transport;
        } else if (category.compareTo(AppControlCategory.InternetSoftware_ImVoipAndVideo.getName()) == 0) {
            return AppCategory.InternetSoftware_ImVoipAndVideo;
        } else if (category.compareTo(AppControlCategory.InternetSoftware_OnlineStorage.getName()) == 0) {
            return AppCategory.InternetSoftware_OnlineStorage;
        } else if (category.compareTo(AppControlCategory.InternetSoftware_SoftwareDownloaders.getName()) == 0) {
            return AppCategory.InternetSoftware_SoftwareDownloaders;
        } else if (category.compareTo(AppControlCategory.Multimedia.getName()) == 0) {
            return AppCategory.Multimedia;
        } else if (category.compareTo(AppControlCategory.OperatingSystemsAndUtilities.getName()) == 0) {
            return AppCategory.OperatingSystemsAndUtilities;
        } else if (category.compareTo(AppControlCategory.OperatingSystemsAndUtilities_Launchers.getName()) == 0) {
            return AppCategory.OperatingSystemsAndUtilities_Launchers;
        } else if (category.compareTo(AppControlCategory.Browsers.getName()) == 0) {
            return AppCategory.Browsers;
        } else if (category.compareTo(AppControlCategory.DeveloperTools.getName()) == 0) {
            return AppCategory.DeveloperTools;
        } else if (category.compareTo(AppControlCategory.GoldenImage.getName()) == 0) {
            return AppCategory.GoldenImage;
        } else if (category.compareTo(AppControlCategory.InternetSoftware.getName()) == 0) {
            return AppCategory.InternetSoftware;
        } else if (category.compareTo(AppControlCategory.NetworkingInfrastructureSoftware.getName()) == 0) {
            return AppCategory.NetworkingInfrastructureSoftware;
        } else if (category.compareTo(AppControlCategory.NetworkingSoftware.getName()) == 0) {
            return AppCategory.NetworkingSoftware;
        } else if (category.compareTo(AppControlCategory.OperatingSystemsAndUtilities_SystemUtilities.getName()) == 0) {
            return AppCategory.OperatingSystemsAndUtilities_SystemUtilities;
        } else if (category.compareTo(AppControlCategory.SecuritySoftware.getName()) == 0) {
            return AppCategory.SecuritySoftware;
        } else if (category.compareTo(AppControlCategory.OtherSoftware.getName()) == 0) {
            return AppCategory.OtherSoftware;
        } else if (category.compareTo(AppControlCategory.Unknown.getName()) == 0) {
            return AppCategory.Unknown;
        } else if (category.compareTo(AppControlCategory.BusinessSoftware_EmailSoftware.getName()) == 0) {
            return AppCategory.BusinessSoftware_EmailSoftware;
        }

        return null;
    }
}