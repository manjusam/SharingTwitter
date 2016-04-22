package com.example.tweets.sharingtwit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by admin on 13/08/2015.
 */
public class ServiceBoot extends BroadcastReceiver {
    //ProfileTimelineFragment profile = new ProfileTimelineFragment();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("SERVICEBOOT", "DealBootReceiver invoked, configuring AlarmManager");
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(context,TwitterUpdateReceiver.class),0);

        // Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30000,
                AlarmManager.INTERVAL_HOUR, pendingIntent);
    }
}
/*
* @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Constants.LOG_TAG, "DealBootReceiver invoked, configuring AlarmManager");
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(context, DealAlarmReceiver.class), 0);

        // use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, Constants.ALARM_TRIGGER_AT_TIME,
                Constants.ALARM_INTERVAL, pendingIntent);
    }*/