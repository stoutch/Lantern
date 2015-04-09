package com.example.deept_000.masproject.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Chris on 3/31/2015.
 */
public class GetLocationBootReceiver extends BroadcastReceiver {
    GetLocationReceiver alarm = new GetLocationReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setAlarm(context);
        }
    }
}
