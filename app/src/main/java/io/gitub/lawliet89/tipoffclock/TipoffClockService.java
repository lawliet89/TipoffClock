package io.gitub.lawliet89.tipoffclock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TipoffClockService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final int ONGOING_NOTIFICATION_ID = 1359;
    public static boolean service_enabled = false;

    private static final String ICON_TEMPLATE = "ic_clock_24h_%02d_%02d";

    private NotificationCompat.Builder builder;
    private TimeZone timezone;
    private int priority;
    private SharedPreferences settings;
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private UpdateClockReceiver updateReceiver;
    public TipoffClockService() {

    }

    public static int getImageOffsetIndex(Calendar calendar) {
        return getImageOffsetIndex(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    public static int getImageOffsetIndex(int hour, int minute) {
        return hour * 60 + minute;
    }

    public static boolean startServiceIfEnabled(Context context) {
        updateEnabled(context);
        Intent intent = new Intent(context, TipoffClockService.class);
        if (service_enabled) {
            context.startService(intent);
        }
        else {
            context.stopService(intent);
        }
        return service_enabled;
    }

    private static void updateEnabled(Context context) {
        service_enabled = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("setting_enabled", true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (startServiceIfEnabled(this)) {
            updateSettings();
            updateClock();
        }
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
                        R.mipmap.ic_launcher_icon));

        // Setup settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);
        updateSettings();
        settings.registerOnSharedPreferenceChangeListener(this);

        // Setup every minute update
        updateReceiver = new UpdateClockReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(updateReceiver, filter);
    }

    private void updateSettings() {
        timezone = TimeZone.getTimeZone(settings.getString("setting_clock_timezone",
                Calendar.getInstance().getTimeZone().getID()));
        switch(settings.getString("setting_priority", "default")) {
            case "max":
                priority = NotificationCompat.PRIORITY_MAX;
                break;
            case "high":
                priority = NotificationCompat.PRIORITY_HIGH;
                break;
            case "low":
                priority = NotificationCompat.PRIORITY_LOW;
                break;
            case "min":
                priority = NotificationCompat.PRIORITY_MIN;
                break;
            default:
                priority = NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateClock();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(updateReceiver);
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    private Calendar getCalendar() {
        return Calendar.getInstance(timezone);
    }

    private Notification buildNotification() {
        Calendar calendar = getCalendar();
        Date date = calendar.getTime();
        String title = String.format("%s - %s", timeFormat.format(date), dateFormat.format(date));
        builder.setSmallIcon(getResource(calendar))
                .setCategory(Notification.CATEGORY_STATUS)
                .setContentTitle(title)
                .setContentText(timezone.getDisplayName(timezone.inDaylightTime(date), TimeZone.LONG))
                .setPriority(priority);
        return builder.build();
    }

    private int getResource(Calendar calendar) {
        return getResource(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

    private int getResource(int hour, int minute) {
        return getResources().getIdentifier(String.format(ICON_TEMPLATE, hour, minute),
                "drawable", getApplicationInfo().packageName);

    }

    private void updateClock() {
        startForeground(ONGOING_NOTIFICATION_ID, buildNotification());
    }

    private class UpdateClockReceiver extends BroadcastReceiver {
        private TipoffClockService service;

        public UpdateClockReceiver(TipoffClockService service) {
            this.service = service;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                service.updateClock();
            }
        }
    }
}
