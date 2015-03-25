package com.example.deept_000.masproject;

import android.location.Location;
import android.os.AsyncTask;

import com.example.deept_000.masproject.Gson.Heatmap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

    /**
     * Adds a heatmap to the map
     *
     * @param googleMap The map to which the heatmap will be added
     * @param location  The user's location
     */
    public void addHeatmap(GoogleMap googleMap, Location location) {
        mGoogleMap = googleMap;
        if (dumb) {
            addHeatMapDumb(googleMap);
            return;
        }
        getHeatmapFromServer(googleMap, location);
    }

    /**
     * Formulates the HTTP query for the server using our REST API guideline
     *
     * @param googleMap The map to which the heatmap will be added
     * @param location  The user's location
     */
    private void getHeatmapFromServer(GoogleMap googleMap, Location location) {
        // GET
        double lat = location.getLatitude();//32.725371;
        double lng = location.getLongitude();//-117.160721;
        int radius = 2500;
        String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&radius=%d", ADDRESS, lat, lng, radius);
        HttpGetTask httpGet = new HttpGetTask();
        httpGet.execute(uri);
    }

    /**
     * Asynchronously carry out the HttpGet query
     */
    private class HttpGetTask extends AsyncTask<String, Integer, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(String... params) {
            try {
                HttpGet httpGet = new HttpGet(params[0]);
                System.out.println(params[0]);
                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("Content-type", "application/json");
                return new DefaultHttpClient().execute(httpGet);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            String json = httpResponseToString(response);
            System.out.println(json);
            Gson gson = new Gson();
            Heatmap heatmap = gson.fromJson(json, Heatmap.class);
            System.out.println(heatmap.toString());
            addPointsToMapWithWeight(heatmap);
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
            System.out.println("Adding " + response.loc.coordinates[1] + ", " + response.loc.coordinates[0] + ": " + weight);
        }
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().weightedData(list).radius(RADIUS).opacity(OPACITY).build();
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    /**
     * Utility to convert an HttpResponse object's payload to a string
     *
     * @param response The HttpResponse to convert
     * @return The string representation of the payload.
     */
    private String httpResponseToString(HttpResponse response) {
        if (response == null || response.getEntity() == null)
            return null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
