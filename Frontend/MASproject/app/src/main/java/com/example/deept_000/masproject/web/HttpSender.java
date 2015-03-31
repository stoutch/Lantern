package com.example.deept_000.masproject.web;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Chris on 3/31/2015.
 */
public class HttpSender {
    private WebResponseListener mCallbacks;

    public void sendHttpRequest(String uri, String data, String type, WebResponseListener callbacks) {
        mCallbacks = callbacks;
        if (mCallbacks == null) {
            return;
        }
        if (type.equals("POST")) {
            HttpPostTask httpPostTask = new HttpPostTask();
            httpPostTask.execute(uri, data);
        } else if (type.equals("GET")) {
            HttpGetTask httpGet = new HttpGetTask();
            httpGet.execute(uri);
        }
    }

    /**
     * Asynchronously carry out the HttpGet query
     */
    private class HttpGetTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Exception ex = null;
            try {
                HttpGet httpGet = new HttpGet(params[0]);
                System.out.println(params[0]);
                httpGet.setHeader("Accept", "application/json");
                httpGet.setHeader("Content-type", "application/json");
                HttpResponse response = new DefaultHttpClient().execute(httpGet);
                String strResponse = httpResponseToString(response);
                System.out.println("GET: " + strResponse);
                return strResponse;
            } catch (UnsupportedEncodingException e) {
                ex = e;
            } catch (ClientProtocolException e) {
                ex = e;
            } catch (IOException e) {
                ex = e;
            } finally {
                if (ex != null) {
                    mCallbacks.OnError(ex);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                mCallbacks.OnSuccess(response);
            }
        }
    }

    private class HttpPostTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Exception ex = null;
            try {
                HttpPost httpPost = new HttpPost(params[0]);
                StringEntity se = new StringEntity(params[1]);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");
                httpPost.setEntity(se);
                HttpResponse response = new DefaultHttpClient().execute(httpPost);
                String strResponse = httpResponseToString(response);
                System.out.println("HttpPost response: " + strResponse);
                return strResponse;
            } catch (UnsupportedEncodingException e) {
                ex = e;
            } catch (ClientProtocolException e) {
                ex = e;
            } catch (IOException e) {
                ex = e;
            } finally {
                if (ex != null) {
                    mCallbacks.OnError(ex);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                mCallbacks.OnSuccess(response);
            }
        }
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
}
