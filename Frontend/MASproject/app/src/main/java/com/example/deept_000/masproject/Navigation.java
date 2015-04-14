package com.example.deept_000.masproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class Navigation extends ActionBarActivity implements AsyncResponse {

    private final String ADDRESS = "http://173.236.254.243:8080";
    GoogleMap googleMap;
    int selected_route;
    String selected_route_string;
    ArrayList<LatLng> route;
    public static String serverURL = "http://173.236.254.243:8080/";
    private final String TAG = "Navigation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        try {
            //selected_route = Integer.parseInt(getIntent().getStringExtra("selected_route_id"));
            selected_route_string = getIntent().getStringExtra("selected_route_id");
            Log.v("Navigation.onCreate", "Selected route string is " + selected_route_string);
        } catch (Exception e) {
            Log.v("Problem here", "i");
        }
        try {
            Bundle bundle = getIntent().getExtras();
            route = bundle.getParcelableArrayList("selected_route");
            postSelectedRoute();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
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

    private void postSelectedRoute() {
        String url = serverURL + "routes/select/" + selected_route_string;
        Log.v("Url is ", url);
        AsyncPostData getJSON = new AsyncPostData();
        getJSON.execute(url);
        getJSON.delegate = this;
//        HttpGetTask httpGet = new HttpGetTask();
//        httpGet.execute(url);
        return;
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
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(route.get(0))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_marker_34dp)));
                    //addHeatMap();
                    displayRoute();
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
                provider.addHeatmap(googleMap, getLastLocation());
                //addHeatmap();
            }
        }
    }

    private void displayRoute() {
        PolylineOptions wayOptions = new PolylineOptions();
        for (int i = 0; i < route.size(); i++) {
            wayOptions.add(route.get(i));
        }
        wayOptions.color(0xFFBB77EE)
                .width(17)
                .geodesic(true)
                .zIndex(2);

        Polyline myRoutes = googleMap.addPolyline(wayOptions);
        wayOptions.color(0xFF893ec7)
                .width(25)
                .geodesic(true)
                .zIndex(1);
        Polyline myRoutes2 = googleMap.addPolyline(wayOptions);
    }

    private class AsyncPostData extends AsyncTask<String, Void, String> { // last variable: return value of doInBackground

        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            String url = params[0];
            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpPost = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);
                Log.v("Post response is ", result);
            } catch (Exception ex) {
                Toast errorToast =
                        Toast.makeText(getApplicationContext(),
                                "Error reading xml", Toast.LENGTH_LONG);
                errorToast.show();
            }
            System.out.println(result);
            return result;
        }


        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }

    public void reportButton(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = Navigation.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.report_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // send to server
                    }
                })

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog report_dialog = builder.create();

        report_dialog.show();

    }
    // This is the function called on clicking the End button

    public void reportConditions(View view) {
        final Dialog dialog = new Dialog(Navigation.this);
        dialog.setContentView(R.layout.report_dialog);
        TextView lighting = (TextView) dialog.findViewById(R.id.tvLight);
        lighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Reported lighting conditions");
                sendLightRating(dialog);
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

    private void sendLightRating(final Dialog dialog) {
        Button send = (Button) dialog.findViewById(R.id.btnSend);
        TextView police = (TextView) dialog.findViewById(R.id.tvPolicePresence);
        final RatingBar rbLight = (RatingBar) dialog.findViewById(R.id.rbLight);
        rbLight.setVisibility(View.VISIBLE);
        police.setVisibility(View.GONE);
        send.setVisibility(View.VISIBLE);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stars = (int) rbLight.getRating();
                Location location = LocationUtil.getLastLocation();//getLastLocation();
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                int rating = getRatingFromStars(stars);
                Log.d(TAG, "Light rating: " + rating);
                dialog.dismiss();
                String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&type=lighting&value=%d", ADDRESS, lat, lng, rating);
                HttpPostTask httpPostTask = new HttpPostTask();
                httpPostTask.execute(uri);
            }
        });

    }

    private int getRatingFromStars(int stars) {
        int rating = 0;
        switch (stars) {
            case 0:
                rating = -10;
                break;
            case 1:
                rating = -5;
                break;
            case 2:
                rating = 0;
                break;
            case 3:
                rating = 3;
                break;
            case 4:
                rating = 5;
                break;
            case 5:
                rating = 10;
                break;
        }
        return rating;
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

    public void endNavigation(View view) {
        final Dialog dialog = new Dialog(Navigation.this);
        dialog.setContentView(R.layout.ratings_layout);
        // if done get the rating
        TextView done = (TextView) dialog.findViewById(R.id.tvDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar rb = (RatingBar) dialog.findViewById(R.id.ratingBar);
                int rating = (int) rb.getRating();
                System.out.println("Clicked done, rating: " + rating);
                dialog.dismiss();
                sendRating(rating);
                Intent intent = new Intent(getApplicationContext(), InitialMapActivity.class);
                startActivity(intent);
            }
        });

        // If canceled, just exit out
        TextView cancel = (TextView) dialog.findViewById(R.id.tvCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void sendRating(int rating) {
        //Filler material for now
        HttpGetTask httpGet = new HttpGetTask();
        httpGet.execute("http://173.236.254.243:8080/heatmaps/positive?lat=32.725371&lng=%20-117.160721&radius=2500&total=2");
    }

    private class HttpGetTask extends AsyncTask<String, Integer, HttpResponse> {

        @Override
        protected HttpResponse doInBackground(String... params) {
            try {
                HttpGet httpGet = new HttpGet(params[0]);
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
            String result = httpResponseToString(response);
            Log.v("onPostExecute", "Post result: " + result);
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

    public Location getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(bestProvider);
    }

    public void processFinish(String output) {

        Log.i("in processFinish:", output);
        try {
            JSONObject top = new JSONObject(output); // outer bracket
            //JSONObject status_obj = top.getJSONObject(0);
            String status = top.getString("success");
            System.out.println("Post result is " + status);
            Log.v("Post result is ", status);
            if (status == "true") {
                System.out.println("Success in selecting the route");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
