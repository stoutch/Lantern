package com.example.deept_000.masproject.web;

/**
 * Created by Chris on 3/30/2015.
 */
public interface WebResponseListener {
    public void OnSuccess(String response, String... params);

    public void OnError(Exception e, String... params);

    public void OnProcessing();
}
