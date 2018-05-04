package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class Search extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Intent in;
    TextView tv;
    EditText searchInput;
    Button searchButton;
    String query, clientID, distance;
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url, url2;
    ArrayList<HashMap<String, String>> searchList;
    private String TAG = Search.class.getSimpleName();
    double latitude, longitude, myLat, myLong;

    // Navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            // Get the clientID
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            switch (item.getItemId()) {
                case R.id.navigation_calendar:
                    in = new Intent(Search.this, Calendar.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(Search.this, Home.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(Search.this, Account.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(Search.this, Favourites.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(Search.this, Search.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
            }
            return false;
        }
    };

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_search);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        searchList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listSearch);

        // Initialise search input box and button
        searchInput = (EditText) findViewById(R.id.searchInput);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
    }

    /**
     * On click method
     * @param v
     */
    public void onClick(View v) {
        // Grab the query and run the Search Organisations method
        query = searchInput.getText().toString();
        new Search.SearchOrganisations().execute();
        searchButton.setVisibility(View.GONE);
        searchInput.setVisibility(View.GONE);
    }

    /**
     * Searches the database for organisations matching the query
     */
    private class SearchOrganisations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Search.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/organisation/search.php?search=" + query + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get search as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray searches = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ViewOrganisation activity
                            Intent in = new Intent(Search.this, ViewOrganisation.class);

                            // Pass through the necessary values
                            for (int j = 0; j < searches.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = searches.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String organisationID = c.getString("organisation_ID");
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("clientID", clientID);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            // Start the activity
                            startActivity(in);
                        }
                    });

                    // Add the search to the array list
                    for (int i = 0; i < searches.length(); i++) {
                        JSONObject c = searches.getJSONObject(i);
                        String name = c.getString("name");
                        latitude = c.getDouble("latitude");
                        longitude = c.getDouble("longitude");
                        HashMap<String, String> search = new HashMap<>();
                        search.put("name", name);

                        // UNCOMMENT CODE TO GET THE CURRENT LOCATION OF THE DEVICE.
                        // COMMENTED OUT FOR TESTING ON EMULATOR AS IT MAKES THE LOCATION TO CALIFORNIA.

                        /*mFusedLocationProviderClient =
                                LocationServices.getFusedLocationProviderClient(Search.this);

                        try {
                            Task location = mFusedLocationProviderClient.getLastLocation();
                            location.addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: found location");
                                        Location currentLocation = (Location) task.getResult();
                                        myLat = currentLocation.getLatitude();
                                        myLong = currentLocation.getLongitude();
                                    }

                                    else {
                                        Log.d(TAG, "onComplete: current location is null");
                                        Toast.makeText(Search.this,
                                                "unable to get current location",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (SecurityException e) {
                            Log.e(TAG, "getDeviceLocation: SecurityException: " +
                                    e.getMessage());
                        }*/

                        // Test current location
                        myLat = 57.364597;
                        myLong = -2.072972;

                        // Making a request to url and getting response
                        url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + myLat + "," + myLong + "&destinations=" + latitude + "," + longitude + "&key=AIzaSyBgHeLgYUK-OF3zrmrZho-478Up8uARfa8";
                        HttpRequest sh2 = new HttpRequest();
                        String jsonStr2 = sh2.makeServiceCall(url2);
                        Log.e(TAG, "Response from url: " + jsonStr2);

                        if (jsonStr2 != null) {
                            try {
                                // Get the distance
                                JSONObject jsonObj2 = new JSONObject(jsonStr2);
                                JSONArray array = jsonObj2.getJSONArray("rows");
                                JSONObject object = array.getJSONObject(0);
                                JSONArray array2 = object.getJSONArray("elements");
                                JSONObject object2 = array2.getJSONObject(0);
                                JSONObject object3 = object2.getJSONObject("distance");
                                distance = object3.getString("text") + " away";

                                // Add the distance to the array list
                                search.put("distance", distance);
                                searchList.add(search);

                            }
                            // JSON parsing error
                            catch (final JSONException e) {
                                Log.e(TAG, "Json parsing error: " + e.getMessage());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {}
                                });
                            }
                        }
                        // Couldn't get JSON from server
                        else {
                            Log.e(TAG, "Couldn't get json from server.");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "Couldn't get json from server. Check LogCat for possible errors!",
                                            Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        }
                    }
                }
                // JSON parsing error
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {}
                    });
                }
            }
            // Couldn't get JSON from server
            else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            // Add the search array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    Search.this, searchList,
                    R.layout.list_item_search, new String[]{"name", "distance"}, new int[]{R.id.name, R.id.distance});
            lv.setAdapter(adapter);

            // Display a message if there are no results
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setVisibility(View.VISIBLE);
                tv.setText("There are no organisations with that name or type.");
            }
        }
    }
}