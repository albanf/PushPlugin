package com.visma.vmm.gcm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.LinkedList;
import java.util.Queue;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GCMIntentService extends com.plugin.gcm.GCMIntentService {

    private static Queue<String> notifications = new LinkedList<String>();
    private static int additionalNotifications = 0;

    public static final int NOTIFICATION_ID = 237;

    @Override
    public void createNotification(Context context, Bundle extras) {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra("pushBundle", extras);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String message = extras.getString("message");
        notifications.add(message);
        if (notifications.size() > 5) {
            additionalNotifications++;
            notifications.remove();
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon((int) getResources().getIdentifier("ic_stat_notify", "drawable", getPackageName()))
                        .setWhen(System.currentTimeMillis())
                        .setTicker(extras.getString("title"))
                        .setContentIntent(contentIntent)
                        .setDeleteIntent(getDeleteIntent());

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (notifications.size() + additionalNotifications > 1) {
            mBuilder.setContentTitle(Integer.toString((notifications.size() + additionalNotifications)) + " " + getLocalizedText("pushplugin_new_tasks"));
            inboxStyle.setBigContentTitle(Integer.toString((notifications.size() + additionalNotifications)) + " " + getLocalizedText("pushplugin_new_tasks"));

            for (String notification : notifications) {
                inboxStyle.addLine(notification);
            }
            if (additionalNotifications > 0) {
                inboxStyle.setSummaryText("+ " + additionalNotifications + " " + getLocalizedText("pushplugin_more"));
            } else {
                inboxStyle.setSummaryText("");
            }
            mBuilder.setContentText("");
            mBuilder.setStyle(inboxStyle);
        } else {
            mBuilder.setContentTitle(getLocalizedText("pushplugin_new_task"));
            mBuilder.setContentText(message);
        }

        mBuilder.setAutoCancel(false);

        String msgcnt = extras.getString("msgcnt");
        mBuilder.setNumber(Integer.parseInt(msgcnt));

        mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
    }

    private String getLocalizedText(String identifier) {
        return getResources().getString((int) getResources().getIdentifier(identifier, "string", getPackageName()));
    }

    protected PendingIntent getDeleteIntent() {
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("notification_cancelled");
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel((String) getAppName(context), NOTIFICATION_ID);
        clearNotifications();
    }

    public static void clearNotifications() {
        notifications.clear();
        additionalNotifications = 0;
    }

    // Private is base class
    private static String getAppName(Context context) {
        CharSequence appName =
                context
                        .getPackageManager()
                        .getApplicationLabel(context.getApplicationInfo());

        return (String) appName;
    }

}
