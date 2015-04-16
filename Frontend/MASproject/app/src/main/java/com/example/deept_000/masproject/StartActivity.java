package com.example.deept_000.masproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.deept_000.masproject.web.HttpSender;
import com.example.deept_000.masproject.web.WebResponseListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class StartActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String URI = "http://173.236.254.243:8080/login?login=%s&password=%s";
    private ProgressBar loginLoading;
    private Button loginButton;
    private GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "StartActivity";
    protected LocationRequest mLocationRequest;
    private boolean mResolvingError = false;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_start);

        loginLoading = (ProgressBar) findViewById(R.id.pbLogin);
        loginButton = (Button) findViewById(R.id.btnLogin);
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        // getting GPS status
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        } else {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void login(View view) {
        showLoading();
        String username = ((EditText) findViewById(R.id.etUsername)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        HttpSender sender = new HttpSender();
        final Intent intent = new Intent(this, InitialMapActivity.class);
        sender.sendHttpRequest(String.format(URI, username, password), "", "POST", new WebResponseListener() {
            @Override
            public void OnSuccess(String response, String... params) {
                // authentication steps to be added
                if (response.contains("\"success\": false")) {
//                    Toast toast = Toast.makeText(getApplicationContext(), "Could not log in", Toast.LENGTH_LONG);
//                    toast.show();
                    hideLoading();
                    startActivity(intent);
                } else {
                    // add shared prefs stuff
                    startActivity(intent);
                }

            }

            @Override
            public void OnError(Exception e, String... params) {
                hideLoading();
                Toast toast = Toast.makeText(getApplicationContext(), "Error logging in", Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void OnProcessing() {

            }
        });
    }

    private void showLoading() {
        loginButton.setVisibility(View.GONE);
        loginLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        loginButton.setVisibility(View.VISIBLE);
        loginLoading.setVisibility(View.GONE);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //startLocationUpdates();
        if (lastLocation != null) {
            LocationUtil.updateLocation(lastLocation);
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
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((StartActivity) getActivity()).onDialogDismissed();
        }
    }

    public void showSettingsAlert() {
        final Context mContext = StartActivity.this;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS needed");

        // Setting Dialog Message
        alertDialog.setMessage("GPS must be enabled to use this app. Would you like to enable it?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }
}
