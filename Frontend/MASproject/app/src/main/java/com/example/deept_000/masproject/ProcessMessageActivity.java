package com.example.deept_000.masproject;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deept_000.masproject.Gson.Routes;
import com.example.deept_000.masproject.Gson.Routes.Response.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class ProcessMessageActivity extends ActionBarActivity implements AsyncResponse {
    GoogleMap googleMap;
    Location mLastLocation;
    ArrayList<ArrayList<LatLng>> candidates;
    Routes mRoutes;
    String xmlString;
    ArrayList wayPoints;
    LatLng current;
    int selected_route_id;
    int selected_route;
    int best_score;
    int chosen_index;
    String selected_route_string;
    private LatLng mDest;
    private LatLng mStart;
    private ArrayList<Polyline> mPolyLines;
    private Polyline[] mSelectedPolylines;
    private final String TAG = "ProcessMessageActivity";
    private ArrayList<String> mDurations;
    private ArrayList<String> mSafetyScores;
//    private ArrayList<Polyline> mPol

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.routesToolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_process_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(InputActivity.EXTRA_MESSAGE);
        /* Destination: */
        mDest = LocationUtil.getLocationFromAddress(message, this); // 33.772579, -84.394822
        selected_route = 0;
        setUpMapIfNeeded();

        /* Start: */
        Location l = LocationUtil.getLastLocation();
        mStart = new LatLng(l.getLatitude(), l.getLongitude());//new LatLng(33.777482, -84.397300);

        getDirections(mStart.latitude, mStart.longitude, mDest.latitude, mDest.longitude);
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
                    googleMap.setMyLocationEnabled(true);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDest, 15));
                    /* Location Manager: */
                    provider.addHeatmap(googleMap, LocationUtil.getLastLocation());
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(mDest)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_marker_34dp)));
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
                provider.addHeatmap(googleMap, LocationUtil.getLastLocation());
            }
        }
    }

    public void getDirections(double lat1, double lon1, double lat2, double lon2) {
        int a = 12;
        // { before after, include ", escape\" etc. see github comment
        String url = "http://maps.googleapis.com/maps/api/directions/xml?origin=" + lat1 + "," + lon1 + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=walking";
        try {
            String star = URLEncoder.encode("{\"lat\":" + String.valueOf(lat1) + ",\"lng\":" + String.valueOf(lon1) + "}", "UTF-8"); // coc 33.777444, -84.397250
            String dest = URLEncoder.encode("{\"lat\":" + String.valueOf(lat2) + ",\"lng\":" + String.valueOf(lon2) + "}", "UTF-8"); // tech tower

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
        Log.d(TAG, output);
        ProgressBar pbRoute = (ProgressBar) findViewById(R.id.pbRoute);
        pbRoute.setVisibility(View.GONE);
        Log.i("in processFinish:", output);
        Gson gson = new Gson();
        try {
            mRoutes = gson.fromJson(output, Routes.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Error loading routes", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        Log.d("processFinish", mRoutes.toString());
        TextView[] routeViews = {};//(TextView) findViewById(R.id.tvRoute1), (TextView) findViewById(R.id.tvRoute2), (TextView) findViewById(R.id.tvRoute3)
        int routeNo = 0;
        mPolyLines = new ArrayList<Polyline>();
        candidates = new ArrayList<ArrayList<LatLng>>();
        mSelectedPolylines = new Polyline[2];
        mDurations = new ArrayList<String>();
        mSafetyScores = new ArrayList<String>();
        int bestScore = Integer.MIN_VALUE;

        for (Routes.Response.Route r : mRoutes.response.routes) {
            ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
            PolylineOptions wayOptions = new PolylineOptions();
            int tempScore = Integer.MIN_VALUE;
            for (Route.Leg leg : r.legs) {
                mDurations.add(leg.duration.text);
                tempScore = (int) (mRoutes.response.score[routeNo] * 100);
                mSafetyScores.add(Integer.toString(tempScore));
                Route.Leg.Step step;
                for (int j = 0; j < leg.steps.length - 1; j++) {
                    step = leg.steps[j];
                    wayOptions.add(new LatLng(step.start_location.lat, step.start_location.lng));
                    latLngList.add(new LatLng(step.start_location.lat, step.start_location.lng));
                }
                step = leg.steps[leg.steps.length - 1];
                wayOptions.add(new LatLng(step.end_location.lat, step.end_location.lng));
                latLngList.add(new LatLng(step.end_location.lat, step.end_location.lng));
            }
            // add the polylines to the map
            candidates.add(latLngList);
            wayOptions.color(getResources().getColor(R.color.route_grey_main))
                    .width(17)
                    .zIndex(2 * (3 - routeNo));
            Polyline main = googleMap.addPolyline(wayOptions);
            wayOptions.color(getResources().getColor(R.color.route_grey_border))
                    .width(25)
                    .zIndex(2 * (3 - routeNo) - 1);
            Polyline border = googleMap.addPolyline(wayOptions);

            mPolyLines.add(main);
            mPolyLines.add(border);
            Log.d(TAG, "temp score: " + tempScore);
            if (tempScore > bestScore) {
                Log.d(TAG, "best score: " + bestScore);
                if (routeNo == 0) {
                    main.setColor(getResources().getColor(R.color.bright_purple));
                    border.setColor(getResources().getColor(R.color.purple));
                    mSelectedPolylines[0] = main;
                    mSelectedPolylines[1] = border;
                    setRouteInfoText();
                } else {
                    switchRoutes(routeNo);
                }
                bestScore = tempScore;
            }
            routeNo++;
        }

        LatLngBounds route = new LatLngBounds.Builder()
                .include(mDest)
                .include(mStart)
                .build();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(route, 300));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick");
                for (ArrayList<LatLng> polyline : candidates) {
                    if (PolyUtil.isLocationOnPath(latLng, polyline, false, 50)) {
                        Log.d(TAG, "location is on path: " + candidates.indexOf(polyline));
                        int index = candidates.indexOf(polyline);
                        switchRoutes(index);
                    }
                }
            }
        });
    }

    public void switchRoutes(int index) {
        Polyline main = mPolyLines.get(2 * index);
        Polyline border = mPolyLines.get(2 * index + 1);

        main.setColor(getResources().getColor(R.color.bright_purple));
        border.setColor(getResources().getColor(R.color.purple));
        float mainZIndex, borderZIndex;
        mSelectedPolylines[0].setColor(getResources().getColor(R.color.route_grey_main));
        mainZIndex = mSelectedPolylines[0].getZIndex();
        mSelectedPolylines[0].setZIndex(main.getZIndex());
        main.setZIndex(mainZIndex);
        mSelectedPolylines[0] = main;

        mSelectedPolylines[1].setColor(getResources().getColor(R.color.route_grey_border));
        borderZIndex = mSelectedPolylines[1].getZIndex();
        mSelectedPolylines[1].setZIndex(border.getZIndex());
        border.setZIndex(borderZIndex);
        mSelectedPolylines[1] = border;

        selected_route = index;
        setRouteInfoText();
    }

    public void setRouteInfoText() {
        TextView duration = (TextView) findViewById(R.id.tvDuration);
        TextView safety = (TextView) findViewById(R.id.tvSafetyScore);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.selectRouteInfo);

        duration.setText(mDurations.get(selected_route));
        safety.setText(mSafetyScores.get(selected_route));
        layout.setVisibility(View.VISIBLE);
    }

    public void startNavigation(View view) {
        Intent intent = new Intent(this, Navigation.class);
        String routeId = Long.toString(mRoutes.response.route_index[selected_route]);
        intent.putExtra("selected_route_id", routeId);

        ArrayList<LatLng> route = candidates.get(selected_route);
        intent.putParcelableArrayListExtra("selected_route", route);
        startActivity(intent);
    }
}
