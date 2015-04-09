package com.example.deept_000.masproject;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deept_000.masproject.Gson.Routes;
import com.example.deept_000.masproject.Gson.Routes.Response.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class ProcessMessageActivity extends Activity implements AsyncResponse {
    GoogleMap googleMap;
    Location mLastLocation;
    List<ArrayList<LatLng>> candidates;
    String xmlString;
    ArrayList wayPoints;
    LatLng current;
    int selected_route_id;
    int selected_route;
    int best_score;
    int chosen_index;
    String selected_route_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(InputActivity.EXTRA_MESSAGE);

        setContentView(R.layout.activity_process_message);
        selected_route = 0;
        setUpMapIfNeeded();

        /* Start: */
        Location l = LocationUtil.getLastLocation();
        LatLng start = new LatLng(l.getLatitude(), l.getLongitude());//new LatLng(33.777482, -84.397300);
        /* Destination: */
        LatLng dest = getLocationFromAddress(message); // 33.772579, -84.394822

        getDirections(start.latitude, start.longitude, dest.latitude, dest.longitude);


    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            int address_size = address.size();
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();
            return new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tech, 13));
                    //addHeatMap();
                    /* Location Manager: */
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String bestProvider = locationManager.getBestProvider(criteria, true);
                    Location location = locationManager.getLastKnownLocation(bestProvider);
                    provider.addHeatmap(googleMap, location);
                    //current = new LatLng(location.getAltitude(), location.getLongitude());

                    //locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String bestProvider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(bestProvider);
                provider.addHeatmap(googleMap, location);
                //addHeatMap();
            }
        }
    }

    public void getDirections(double lat1, double lon1, double lat2, double lon2) {
        int a = 12;
        // { before after, include ", escape\" etc. see github comment
        String url = "http://maps.googleapis.com/maps/api/directions/xml?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=walking";
//        AsyncPostData getxml = new AsyncPostData();
//        getxml.execute(url);
//        getxml.delegate = this;
        //String url_json = "http://maps.googleapis.com/maps/api/directions/json?origin=" +lat1 + "," + lon1  + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=walking&alternatives=true";

        // from server:
        // String routesHeatmapFromServer = "http://173.236.254.243:8080/routes?dest={%22lat%22:33.781777,%20%22lng%22:-84.395426}&start={%22lat%22:33.777229,%22lng%22:%20-84.396187}";

        // String rfs = "http://173.236.254.243:8080/routes?dest={\"lat\":33.781761, \"lng\":-84.405155}&start={\"lat\":33.781940,\"lng\": -84.376917}";


        try {

////            String slng = URLEncoder.encode(String.valueOf(-84.396187), "UTF-8");

//            String dlng = URLEncoder.encode(String.valueOf(-84.395426), "UTF-8");


            // 33.772579, -84.394822
//            String star = URLEncoder.encode("{\"lat\":" + String.valueOf(33.781940) + ",\"lng\":" + String.valueOf(-84.376917) + "}", "UTF-8");
            String star = URLEncoder.encode("{\"lat\":" + String.valueOf(lat1) + ",\"lng\":" + String.valueOf(lon1) + "}", "UTF-8"); // coc 33.777444, -84.397250
            String dest = URLEncoder.encode("{\"lat\":" + String.valueOf(lat2) + ",\"lng\":" + String.valueOf(lon2) + "}", "UTF-8"); // tech tower
//            String dest = URLEncoder.encode("{\"lat\":" + String.valueOf(33.781761) + ",\"lng\":" + String.valueOf(-84.405155) + "}", "UTF-8");

//            String star = URLEncoder.encode("{\"lat\":" + String.valueOf(lat1) + ",\"lng\":" + String.valueOf(lon1) + "}", "UTF-8");
//            String dest = URLEncoder.encode("{\"lat\":" + String.valueOf(lat2) + ",\"lng\":" + String.valueOf(lon2) + "}", "UTF-8");

            String routesHeatmapFromServer = "http://173.236.254.243:8080/routes?dest=" + dest + "&start=" + star;//{\"lat\":"+dlat+",\"lng\":"+dlng+"}&start={\"lat\":"+slat+",\"lng\":"+slng+"}";
            System.out.println(routesHeatmapFromServer);
            AsyncPostData getJSON = new AsyncPostData();
            getJSON.execute(routesHeatmapFromServer);
            getJSON.delegate = this;


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    private class AsyncPostData extends AsyncTask<String, Void, String> { // last variable: return value of doInBackground

        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(String... params) {
            String result = getXML(params[0]);
            return result;
        }


        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }


    }

    public String getXML(String url) {

        String result = null;


        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpPost = new HttpGet(url);

            Log.v("ProcessMessageActivity", "Post requested");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            Log.v("ProcessMessageActivity", "Parsing requested.");
            result = EntityUtils.toString(httpEntity);//EntityUtils.toString(httpEntity);
            Log.v("ProcessMessageActivity", result);
        } catch (Exception ex) {
            Toast errorToast =
                    Toast.makeText(getApplicationContext(),
                            "Error reading xml", Toast.LENGTH_LONG);
            errorToast.show();
        }
        return result;

    }

    @Override
    public void processFinish(String output) {

        Log.i("in processFinish:", output);
        Gson gson = new Gson();
        Routes routes;
        try {
            routes = gson.fromJson(output, Routes.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error loading routes", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        Log.d("processFinish", routes.toString());
        TextView[] routeViews = {(TextView) findViewById(R.id.tvRoute1),
                (TextView) findViewById(R.id.tvRoute2),
                (TextView) findViewById(R.id.tvRoute3)};
        int routeNo = 0;
        int[] colors = {0x8F669900, 0x8FFF8800, 0x8FCC0000};
        for (Routes.Response.Route r : routes.response.routes) {
            routeViews[routeNo].setVisibility(View.VISIBLE);
            PolylineOptions wayOptions = new PolylineOptions();
            for (Route.Leg leg : r.legs) {
                routeViews[routeNo].setText(leg.duration.text + "\nScore: " + routes.response.score[routeNo]);
                Route.Leg.Step step;
                for (int j = 0; j < leg.steps.length - 1; j++) {
                    step = leg.steps[j];
                    wayOptions.add(new LatLng(step.start_location.lat, step.start_location.lng));
                }
                step = leg.steps[leg.steps.length - 1];
                wayOptions.add(new LatLng(step.end_location.lat, step.end_location.lng));
            }
            wayOptions.color(colors[routeNo])
                    .width(20)
                    .geodesic(true)
                    .zIndex(routeViews.length - routeNo);
            Polyline polyLine = googleMap.addPolyline(wayOptions);
            routeNo++;
        }

//        try {
//            JSONObject top = new JSONObject(output); // outer bracket
//            JSONArray routes = top.getJSONObject("response").getJSONArray("routes");
//            final JSONArray scores = top.getJSONObject("response").getJSONArray("score");
//            JSONArray route_indices = top.getJSONObject("response").getJSONArray("route_index");
//
//            candidates = new ArrayList<ArrayList<LatLng>>();
//            for (int i = 0; i < routes.length(); ++i) {
//                JSONObject curr_route_total = routes.getJSONObject(i);
//                JSONArray legs = curr_route_total.getJSONArray("legs");
//                JSONArray steps = legs.getJSONObject(0).getJSONArray("steps"); // assume no waypoints
//
//                candidates.add(new ArrayList<LatLng>());
//                int candidates_tail = candidates.size() - 1;
//
//                for (int j = 0; j < steps.length(); ++j) {
//                    JSONObject curr_step_total = steps.getJSONObject(j);
//                    JSONObject curr_step_start = curr_step_total.getJSONObject("start_location");
//                    JSONObject curr_step_end = curr_step_total.getJSONObject("end_location");
//
//                    double start_lat = curr_step_start.getDouble("lat");
//                    double start_lng = curr_step_start.getDouble("lng");
//                    LatLng leg_start_latlng = new LatLng(start_lat, start_lng);
//
//                    double end_lat = curr_step_end.getDouble("lat");
//                    double end_lng = curr_step_end.getDouble("lng");
//                    LatLng leg_end_latlng = new LatLng(end_lat, end_lng);
//
//                    candidates.get(candidates_tail).add(leg_start_latlng);
//                    candidates.get(candidates_tail).add(leg_end_latlng);
//                }
//            }
//
//            // render all routes:
//            int route_color = 0x8F000000;
//            final List<PolylineOptions> mPolylines = new ArrayList<PolylineOptions>();
//            for (int i = 0; i < candidates.size(); ++i) { // for all routes
//                PolylineOptions wayOptions = new PolylineOptions();
//                ArrayList<LatLng> curr_route = candidates.get(i);
//                for (int j = 0; j < curr_route.size(); ++j) { // each step in one route
//                    wayOptions.add(curr_route.get(j));
//                }
//                wayOptions.color(route_color)
//                        .width(20)
//                        .geodesic(true);
//                mPolylines.add(wayOptions);
//                route_color = route_color + 500;
//                Polyline myRoutes = googleMap.addPolyline(wayOptions);
//            }
//            Log.e("mPolylines:", "" + mPolylines.size());

        // Code for selecting route by clicking on it
//            selected_route = 0;
//            best_score = 0;
//            chosen_index = route_indices.getInt(0);
//            selected_route_string = route_indices.getString(0);
//            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(LatLng clickCoords) {
//                    boolean flag = true;
//                    for (PolylineOptions polyline : mPolylines) {
//
//                        for (LatLng polyCoords : polyline.getPoints()) {
//                            float[] results = new float[1];
//                            Location.distanceBetween(clickCoords.latitude, clickCoords.longitude,
//                                    polyCoords.latitude, polyCoords.longitude, results);
//
//                            if (results[0] < 100) {
//                                // If distance is less than 100 meters, this is your polyline
//                                Log.e("processFinish", "Found @ " + clickCoords.latitude + " " + clickCoords.longitude);
//                                //Log.e("processFinish", "mPolyline index:" + selected_route_id);
//                                if (flag) {
//                                    best_score = selected_route_id;
//                                    flag = false;
//                                }
//                                try {
//
//                                    if (scores.getDouble(selected_route_id) > scores.getDouble(best_score))
//                                        best_score = selected_route_id;
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//
//                            }
//                        }
//                        selected_route_id++;
//                    }
//                }
//            });

//            selected_route_id = best_score;
//            selected_route = best_score;
//            chosen_index = route_indices.getInt(best_score);

        Log.e("id pick:", "" + selected_route_id);
    }


    public void startNavigation(View view) {
        Intent intent = new Intent(this, Navigation.class);
        //String route_id = Integer.toString(chosen_index);
        Log.v("startNavigation", "PM selected route is " + selected_route_string);
        intent.putExtra("selected_route_id", selected_route_string);
        ArrayList<LatLng> route = candidates.get(selected_route);
        intent.putParcelableArrayListExtra("selected_route", route);
        startActivity(intent);
    }
}
