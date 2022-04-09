package com.vzardd.greenqube;

import android.app.Application;

import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;

import java.util.ArrayList;

public class ApplicationClass extends Application {
    private static final String ONESIGNAL_APP_ID = "2a56313c-7191-4628-aabd-aca2d800a91f";
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        OneSignal.setNotificationWillShowInForegroundHandler(new OneSignal.OSNotificationWillShowInForegroundHandler() {
            @Override
            public void notificationWillShowInForeground(OSNotificationReceivedEvent notificationReceivedEvent) {
                notificationReceivedEvent.complete(notificationReceivedEvent.getNotification());
            }
        });
    }
}
