package com.example.deept_000.masproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;


public class Navigation extends ActionBarActivity {

    GoogleMap googleMap;
    int selected_route;
    ArrayList<LatLng> route;
    public static String serverURL = "http://173.236.254.243:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        selected_route = Integer.parseInt(getIntent().getStringExtra("selected_route"));
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
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tech, 13));
                    provider.addHeatmap(googleMap, getLastLocation());
                    //addHeatMap();
                    displayRoute();
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
                //addHeatmap();
            }
        }
    }

    private void addHeatMap() {
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
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder().data(list).radius(15).opacity(.5).build();
        TileOverlay overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private void displayRoute() {
        PolylineOptions wayOptions = new PolylineOptions();
        for (int i = 0; i < route.size(); i++) {
            wayOptions.add(route.get(i));
        }
        Polyline myRoutes = googleMap.addPolyline(wayOptions);
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
            /*
            LinearLayout lighting = (LinearLayout) report_dialog.findViewById(R.id.lighting_layout);
            lighting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //send lighting update to server - right now as -4
                    //
                    try {
                        //Get current location
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String bestProvider = locationManager.getBestProvider(criteria, true);
                        Location location = locationManager.getLastKnownLocation(bestProvider);
                        if (location == null) {
                            //Fail - display alert saying couldn't get location
                            return;
                        }
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String location_string = "lat="+ Double.toString(latitude)+"&lng=" + Double.toString(longitude);
                        String url = serverURL+"heatmaps/negative?"+ location_string + "&type=lighting&value=4";
                        AsyncPostData post_rating = new AsyncPostData();
                        post_rating.execute(url);
                       // post_rating.delegate = this;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            */
        LinearLayout police_tower = (LinearLayout) report_dialog.findViewById(R.id.police_tower_layout);
        police_tower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send police tower update to server
                System.out.println("Clicked police tower");
            }
        });
        LinearLayout road_closure = (LinearLayout) report_dialog.findViewById(R.id.police_station_layout);
        road_closure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send road closure update to server
            }
        });

        report_dialog.show();

    }
    // This is the function called on clicking the End button

    public void endNavigation(View view) {
//        Intent intent = new Intent(this, RatingActivity.class);
//        intent.putExtra("selected_route", selected_route);
//        startActivity(intent);
        final Dialog dialog = new Dialog(Navigation.this);
        dialog.setContentView(R.layout.ratings_layout);
        // if done get the rating
        TextView done = (TextView) dialog.findViewById(R.id.tvDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RatingBar rb = (RatingBar) dialog.findViewById(R.id.ratingBar);
                int rating = (int) rb.getRating();
                System.out.println("Clicked done, rating::: " + rating);
                dialog.dismiss();
                sendRating(rating);
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
            try {
                System.out.println(readIt(response.getEntity().getContent(), 450));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public Location getLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(bestProvider);
    }
}
