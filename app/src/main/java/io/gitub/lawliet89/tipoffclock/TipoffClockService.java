package io.gitub.lawliet89.tipoffclock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TipoffClockService extends Service {
    public TipoffClockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
