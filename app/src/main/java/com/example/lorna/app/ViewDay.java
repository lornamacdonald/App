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

public class ViewDay extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    TextView tv;
    String clientID, date, dayOfWeek, appointmentID, startTime;
    Intent in;
    private String TAG = ViewDay.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> appointmentList;

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
                    in = new Intent(ViewDay.this, Calendar.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(ViewDay.this, Search.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(ViewDay.this, Account.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(ViewDay.this, Favourites.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(ViewDay.this, Home.class);
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
        setContentView(R.layout.activity_view_day);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_calendar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        appointmentList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listDay);

        // Run the Get Appointments method
        new ViewDay.GetAppointments().execute();
    }

    /**
     * Gets appointments on specific date from database
     */
    private class GetAppointments extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewDay.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
                date = extras.getString("date");
                dayOfWeek = extras.getString("dayOfWeek");
            }

            // URL
            url = urlBase + "/appointment/displayAppointmentsOnDate.php?client_ID=" + clientID + "&date=" + date + "&authorisation=" + urlAuth;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get appointments as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray appointments = jsonObj.getJSONArray("records");

                    // On click listener for list item
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the ViewAppointment activity
                            Intent in = new Intent(ViewDay.this, ViewAppointment.class);

                            // Pass through the necessary values
                            for (int j = 0; j < appointments.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = appointments.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    appointmentID = c.getString("appointment_ID");
                                    date = c.getString("date");
                                    startTime = c.getString("startTime");
                                    in.putExtra("appointmentID", appointmentID);
                                    in.putExtra("date", date);
                                    in.putExtra("startTime", startTime);
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

                    // Add the appointments to the array list
                    for (int i = 0; i < appointments.length(); i++) {
                        JSONObject c = appointments.getJSONObject(i);
                        String startTime = c.getString("startTime");
                        String endTime = c.getString("endTime");
                        String name = c.getString("name");
                        String typeName = c.getString("typeName");
                        String time = startTime + " - " + endTime;
                        dayOfWeek = c.getString("dayOfWeek");

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

                        HashMap<String, String> appointment = new HashMap<>();
                        appointment.put("name", name);
                        appointment.put("typeName", typeName + " Appointment");
                        appointment.put("time", time);
                        appointmentList.add(appointment);
                    }
                }
                // JSON parsing error
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
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

            // Add the appointment array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    ViewDay.this, appointmentList,
                    R.layout.list_item_simple_appointment, new String[]{"name", "time",
                    "typeName"}, new int[]{R.id.name,
                    R.id.timeSlot, R.id.typeName});
            lv.setAdapter(adapter);

            // Set the action bar title
            String title = dayOfWeek + ", " + date;
            getSupportActionBar().setTitle(title);

            // Display a message if there are no appointments
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.upcomingAppointments);
                tv.setText("You have no upcoming appointments. Visit an organisation's page to book with them.");
            }
        }
    }
}