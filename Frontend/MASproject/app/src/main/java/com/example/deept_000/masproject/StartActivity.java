package com.example.deept_000.masproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.deept_000.masproject.web.HttpSender;
import com.example.deept_000.masproject.web.WebResponseListener;


public class StartActivity extends ActionBarActivity {
    private final String URI = "http://173.236.254.243:8080/login?login=%s&password=%s";
    private ProgressBar loginLoading;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        loginLoading = (ProgressBar) findViewById(R.id.pbLogin);
        loginButton = (Button) findViewById(R.id.btnLogin);
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
                    Toast toast = Toast.makeText(getApplicationContext(), "Could not log in", Toast.LENGTH_LONG);
                    toast.show();
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
}
