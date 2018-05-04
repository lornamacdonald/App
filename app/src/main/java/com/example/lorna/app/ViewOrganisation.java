package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewOrganisation extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String organisationID, name, clientID, value;
    private Intent in;
    private String TAG = ViewAppointment.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> organisationList;
    Button book;
    private static String url;
    double longitude, latitude;
    private Menu menu;

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
                    in = new Intent(ViewOrganisation.this, Calendar.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(ViewOrganisation.this, Search.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(ViewOrganisation.this, Account.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(ViewOrganisation.this, Favourites.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(ViewOrganisation.this, Home.class);
                    in.putExtra("clientID",clientID);
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
        setContentView(R.layout.activity_view_organisation);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_search);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        organisationList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.list2);

        // Run the Get Organisation method
        new ViewOrganisation.GetOrganisation().execute();

        // Add on click listener to book button
        book = (Button)findViewById(R.id.bookButton);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(ViewOrganisation.this, TreatmentSelection.class);
                in.putExtra("organisationID", organisationID);
                in.putExtra("clientID", clientID);
                startActivity(in);
            }
        });
    }

    /**
     * Display the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Display initial state of heart
        new ViewOrganisation.IsFavouritedInitial().execute();
        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_organisation, menu);
        return true;
    }

    /**
     * On click listener for action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Location icon
        if (id == R.id.locationActionBar) {
            Intent in = new Intent(ViewOrganisation.this, Map.class);
            in.putExtra("longitude", longitude);
            in.putExtra("latitude", latitude);
            in.putExtra("name", name);
            in.putExtra("clientID", clientID);
            startActivity(in);
        }
        // Favourite icon
        else if ((id == R.id.emptyHeart) || (id == R.id.solidHeart)) {
            new ViewOrganisation.IsFavourited().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets an organisation from the database
     */
    private class GetOrganisation extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewOrganisation.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                clientID = extras.getString("clientID");
                value = extras.getString("value");
            }

            // URL
            url = urlBase + "/organisation/read_one.php?organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get details as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray organisations = jsonObj.getJSONArray("records");

                    // Add the details to the array list
                    for (int i = 0; i < organisations.length(); i++) {
                        JSONObject c = organisations.getJSONObject(i);
                        organisationID = c.getString("organisation_ID");
                        String typeName = c.getString("typeName");
                        String address = c.getString("address");
                        String town = c.getString("town");
                        String postcode = c.getString("postcode");
                        String description = c.getString("description");
                        String email = c.getString("email");
                        String phone = c.getString("phone");
                        String fullAddress = address + ", " + town + ", " + postcode;
                        String openingTimes = c.getString("openingTimes");
                        name = c.getString("name");
                        latitude = c.getDouble("latitude");
                        longitude = c.getDouble("longitude");

                        HashMap<String, String> organisation = new HashMap<>();
                        organisation.put("organisation_ID", organisationID);
                        organisation.put("typeName", typeName);
                        organisation.put("fullAddress", fullAddress);
                        organisation.put("description", description);
                        organisation.put("email", email);
                        organisation.put("phone", phone);
                        organisation.put("openingTimes", openingTimes);
                        organisation.put("name", name);
                        organisation.put("openingTimes", openingTimes);

                        organisationList.add(organisation);
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

            // Add the details array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    ViewOrganisation.this, organisationList,
                    R.layout.list_item_organisation, new String[]{"typeName", "fullAddress", "phone",
                    "email", "description", "openingTimes"},
                    new int[]{R.id.orgTypeName, R.id.orgAddress, R.id.orgPhone,
                            R.id.orgEmail, R.id.orgDescription, R.id.orgOpeningTimes});
            lv.setAdapter(adapter);

            // Set the action bar title
            getSupportActionBar().setTitle(name);
        }
    }

    /**
     * Check if an organisation has been favourited by the client
     */
    private class IsFavourited extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                clientID = extras.getString("clientID");
                value = extras.getString("value");
            }

            // URL
            url = urlBase + "/favourites/isFavourited.php?client_ID=" + clientID + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get response as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray response = jsonObj.getJSONArray("records");

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject c = response.getJSONObject(i);
                        value = c.getString("value");
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

            // Delete favourite if it has been favourited
            if (value.equals("1")) {
                new ViewOrganisation.DeleteFavourite().execute();
                menu.findItem(R.id.emptyHeart).setVisible(true);
                menu.findItem(R.id.solidHeart).setVisible(false);
            }

            // Favourite organisation if it has not been favourited
            else if (value.equals("0")) {
                new ViewOrganisation.FavouriteOrganisation().execute();
                menu.findItem(R.id.emptyHeart).setVisible(false);
                menu.findItem(R.id.solidHeart).setVisible(true);
            }
        }
    }

    /**
     * Initial check for if an organisation has been favourited by the client
     */
    private class IsFavouritedInitial extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                clientID = extras.getString("clientID");
                value = extras.getString("value");
            }

            // URL
            url = urlBase + "/favourites/isFavourited.php?client_ID=" + clientID + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get response as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray response = jsonObj.getJSONArray("records");

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject c = response.getJSONObject(i);
                        value = c.getString("value");
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

            // Organisation has been favourited
            if (value.equals("1")) {

                menu.findItem(R.id.emptyHeart).setVisible(false);
                //emptyHeart.setVisible(true);
                //solidHeart.setVisible(false);
            }

            // Organisation has not been favourited
            else if (value.equals("0")) {
                menu.findItem(R.id.solidHeart).setVisible(false);
            }
        }
    }

    /**
     * Delete a favourite
     */
    private class DeleteFavourite extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewOrganisation.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            // URL
            url = urlBase + "/favourites/delete.php?client_ID=" + clientID + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }

    /**
     * Favourite an organisation
     */
    private class FavouriteOrganisation extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewOrganisation.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/favourites/create.php";
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Details for the POST request
            String[] details = {"organisation_ID", organisationID, "client_ID", clientID,
                    "authorisation", urlAuth};

            // Making a request to url and getting response
            HttpRequest hr = new HttpRequest();
            hr.makeServiceCallPOST(url, details);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}