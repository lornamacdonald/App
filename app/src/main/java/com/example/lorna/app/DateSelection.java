package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class DateSelection extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    int lengthAfterSearch;
    TextView tv;
    String clientID, month, name, staffID, staffName, organisationID, treatmentName, treatmentID;
    private String TAG = DateSelection.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    private static String url2;
    ArrayList<HashMap<String, String>> dateList;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_selection);

        // Initialise array list and list view
        dateList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listDate);

        // Run the Get Dates method
        new DateSelection.GetDates().execute();
    }

    /**
     * Display the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_search_month, menu);
        return true;
    }

    /**
     * On click listener for action bar icons
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
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.february:
                month = "2";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.march:
                month = "3";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.april:
                month = "4";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.may:
                month = "5";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.june:
                month = "6";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.july:
                month = "7";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.august:
                month = "8";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.september:
                month = "9";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.october:
                month = "10";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.november:
                month = "11";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            case R.id.december:
                month = "12";
                dateList.clear();
                lengthAfterSearch = 0;
                new DateSelection.SearchDates().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get all available dates from the database
     */
    private class GetDates extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(DateSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            /// Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                staffID = extras.getString("staffID");
                treatmentName = extras.getString("treatmentName");
                organisationID = extras.getString("organisationID");
                staffName = extras.getString("staffName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
            }

            // URLs
            url = urlBase + "/timeSlot/search.php?staff_ID=" + staffID + "&authorisation=" + urlAuth;
            url2 = urlBase + "/treatment/getTreatmentID.php?staff_ID=" + staffID + "&treatmentName=" + treatmentName + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(url2);
            Log.e(TAG, "Response from url: " + jsonStr2);

            if (jsonStr2 != null) {
                try {
                    // Get results list as JSON array
                    JSONObject jsonObj2 = new JSONObject(jsonStr2);
                    final JSONArray dates = jsonObj2.getJSONArray("records");

                    // Get treatmentID
                    for (int i = 0; i < dates.length(); i++) {
                        JSONObject c = dates.getJSONObject(i);
                        treatmentID = c.getString("treatment_ID");
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

            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get dates list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray dates = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the TimeSelection activity
                            Intent in = new Intent(DateSelection.this, TimeSelection.class);

                            // Pass through necessary values
                            for (int j = 0; j < dates.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = dates.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String staffID = c.getString("staff_ID");
                                    String dayOfWeek = c.getString("dayOfWeek");
                                    String date = c.getString("date");
                                    in.putExtra("staffID", staffID);
                                    in.putExtra("date", date);
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("treatmentName", treatmentName);
                                    in.putExtra("staffName", staffName);
                                    in.putExtra("name", name);
                                    in.putExtra("treatmentID", treatmentID);
                                    in.putExtra("clientID", clientID);
                                    in.putExtra("dayOfWeek", dayOfWeek);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            startActivity(in);
                            finish();
                        }
                    });

                    // Add the dates to the array list
                    for (int i = 0; i < dates.length(); i++) {
                        JSONObject c = dates.getJSONObject(i);
                        String theDate = c.getString("date");
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
                        String dateWithDay = dayOfWeek + ", " + theDate;

                        HashMap<String, String> date = new HashMap<>();
                        date.put("date", dateWithDay);
                        dateList.add(date);
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

            // Add the dates array to the list view
            ListAdapter adapter = new SimpleAdapter(
                    DateSelection.this, dateList,
                    R.layout.list_item_simple, new String[]{"date"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no dates
            if (dateList.size() < 1) {
                tv = (TextView)findViewById(R.id.noDates);
                tv.setVisibility(View.VISIBLE);
                tv.setText("There are no available appointments.");
            }
        }
    }

    /**
     * Queries the database for available dates in a specified month
     */
    private class SearchDates extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(DateSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            /// Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                staffID = extras.getString("staffID");
                treatmentName = extras.getString("treatmentName");
                organisationID = extras.getString("organisationID");
                staffName = extras.getString("staffName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/timeSlot/searchMonth.php?staff_ID=" + staffID + "&month=" + month + "&authorisation=" + urlAuth;
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

                    // Add the dates to the array list
                    for (int i = 0; i < dates.length(); i++) {
                        JSONObject c = dates.getJSONObject(i);
                        String theDate = c.getString("date");
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

                        String dateWithDay = dayOfWeek + ", " + theDate;
                        HashMap<String, String> date = new HashMap<>();
                        date.put("date", dateWithDay);
                        dateList.add(date);
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

            // Add the date array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    DateSelection.this, dateList,
                    R.layout.list_item_simple, new String[]{"date"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no dates
            if (lengthAfterSearch  == 0) {
                dateList.clear();
                // Hide list
                lv.setVisibility(View.GONE);
                tv = (TextView)findViewById(R.id.noDates);
                // Show message
                tv.setVisibility(View.VISIBLE);
                tv.setText("No appointments available.");
            }

            else if (lengthAfterSearch > 0) {
                // Show list
                lv.setVisibility(View.VISIBLE);
                tv = (TextView)findViewById(R.id.noDates);
                // Hide message
                tv.setVisibility(View.GONE);
            }
        }
    }
}