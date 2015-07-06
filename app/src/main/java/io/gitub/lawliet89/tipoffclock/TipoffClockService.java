package io.gitub.lawliet89.tipoffclock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class TipoffClockService extends Service {
    private Notification notification;

    public int ONGOING_NOTIFICATION_ID = 1359;

    public TipoffClockService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // Binding is not supported
        return null;
    }

    @Override
    public void onCreate() {
        Intent settingsIntent = new Intent(this, SettingActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SettingActivity.class);
        stackBuilder.addNextIntent(settingsIntent);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.notification_clock_icon)
            .setCategory(Notification.CATEGORY_STATUS)
            .setContentTitle("TipOff Clock")
            .setContentText("13:00")
            .setContentIntent(intent);
        notification = builder.build();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        return START_REDELIVER_INTENT;
    }
}
