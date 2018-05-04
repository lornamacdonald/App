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
import java.util.Date;
import java.util.HashMap;

public class TimeSelection extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String dayOfWeek, date, clientID, name, staffID, staffName, organisationID, treatmentName, treatmentID;
    private String TAG = TimeSelection.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> timeList;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_selection);

        // Initialise array list and list view
        timeList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listTime);

        // Run the Get Times method
        new TimeSelection.GetTimes().execute();
    }

    /**
     * Gets a list of available times from the database
     */
    private class GetTimes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(TimeSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                staffID = extras.getString("staffID");
                date = extras.getString("date");
                treatmentName = extras.getString("treatmentName");
                organisationID = extras.getString("organisationID");
                staffName = extras.getString("staffName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
                treatmentID = extras.getString("treatmentID");
                dayOfWeek = extras.getString("dayOfWeek");
            }

            // URL
            url = urlBase + "/timeSlot/displayFreeSlots.php?date=" + date + "&staff_ID=" + staffID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null) {
                        try {
                            // Get times as JSON array
                            JSONObject jsonObj = new JSONObject(jsonStr);
                            final JSONArray dates = jsonObj.getJSONArray("records");

                            // On click listener for list items
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    // Open the ReviewBooking activity
                                    Intent in = new Intent(TimeSelection.this, ReviewBooking.class);

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
                                            String startTime = c.getString("startTime");
                                            String endTime = c.getString("endTime");
                                            in.putExtra("staffID", staffID);
                                            in.putExtra("treatmentName", treatmentName);
                                            in.putExtra("organisationID", organisationID);
                                            in.putExtra("date", date);
                                            in.putExtra("startTime", startTime);
                                            in.putExtra("endTime", endTime);
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
                                    // Start the activity
                                    startActivity(in);
                                    // Block back button
                                    finish();
                                }
                            });

                            // Add the times to the array list
                            for (int i = 0; i < dates.length(); i++) {
                                JSONObject c = dates.getJSONObject(i);
                                String startTime = c.getString("startTime");
                                String endTime = c.getString("endTime");
                                String timeSlot = startTime + " - " + endTime;
                                HashMap<String, String> date = new HashMap<>();
                                date.put("timeSlot", timeSlot);
                                timeList.add(date);
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

            // Add the time array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    TimeSelection.this, timeList,
                    R.layout.list_item_simple, new String[]{"timeSlot"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no times
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setText("There are no available appointments.");
            }

            // Display a message if the appointment date is today or before today (meaning it's happened)
            int year = Integer.parseInt(date.substring(0,4));
            int month = Integer.parseInt(date.substring(5,7));
            int day = Integer.parseInt(date.substring(8,10));

            Date formattedDate = new Date((year - 1900), (month - 1), day);
            Date today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);

            if (formattedDate.toString().equals(today.toString())) {
                lv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                tv.setText("If you would like to make an appointment for today, please contact " + name + " directly.");
            }
        }
    }
}