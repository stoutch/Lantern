package com.example.deept_000.masproject;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

//import android.content.Intent;

//import android.content.Intent;


public class InitialMapActivity extends ActionBarActivity {

    GoogleMap googleMap;
    private final String ADDRESS = "http://173.236.254.243:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        HeatmapProvider provider = new HeatmapProvider();
        try {
            if (googleMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                // Check if we were successful in obtaining the map.
                if (googleMap != null) {
                    LatLng tech = new LatLng(33.775635, -84.396444);
                    googleMap.setMyLocationEnabled(true);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tech, 15));
                    provider.addHeatmap(googleMap, getLastLocation());
                    //addHeatMap();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                provider.addHeatmap(googleMap, getLastLocation());
                //addHeatMap();
            }
        }
    }

    public void inputDestination(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    public void reportConditions(View view) {
        final Dialog dialog = new Dialog(InitialMapActivity.this);
        dialog.setContentView(R.layout.report_dialog);
        TextView lighting = (TextView) dialog.findViewById(R.id.tvLight);
        lighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Reported lighting conditions");
                dialog.dismiss();
                sendLightRating();
            }
        });
        TextView police = (TextView) dialog.findViewById(R.id.tvPolicePresence);
        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Reported police");
                dialog.dismiss();
                sendPoliceRating();
            }
        });
        TextView cancel = (TextView) dialog.findViewById(R.id.tvCancelReport);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void sendLightRating() {
        Location location = getLastLocation();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        int radius = 2500;
        String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&type=lighting&value=10", ADDRESS, lat, lng);
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.execute(uri);
    }

    private void sendPoliceRating() {
        Location location = getLastLocation();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        int radius = 2500;
        String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&type=police_tower&value=10", ADDRESS, lat, lng);
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.execute(uri);
    }

    public Location getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(bestProvider);
    }

    private class HttpPostTask extends AsyncTask<String, Integer, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(String... params) {
            try {
                HttpPost httpPost = new HttpPost(params[0]);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                HttpResponse response = new DefaultHttpClient().execute(httpPost);
                String json = httpResponseToString(response);
                System.out.println("HttpPost response: " + json);
                return null;
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
        }
    }

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
}
