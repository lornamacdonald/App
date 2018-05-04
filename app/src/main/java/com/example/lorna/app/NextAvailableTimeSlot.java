package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class NextAvailableTimeSlot extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String clientID, name, staffName, staffID, organisationID, treatmentName, treatmentID;
    private String TAG = NextAvailableTimeSlot.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    TextView tv;
    private static String url, url2;
    ArrayList<HashMap<String, String>> timeList;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next_available_time_slot);

        // Initialise array list and list view
        timeList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listNextAvailable);

        // Run the Get Times method
        new NextAvailableTimeSlot.GetTimes().execute();
    }

    /**
     * Gets the next available time slot from the database
     */
    private class GetTimes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(NextAvailableTimeSlot.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                staffID = extras.getString("staffID");
                organisationID = extras.getString("organisationID");
                treatmentName = extras.getString("treatmentName");
                staffName = extras.getString("staffName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
            }

            // URLs
            url = urlBase + "/timeSlot/nextAvailable.php?staff_ID=" + staffID + "&authorisation=" + urlAuth;
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
                    final JSONArray results = jsonObj2.getJSONArray("records");

                    // Get treatmentID
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);
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
                    // Get results list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray results = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ReviewBooking activity
                            Intent in = new Intent(NextAvailableTimeSlot.this, ReviewBooking.class);

                            // Pass through necessary values
                            for (int j = 0; j < results.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = results.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String startTime = c.getString("startTime");
                                    String endTime = c.getString("endTime");
                                    String date = c.getString("date");
                                    String dayOfWeek = c.getString("dayOfWeek");
                                    in.putExtra("staffID", staffID);
                                    in.putExtra("treatmentName", treatmentName);
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("date", date);
                                    in.putExtra("startTime", startTime);
                                    in.putExtra("endTime", endTime);
                                    in.putExtra("staffName", staffName);
                                    in.putExtra("name", name);
                                    in.putExtra("clientID", clientID);
                                    in.putExtra("treatmentID", treatmentID);
                                    in.putExtra("dayOfWeek", dayOfWeek);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            // Start the activity
                            startActivity(in);

                            // Block back button
                            finish();
                        }
                    });

                    // Add the results to the array list
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject c = results.getJSONObject(i);
                        String startTime = c.getString("startTime");
                        String endTime = c.getString("endTime");
                        String date = c.getString("date");
                        String timeSlot = date + ", " + startTime + " - " + endTime;
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

                        String dateWithDay = dayOfWeek + ", " + timeSlot;
                        HashMap<String, String> time = new HashMap<>();
                        time.put("timeSlot", dateWithDay);
                        timeList.add(time);
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

            // Add the results array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    NextAvailableTimeSlot.this, timeList,
                    R.layout.list_item_simple, new String[]{"timeSlot"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no time slots
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.noAppointmentsNext);
                tv.setVisibility(View.VISIBLE);
                tv.setText("There are no available appointments.");
            }
        }
    }
}