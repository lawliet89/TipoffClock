package io.gitub.lawliet89.tipoffclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by yongwen on 06/07/15.
 */
public class ServiceStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startService(context);
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, TipoffClockService.class);
        context.startService(serviceIntent);
    }
}
