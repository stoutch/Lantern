package com.example.deept_000.masproject;

import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ProcessMessageActivity extends ActionBarActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, AsyncResponse {
    GoogleMap googleMap;
    Location mLastLocation;
    List<ArrayList<LatLng>> candidates;
    GoogleApiClient mGoogleApiClient;
    String xmlString;
    ArrayList wayPoints;
    LatLng current;
    int selected_route_id;
    int selected_route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(InputActivity.EXTRA_MESSAGE);

        setContentView(R.layout.activity_process_message);
        selected_route = 0;
        setUpMapIfNeeded();

        /* Start: */
        LatLng start = new LatLng(33.777482, -84.397300);
        /* Destination: */
        LatLng dest = getLocationFromAddress(message);

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
        try {
            if (googleMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                // Check if we were successful in obtaining the map.
                if (googleMap != null) {
                    LatLng tech = new LatLng(33.775635, -84.396444);
                    googleMap.setMyLocationEnabled(true);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tech, 13));
                    addHeatMap();
                    /* Location Manager: */
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String bestProvider = locationManager.getBestProvider(criteria, true);
                    Location location = locationManager.getLastKnownLocation(bestProvider);
                    //current = new LatLng(location.getAltitude(), location.getLongitude());
                    if (location != null) {
                        onLocationChanged(location);
                    }

                    locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
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
                addHeatMap();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        //googleMap.addMarker(new MarkerOptions().position(latLng));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_process_message, menu);
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

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
            String star = URLEncoder.encode("{\"lat\":" + String.valueOf(33.781940) + ",\"lng\":" + String.valueOf(-84.376917) + "}", "UTF-8");
//            String slng = URLEncoder.encode(String.valueOf(-84.396187), "UTF-8");
            String dest = URLEncoder.encode("{\"lat\":" + String.valueOf(33.781761) + ",\"lng\":" + String.valueOf(-84.405155) + "}", "UTF-8");
//            String dlng = URLEncoder.encode(String.valueOf(-84.395426), "UTF-8");

            String routesHeatmapFromServer = "http://173.236.254.243:8080/routes?dest=" + dest + "&start=" + star;//{\"lat\":"+dlat+",\"lng\":"+dlng+"}&start={\"lat\":"+slat+",\"lng\":"+slng+"}";

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

            Log.v("InitialMapActivity", "Post requested");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            Log.v("InitialMapActivity", "Parsing requested.");
            result = EntityUtils.toString(httpEntity);//EntityUtils.toString(httpEntity);
            Log.v("InitialMapActivity", result);
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
        try {
            JSONObject top = new JSONObject(output); // outer bracket
            JSONArray routes = top.getJSONObject("response").getJSONArray("routes");

            candidates = new ArrayList<ArrayList<LatLng>>();
            for (int i = 0; i < routes.length(); ++i) {
                JSONObject curr_route_total = routes.getJSONObject(i);
                JSONArray legs = curr_route_total.getJSONArray("legs");

                candidates.add(new ArrayList<LatLng>());
                int candidates_tail = candidates.size() - 1;

                for (int j = 0; j < legs.length(); ++j) {
                    JSONObject curr_leg_total = legs.getJSONObject(j);
                    JSONObject curr_leg_start = curr_leg_total.getJSONObject("start_location");
                    JSONObject curr_leg_end = curr_leg_total.getJSONObject("end_location");

                    double start_lat = curr_leg_start.getDouble("lat");
                    double start_lng = curr_leg_start.getDouble("lng");
                    LatLng leg_start_latlng = new LatLng(start_lat, start_lng);

                    double end_lat = curr_leg_end.getDouble("lat");
                    double end_lng = curr_leg_end.getDouble("lng");
                    LatLng leg_end_latlng = new LatLng(end_lat, end_lng);

                    candidates.get(candidates_tail).add(leg_start_latlng);
                    candidates.get(candidates_tail).add(leg_end_latlng);
                }
            }
            if (output.contains("heatmap"))
                Log.i("candidate count:", "" + candidates.size());
            Log.i("route 1:", "" + candidates.get(0).size());
            Log.i("route 2:", "" + candidates.get(1).size());
            //Log.i("route 3:", ""+candidates.get(2).size());

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        InputStream stream = new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8)); // starndardcharset warning
//        DocumentBuilder builder = null;
//        try {
//            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
//        String tag[] = { "lat", "lng" };
//        Document doc = null;
//        try {
//            doc = builder.parse(stream);
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ArrayList<LatLng> list_of_geopoints = new ArrayList();
//        if (doc != null) {
//            NodeList nl1, nl2;
//            nl1 = doc.getElementsByTagName(tag[0]);
//            nl2 = doc.getElementsByTagName(tag[1]);
//            if (nl1.getLength() > 0) {
//                list_of_geopoints = new ArrayList();
//                for (int i = 0; i < nl1.getLength()-4; i++) { // start, end, bound1, bound2
//                    Node node1 = nl1.item(i);
//                    Node node2 = nl2.item(i);
//                    double lat = Double.parseDouble(node1.getTextContent());
//                    double lng = Double.parseDouble(node2.getTextContent());
//                    list_of_geopoints.add(new LatLng((double) (lat), (double) (lng)));
//                }
//            } else {
//                // No points found
//            }
//        }
//
//
//        PolylineOptions wayOptions = new PolylineOptions();
//        for(int i=0; i<list_of_geopoints.size(); i++){
//            wayOptions.add(list_of_geopoints.get(i));
//        }
//        Polyline myRoutes = googleMap.addPolyline(wayOptions);
    }


    public void startNavigation(View view) {
        Intent intent = new Intent(this, Navigation.class);
        String route_id = Integer.toString(selected_route_id);
        intent.putExtra("selected_route_id", route_id);
        ArrayList<LatLng> route = candidates.get(selected_route);
        intent.putParcelableArrayListExtra("selected_route", route);
        startActivity(intent);
    }
}
