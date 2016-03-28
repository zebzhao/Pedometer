package com.pedometrak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pedometrak.util.Logger;

public class ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Logger.log("shutting down");

        context.startService(new Intent(context, PedometerManager.class));

        DatabaseManager db = DatabaseManager.getInstance(context);
        // TODO: Save workout data before exit
        db.close();
    }

}
