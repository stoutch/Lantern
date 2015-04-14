package com.example.deept_000.masproject;

import android.graphics.Color;
import android.location.Location;

import com.example.deept_000.masproject.Gson.Heatmap;
import com.example.deept_000.masproject.web.HttpSender;
import com.example.deept_000.masproject.web.WebResponseListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Chris on 3/9/2015.
 */
public class HeatmapProvider {
    private boolean dumb = false;
    private final String ADDRESS = "http://173.236.254.243:8080";
    private final int RADIUS = 15;
    private final double OPACITY = .5;
    private GoogleMap mGoogleMap;
    private double mLat;
    private double mLong;
    private final int MAP_RADIUS = 2500;

    /**
     * Adds a heatmap to the map
     *
     * @param googleMap The map to which the heatmap will be added
     * @param location  The user's location
     */
    public void addHeatmap(GoogleMap googleMap, Location location) {
        mGoogleMap = googleMap;
        if (location != null) {
            mLat = location.getLatitude();
            mLong = location.getLongitude();
        }

        if (dumb) {
            addHeatMapDumb(googleMap);
            return;
        }
        getPositiveHeatmapFromServer();
        getNegativeHeatmapFromServer();
    }

    /**
     * Formulates the HTTP query for the server using our REST API guideline
     */
    private void getPositiveHeatmapFromServer() {
        HttpSender sender = new HttpSender();
        String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&radius=%d", ADDRESS, mLat, mLong, MAP_RADIUS);
        sender.sendHttpRequest(uri, null, "GET", new WebResponseListener() {
            @Override
            public void OnSuccess(String response, String... params) {
                onSuccessfulResponse(response, true);
            }

            @Override
            public void OnError(Exception e, String... params) {
                e.printStackTrace();
            }

            @Override
            public void OnProcessing() {
                // figure out how to show a spinner
            }
        });
    }

    /**
     * Formulates the HTTP query for the server using our REST API guideline
     */
    private void getNegativeHeatmapFromServer() {
        HttpSender sender = new HttpSender();
        String uri = String.format("%s/heatmaps/negative?lat=%f&lng=%f&radius=%d", ADDRESS, mLat, mLong, MAP_RADIUS);
        sender.sendHttpRequest(uri, null, "GET", new WebResponseListener() {
            @Override
            public void OnSuccess(String response, String... params) {
                onSuccessfulResponse(response, false);
            }

            @Override
            public void OnError(Exception e, String... params) {
                e.printStackTrace();
            }

            @Override
            public void OnProcessing() {
                // figure out how to show a spinner
            }
        });
    }

    private void onSuccessfulResponse(String response, boolean isPositive) {
        Gson gson = new Gson();
        Heatmap heatmap = gson.fromJson(response, Heatmap.class);
        if (heatmap != null) {
            heatmap.positive = isPositive;
        }
        if (heatmap != null && heatmap.success && heatmap.response != null) {
            //System.out.println(heatmap.toString());
            addPointsToMapWithWeight(heatmap);
        } else {
            System.out.println("Heatmap object is null");
        }
    }

    /**
     * Adds the lat lng points returned from the query to the map.
     *
     * @param heatmap The Heatmap object encapsulating data returned from the server
     */
    private void addPointsToMapWithWeight(Heatmap heatmap) {
        ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();
        for (Heatmap.Response response : heatmap.response) {
            int weight = response.weight * response.value / 10;
            list.add(new WeightedLatLng(new LatLng(response.loc.coordinates[1], response.loc.coordinates[0]), weight));
            //System.out.println("Adding " + response.loc.coordinates[1] + ", " + response.loc.coordinates[0] + ": " + weight);
        }
        HeatmapTileProvider mProvider;
        if (heatmap.positive) {
            mProvider = new HeatmapTileProvider.Builder().weightedData(list).radius(RADIUS).opacity(OPACITY).build();
        } else {
            int[] colors = {
                    Color.rgb(0, 235, 255), // blue
                    Color.rgb(175, 0, 255)  // violet
            };
            float[] startPoints = {
                    0.2f, 1f
            };
            Gradient gradient = new Gradient(colors, startPoints);
            mProvider = new HeatmapTileProvider.Builder().weightedData(list).radius(RADIUS).opacity(OPACITY).gradient(gradient).build();
        }
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider)).setZIndex(10);
    }

    /**
     * Adds hardcoded heatmap values to the map
     *
     * @param googleMap The map to which the values are added.
     */
    private void addHeatMapDumb(GoogleMap googleMap) {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        double minLat = 33.775238;
        double minLong = -84.396663;
        double maxLat = 33.776972;
        double maxLong = -84.396744;
        double randLat, randLong;
        Random rand = new Random();
        for (int i = 0; i < 15; i++) {
            randLat = rand.nextDouble() * (maxLat - minLat) + minLat;
            randLong = rand.nextDouble() * (maxLong - minLong) + minLong;
            list.add(new LatLng(randLat, randLong));
        }

        minLat = 33.774016;
        minLong = -84.398498;
        maxLat = 33.773949;
        maxLong = -84.394920;
        for (int i = 0; i < 15; i++) {
            randLat = rand.nextDouble() * (maxLat - minLat) + minLat;
            randLong = rand.nextDouble() * (maxLong - minLong) + minLong;
            list.add(new LatLng(randLat, randLong));
        }

        minLat = 33.774029;
        minLong = -84.397876;
        maxLat = 33.777775;
        maxLong = -84.397811;
        for (int i = 0; i < 15; i++) {
            randLat = rand.nextDouble() * (maxLat - minLat) + minLat;
            randLong = rand.nextDouble() * (maxLong - minLong) + minLong;
            list.add(new LatLng(randLat, randLong));
        }
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list).radius(RADIUS).opacity(OPACITY).build();
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }
}
