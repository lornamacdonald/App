package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class Favourites extends AppCompatActivity {

    // Variables
    public static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    public static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    TextView tv;
    String clientID;
    private Intent in;
    private String TAG = Favourites.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> favouritesList;

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
                    in = new Intent(Favourites.this, Calendar.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(Favourites.this, Search.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(Favourites.this, Account.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(Favourites.this, Home.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(Favourites.this, Favourites.class);
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
        setContentView(R.layout.activity_favourites);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_favourites);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        favouritesList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.listFavourites);

        // Run the Get Favourites method
        new Favourites.GetFavourites().execute();
    }

    /**
     * Display the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_search_favourites, menu);
        return true;
    }

    /**
     * On click listener for action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Open the FavouritesSearch activity
        Intent in = new Intent(Favourites.this, FavouritesSearch.class);
        in.putExtra("clientID", clientID);
        startActivity(in);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets a client's list of favourites from the database
     */
    private class GetFavourites extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Favourites.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/favourites/displayFavourites.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get favourites list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray favourites = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ViewOrganisation activity
                            Intent in = new Intent(Favourites.this, ViewOrganisation.class);
                            in.putExtra("activityName", "favourites");

                            // Pass through the clientID and organisationID
                            for (int j = 0; j < favourites.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = favourites.getJSONObject(i);
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

                    // Add the favourites to the array list
                    for (int i = 0; i < favourites.length(); i++) {
                        JSONObject c = favourites.getJSONObject(i);
                        String name = c.getString("name");
                        HashMap<String, String> favourite = new HashMap<>();
                        favourite.put("name", name);
                        favouritesList.add(favourite);
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

            // Add the favourites array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    Favourites.this, favouritesList,
                    R.layout.list_item_simple, new String[]{"name"},
                    new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no favourites
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setVisibility(View.VISIBLE);
                tv.setText("You have not favourites any organisations. Visit an organisation's page to favourite them.");
            }
        }
    }
}