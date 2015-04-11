package com.example.deept_000.masproject.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.example.deept_000.masproject.LocationUtil;
import com.example.deept_000.masproject.web.HttpSender;
import com.example.deept_000.masproject.web.WebResponseListener;

import java.util.ArrayList;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SendLocationService extends IntentService {
    private final String ADDRESS = "http://173.236.254.243:8080";
    protected static final String TAG = "SendLocationService";

    public SendLocationService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "--------Firing alarm event--------");
        HttpSender sender = new HttpSender();
        ArrayList<Location> locationHistory = LocationUtil.getLocations();
        Location location;
        double lat, lng;
        for (int i = 0; i < locationHistory.size(); i++) {
            location = locationHistory.get(i);
            lat = location.getLatitude();
            lng = location.getLongitude();
            Log.d(TAG, String.format("Location - lat: %f, long: %f", lat, lng));

            // wrap the intent in a final variable (needed for putting in listeners)
            final Intent tempIntent = intent;

            // check for when we need to let the system know the wakeful intent is done
            final boolean done = (i == locationHistory.size() - 1 ? true : false);
            String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&type=lighting&value=-10", ADDRESS, lat, lng);
            sender.sendHttpRequest(uri, "", "POST", new WebResponseListener() {
                @Override
                public void OnSuccess(String response, String... params) {
                    Log.d(TAG, "Successfully sent location from alarm");
                    if (done) {
                        SendLocationReceiver.completeWakefulIntent(tempIntent);
                    }
                }

                @Override
                public void OnError(Exception e, String... params) {
                    if (done) {
                        SendLocationReceiver.completeWakefulIntent(tempIntent);
                    }

                }

                @Override
                public void OnProcessing() {

                }
            });
        }

    }

//    protected void createLocationRequest() {
//        LocationRequest request = new LocationRequest();
//        request.setInterval(15 * 1000);
//        request.setFastestInterval(5 * 1000);
//        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//    }

}
