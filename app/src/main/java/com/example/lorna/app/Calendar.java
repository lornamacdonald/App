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

public class Calendar extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    private static String url;
    int lengthAfterSearch;
    TextView tv;
    String clientID, month, date;
    private ListView lv;
    Intent in;
    ArrayList<HashMap<String, String>> dateList;
    private String TAG = Calendar.class.getSimpleName();
    private ProgressDialog pDialog;

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
                case R.id.navigation_account:
                    in = new Intent(Calendar.this, Account.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(Calendar.this, Search.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(Calendar.this, Favourites.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(Calendar.this, Home.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_calendar:
                    in = new Intent(Calendar.this, Calendar.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
            }
            return false;
        }
    };

    /**
     * On Create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_calendar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        dateList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.listCalendar);

        // Run the Get Dates method
        new Calendar.GetDates().execute();
    }

    /**
     * Create the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_search_month, menu);
        return true;
    }

    /**
     * On click for the action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Filter dates by month
        switch (item.getItemId()) {
            case R.id.january:
                month = "1";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.february:
                month = "2";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.march:
                month = "3";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.april:
                month = "4";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.may:
                month = "5";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.june:
                month = "6";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.july:
                month = "7";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.august:
                month = "8";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.september:
                month = "9";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.october:
                month = "10";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.november:
                month = "11";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            case R.id.december:
                month = "12";
                dateList.clear();
                lengthAfterSearch = 0;
                new Calendar.SearchDates().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Gets all appointment dates from database
     */
    private class GetDates extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Calendar.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/appointment/displayCalendarDates.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get dates as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray dates = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ViewDay activity
                            Intent in = new Intent(Calendar.this, ViewDay.class);

                            // Pass through the necessary values
                            for (int j = 0; j < dates.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = dates.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String date = c.getString("date");
                                    String name = c.getString("name");
                                    String typeName = c.getString("typeName");
                                    in.putExtra("date", date);
                                    in.putExtra("typeName", typeName);
                                    in.putExtra("name", name);
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

                    // Add the dates to the array list
                    for (int i = 0; i < dates.length(); i++) {
                        JSONObject c = dates.getJSONObject(i);
                        String date = c.getString("date");
                        String dayOfWeek = c.getString("dayOfWeek");

                        // Display day of week
                        switch (dayOfWeek) {
                            case "1":
                                dayOfWeek = "Monday";
                                break;
                            case "2":
                                dayOfWeek = "Tuesday";
                                break;
                            case "3":
                                dayOfWeek = "Wednesday";
                                break;
                            case "4":
                                dayOfWeek = "Thursday";
                                break;
                            case "5":
                                dayOfWeek = "Friday";
                                break;
                            case "6":
                                dayOfWeek = "Saturday";
                                break;
                            case "7":
                                dayOfWeek = "Sunday";
                                break;
                            default:
                                break;
                        }

                        String dateWithDay = dayOfWeek + ", " + date;
                        HashMap<String, String> dateMap = new HashMap<>();
                        dateMap.put("dateWithDay", dateWithDay);
                        dateList.add(dateMap);
                    }

                }
                // JSON parsing error
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
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

            // Add the date array list to the list view
            ListAdapter adapter = new SimpleAdapter(Calendar.this, dateList,R.layout.list_item_simple,
                    new String[]{"dateWithDay"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no dates
            if (dateList.size() < 1) {
                tv = (TextView)findViewById(R.id.noAppointments);
                tv.setVisibility(View.VISIBLE);
                tv.setText("You have made no appointments. Visit an organisation's page to book with them.");
            }
        }
    }

    /**
     * Query the database for dates in a specific month
     */
    private class SearchDates extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Calendar.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/appointment/displayCalendarDatesSearch.php?client_ID=" + clientID + "&month=" + month + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get date list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray dates = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ViewDay activity
                            Intent in = new Intent(Calendar.this, ViewDay.class);

                            // Pass through the necessary values
                            for (int j = 0; j < dates.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = dates.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String date = c.getString("date");
                                    String name = c.getString("name");
                                    String typeName = c.getString("typeName");
                                    String dayOfWeek = c.getString("dayOfWeek");
                                    in.putExtra("date", date);
                                    in.putExtra("typeName", typeName);
                                    in.putExtra("name", name);
                                    in.putExtra("dayOfWeek", dayOfWeek);
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

                    // Add the dates to the array list
                    for (int i = 0; i < dates.length(); i++) {
                        JSONObject c = dates.getJSONObject(i);
                        String date = c.getString("date");
                        String dayOfWeek = c.getString("dayOfWeek");

                        // Display day of week
                        switch (dayOfWeek) {
                            case "1":
                                dayOfWeek = "Monday";
                                break;
                            case "2":
                                dayOfWeek = "Tuesday";
                                break;
                            case "3":
                                dayOfWeek = "Wednesday";
                                break;
                            case "4":
                                dayOfWeek = "Thursday";
                                break;
                            case "5":
                                dayOfWeek = "Friday";
                                break;
                            case "6":
                                dayOfWeek = "Saturday";
                                break;
                            case "7":
                                dayOfWeek = "Sunday";
                                break;
                            default:
                                break;
                        }

                        String dateWithDay = dayOfWeek + ", " + date;
                        HashMap<String, String> dateMap = new HashMap<>();
                        dateMap.put("dateWithDay", dateWithDay);
                        dateList.add(dateMap);
                        lengthAfterSearch = dateList.size();
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

            // Add the dates array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    Calendar.this, dateList,
                    R.layout.list_item_simple, new String[]{"dateWithDay"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if no dates were found
            if (lengthAfterSearch  == 0) {
                dateList.clear();
                // Hide list
                lv.setVisibility(View.GONE);
                tv = (TextView)findViewById(R.id.noAppointments);
                // Show message
                tv.setVisibility(View.VISIBLE);
                tv.setText("You have no appointments in this month.");

            }

            else if (lengthAfterSearch > 0) {
                // Show list
                lv.setVisibility(View.VISIBLE);
                tv = (TextView)findViewById(R.id.noAppointments);
                // Hide message
                tv.setVisibility(View.GONE);
            }
        }
    }
}