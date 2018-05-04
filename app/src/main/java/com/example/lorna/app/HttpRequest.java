package com.example.lorna.app;

// Imports
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpRequest {

    // Variables
    private static final String TAG = HttpRequest.class.getSimpleName();

    // Empty constructor
    public HttpRequest() {}

    /**
     * Make service call - GET/DELETE requests
     * @param reqUrl
     * @return
     */
    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            // Make a connection
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        }
        catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    /**
     * Convert stream to string
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Make service call - POST/PUT requests
     * @param url
     */
    public void makeServiceCallPOST(String url, String[] details) {
        HttpClient hc = new DefaultHttpClient();
        String message;

        HttpPost p = new HttpPost(url);
        JSONObject object = new JSONObject();
        try {

            for (int i = 0; i < details.length * 2; i++) {
                object.put(details[i], details[i+1]);
            }
        }
        catch (Exception ex) {
        }

        try {
            message = object.toString();
            p.setEntity(new StringEntity(message, "UTF-8"));
            p.setHeader("Content-type", "application/json");
            HttpResponse resp = hc.execute(p);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
