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

public class TipoffClockService extends Service {
    public static final int ONGOING_NOTIFICATION_ID = 1359;
    public static boolean service_enabled = false;
    private NotificationCompat.Builder builder;
    private TimeZone timezone;
    private SharedPreferences settings;
    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private UpdateClockReceiver updateReceiver;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (startServiceIfEnabled(TipoffClockService.this)){
                updateSettings();
                updateClock();
            }
        }
    };

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

        // Setup settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
        timeFormat = android.text.format.DateFormat.getTimeFormat(this);
        updateSettings();
        settings.registerOnSharedPreferenceChangeListener(preferenceListener);

        // Setup every minute update
        updateReceiver = new UpdateClockReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(updateReceiver, filter);
    }

    private void updateSettings() {
        timezone = TimeZone.getTimeZone(settings.getString("setting_clock_timezone",
                Calendar.getInstance().getTimeZone().getID()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateClock();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(updateReceiver);
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
        startForeground(ONGOING_NOTIFICATION_ID, buildNotification());
        settings.unregisterOnSharedPreferenceChangeListener(preferenceListener);
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
