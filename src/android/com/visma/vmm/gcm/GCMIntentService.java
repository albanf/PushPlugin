package com.visma.vmm.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.LinkedList;
import java.util.List;

public class GCMIntentService extends com.plugin.gcm.GCMIntentService {

    private static List<String> notifications = new LinkedList<String>();

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

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(context.getApplicationInfo().icon)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(extras.getString("title"))
                        .setContentIntent(contentIntent)
                        .setDeleteIntent(getDeleteIntent());

//        if (message != null) {
//            mBuilder.setContentText(message);
//        } else {
//            mBuilder.setContentText("<missing message content>");
//        }
//
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (notifications.size() > 1) {
            mBuilder.setContentTitle(notifications.size() + " new tasks");
            inboxStyle.setBigContentTitle(notifications.size() + " new tasks");

            for (String notification : notifications) {
                inboxStyle.addLine(notification);
            }
            inboxStyle.setSummaryText("Summary bla bla bla");
            mBuilder.setContentText("Expand to see new messages");
            mBuilder.setStyle(inboxStyle);
        } else {
            mBuilder.setContentTitle(notifications.size() + " new task");
            mBuilder.setContentText(message);
        }

        mBuilder.setAutoCancel(false);

        String msgcnt = extras.getString("msgcnt");
        mBuilder.setNumber(Integer.parseInt(msgcnt));

        mNotificationManager.notify((String) appName, NOTIFICATION_ID, mBuilder.build());
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
