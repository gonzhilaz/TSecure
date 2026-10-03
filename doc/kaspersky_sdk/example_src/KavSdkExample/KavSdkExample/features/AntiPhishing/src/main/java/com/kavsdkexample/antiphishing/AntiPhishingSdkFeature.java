/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.kaspersky.components.smsantiphishing.SmsAntiPhishingEvent;
import com.kaspersky.components.smsantiphishing.SmsAntiPhishingListener;
import com.kaspersky.components.smsantiphishing.SmsAntiPhishingManager;
import com.kaspersky.components.smsantiphishing.SmsAntiPhishingManagerFactory;
import com.kaspersky.components.urlchecker.UrlInfo;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.urlchecker.UrlCheckService;
import com.kavsdkexample.antiphishing.di.DaggerComponent;
import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.antiphishing.view.impl.FeatureClickListener;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import java.util.Map;

import javax.inject.Inject;

import butterknife.internal.Utils;
import dagger.android.AndroidInjector;

@Keep
public class AntiPhishingSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject WebFilterModel               mModel;
    @Inject ThreadManager                mThreadManager;

    private static final String TAG = AntiPhishingSdkFeature.class.getSimpleName();
    private static final SmsAntiPhishingManagerFactory smsApmFactory = new SmsAntiPhishingManagerFactory();

    public AntiPhishingSdkFeature(@NonNull Application            application,
                                  @NonNull ServiceInteractor      serviceInteractor,
                                  @NonNull NotificationInteractor notificationInteractor) {
        super(application, serviceInteractor, notificationInteractor);
        DaggerComponent.builder()
                .application(application)
                .build()
                .inject(this);
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        mModel.setSdkStatusProvider(this);
        mModel.setInteractors(serviceInteractor, notificationInteractor);
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.ANTIPHISHING;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(activity, viewGroup, R.layout.antiphishing_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.anti_phishing_button, R.string.str_anti_phishing_open_url_monitor_settings, listener);

        final SwitchCompat smsApSwitch = (SwitchCompat) activity.findViewById(R.id.sms_anti_phishing_switch);
        try {
            final SmsAntiPhishingManager smsApManager = smsApmFactory.createManager(mContext, new UrlCheckService(activity.getApplicationContext()));
            smsApSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        smsApManager.addListener(new SmsAntiPhishingListener() {
                            @Override
                            public void handleSmsEvent(@NonNull SmsAntiPhishingEvent smsEvent) {
                                Log.i(TAG, "sms event from " + smsEvent.getPhoneNumber());
                                for (Map.Entry<String, UrlInfo> entry : smsEvent.getExtractedUrls().entrySet()) {
                                    Log.i(TAG, "url: " + entry.getKey() + ", info: " + entry.getValue().toString());
                                }
                            }
                        });
                    } else {
                        smsApManager.removeAllListeners();
                    }
                }
            });
        }
        catch (SdkLicenseViolationException exception) {
            smsApSwitch.setEnabled(false);
        }
   }

    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return false;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
