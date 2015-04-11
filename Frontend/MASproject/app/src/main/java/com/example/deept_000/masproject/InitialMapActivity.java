package com.example.deept_000.masproject;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.deept_000.masproject.service.SendLocationReceiver;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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


public class InitialMapActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    GoogleMap googleMap;
    private final String ADDRESS = "http://173.236.254.243:8080";
    private GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "InitialMapActivity";
    protected LocationRequest mLocationRequest;
    public final static String EXTRA_MESSAGE = "com.example.deept_000.MESSAGE";
    private String mDestination;
    private Marker mCurrentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_map);

        buildGoogleApiClient();
        //setUpMapIfNeeded();

        SendLocationReceiver alarm = new SendLocationReceiver();
        //GetLocationReceiver alarm = new GetLocationReceiver();
        alarm.setAlarm(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        System.out.println("InitialMapActivity.handleIntent");
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            System.out.println("doing Action Search");
            // handles a search query
            mDestination = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(mDestination, null);

            FloatingActionButton report = (FloatingActionButton) findViewById(R.id.fabReport);
            FloatingActionButton panic = (FloatingActionButton) findViewById(R.id.fabPanic);
            FloatingActionButton nav = (FloatingActionButton) findViewById(R.id.fabNavigate);
            report.setVisibility(View.GONE);
            panic.setVisibility(View.GONE);
            nav.setVisibility(View.VISIBLE);

            LatLng loc = LocationUtil.getLocationFromAddress(mDestination, this);
            if (mCurrentMarker != null) {
                mCurrentMarker.remove();
            }

            mCurrentMarker = googleMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_marker_34dp)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        LocationUtil.updateLocation(location);
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        Log.d(TAG, "startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
                    provider.addHeatmap(googleMap, LocationUtil.getLastLocation());//getLastLocation());
                    //addHeatMap();
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
                provider.addHeatmap(googleMap, LocationUtil.getLastLocation());//getLastLocation());
                //addHeatMap();
            }
        }
    }

    public void inputDestination(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }

    public void showRoutes(View view) {
        Intent intent = new Intent(this, ProcessMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, mDestination);
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
        Location location = LocationUtil.getLastLocation();//getLastLocation();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String uri = String.format("%s/heatmaps/positive?lat=%f&lng=%f&type=police_tower&value=10", ADDRESS, lat, lng);
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.execute(uri);
    }

//    public Location getLastLocation() {
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String bestProvider = locationManager.getBestProvider(criteria, true);
//        return locationManager.getLastKnownLocation(bestProvider);
//    }

    @Override
    public void onConnected(Bundle bundle) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
        if (lastLocation != null) {
            Log.d(TAG, "Setting last location");
            LocationUtil.updateLocation(lastLocation);
            setUpMapIfNeeded();
        } else {
            Log.d(TAG, "Could not get last location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
