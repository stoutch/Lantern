package com.example.deept_000.masproject;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Chris on 4/6/2015.
 */
public class LocationUtil {
    /**
     * Our best guess as to the user's location
     */
    private static Location mLastLocation;

    /**
     * Keep track of the location that was last added to the arraylist
     */
    private static Location mLastAddedLocation;

    /**
     * The time in milliseconds corresponding to when the location was recorded
     */
    private static long mLastLocationTime;

    private static final double THRESHOLD_DISTANCE = 10;

    /**
     * List of Location objects recorded over time. Collected then sent to server every once in a while
     */
    private static ArrayList<Location> mRecordedLocations;

    public static void updateLocation(Location location) {
        mLastLocation = location;
        Calendar c = Calendar.getInstance();
        mLastLocationTime = c.getTimeInMillis();
        // add time checks to see if we should actually update
        // check if the new location is far enough away too
        Log.d("updateLocation", "Speed:" + location.getSpeed());
        if (mRecordedLocations == null) {
            mRecordedLocations = new ArrayList<Location>();
        }
        if (shouldAddLocation(location)) {
            Log.d("updateLocation", "Adding location");
            mLastAddedLocation = mLastLocation;
            mRecordedLocations.add(mLastLocation);
        }
    }

    private static boolean shouldAddLocation(Location location) {
        double speed = location.getSpeed(), distance = 0;
        if (mLastAddedLocation != null) {
            distance = location.distanceTo(mLastAddedLocation);
        }
        // probably need to update this
        // might not actually need threshold distance
        if (speed > 0 || distance > THRESHOLD_DISTANCE) {
            return true;
        }
        return false;
    }


    public static Location getLastLocation() {
        return mLastLocation;
    }

    public static ArrayList<Location> getLocations() {
        if (mRecordedLocations == null) {
            mRecordedLocations = new ArrayList<Location>();
        }
        return mRecordedLocations;
    }
}
