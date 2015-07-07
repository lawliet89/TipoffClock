package io.gitub.lawliet89.tipoffclock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TipoffClockService extends Service {

    public int ONGOING_NOTIFICATION_ID = 1359;
    private NotificationCompat.Builder builder;
    private TimeZone timezone;
    private boolean enabled;
    private SharedPreferences settings;
    private DateFormat dateFormat;
    private DateFormat timeFormat;

    public TipoffClockService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // Binding is not supported
        return null;
    }

    @Override
    public void onCreate() {
        // Set up notification builder
        Intent settingsIntent = new Intent(this, SettingActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(SettingActivity.class);
        stackBuilder.addNextIntent(settingsIntent);
        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(intent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.notification_clock_icon));
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);
        updateSettings();
    }

    private void updateSettings() {
        timezone = TimeZone.getTimeZone(settings.getString("setting_clock_timezone",
                Calendar.getInstance().getTimeZone().getID()));
        enabled = settings.getBoolean("setting_enabled", true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateClock();
        return START_REDELIVER_INTENT;
    }

    private Calendar getCalendar() {
        return Calendar.getInstance(timezone);
    }

    private Notification buildNotification() {
        Calendar calendar = getCalendar();
        Date date = calendar.getTime();
        String title = String.format("%s - %s", timeFormat.format(date), dateFormat.format(date));
        builder.setSmallIcon(R.drawable.ic_clock_24h, getImageOffsetIndex(calendar))
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentTitle(title)
                .setContentText(timezone.getDisplayName());
        return builder.build();
    }

    private void updateClock() {
        if (enabled) {
            startForeground(ONGOING_NOTIFICATION_ID, buildNotification());
        } else
            stopSelf();
    }

    public static int getImageOffsetIndex(Calendar calendar) {
        return getImageOffsetIndex(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    public static int getImageOffsetIndex(int hour, int minute) {
        return hour * 60 + minute;
    }
}
