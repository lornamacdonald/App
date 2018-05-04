package com.example.lorna.app;

// Imports
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Home extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    TextView tv;
    String clientID, firstName, date, name, startTime;
    private Intent in;
    private String TAG = Home.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    int apptCount;
    private static String url, url2;
    ArrayList<HashMap<String, String>> appointmentList;
    private NotificationHelper nh;
    DatabaseHelper db;

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
                    in = new Intent(Home.this, Calendar.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(Home.this, Search.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(Home.this, Account.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(Home.this, Favourites.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(Home.this, Home.class);
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
        setContentView(R.layout.activity_home);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        appointmentList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        // Run the Get Appointments method
        new Home.GetAppointments().execute();

        // Initialise Database Helper
        db = new DatabaseHelper(this);

        // Initialise Notification Helper
        nh = new NotificationHelper(this);
    }

    public void sendNotification(String title, String message) {
        NotificationCompat.Builder nb = nh.getChannelNotification(title, message);
        nh.getManager().notify(1, nb.build());
    }

    /**
     * Display the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_home, menu);
        return true;
    }

    // On click listener for the action bar icons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Create a dialog
        AlertDialog alertDialog = new AlertDialog.Builder(Home.this).create();
        alertDialog.setTitle("Sign Out");
        alertDialog.setMessage("Are you sure you want to sign out?");

        // Yes button - sign out
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteData(clientID);
                        dialog.dismiss();
                        Intent in = new Intent(Home.this, SignIn.class);
                        startActivity(in);
                        finish();
                    }
                });

        // No button - cancel
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        return super.onOptionsItemSelected(item);
    }


    /**
     * Gets client's upcoming appointments from database
     */
    private class GetAppointments extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Home.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URLs
            url = urlBase + "/appointment/displayUpcomingAppointments.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
            url2 = urlBase + "/client/displayFirstName.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(url2);
            Log.e(TAG, "Response from url: " + jsonStr2);

            if (jsonStr2 != null) {
                try {
                    // Get results as JSON array
                    JSONObject jsonObj2 = new JSONObject(jsonStr2);
                    final JSONArray names = jsonObj2.getJSONArray("records");

                    // Add the first name to the array list
                    for (int i = 0; i < names.length(); i++) {
                        JSONObject c = names.getJSONObject(i);
                        firstName = c.getString("firstName");
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

            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get appointments list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray appointments = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i,
                                                long l) {
                            // Open the ViewAppointment activity
                            Intent in = new Intent(Home.this, ViewAppointment.class);

                            // Pass through necessary values
                            for (int j = 0; j < appointments.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = appointments.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String appointmentID = c.getString("appointment_ID");
                                    String date = c.getString("date");
                                    String startTime = c.getString("startTime");
                                    String value = c.getString("value");
                                    in.putExtra("appointmentID", appointmentID);
                                    in.putExtra("date", date);
                                    in.putExtra("startTime", startTime);
                                    in.putExtra("value", value);
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
                        String date = c.getString("date");
                        String name = c.getString("name");
                        String typeName = c.getString("typeName");
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
                        HashMap<String, String> appointment = new HashMap<>();
                        appointment.put("name", name);
                        appointment.put("typeName", typeName + " Appointment");
                        appointment.put("dateWithDay", dateWithDay);
                        appointmentList.add(appointment);
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

            // Add the appointments array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    Home.this, appointmentList,
                    R.layout.list_item_simple_appointment, new String[]{"name", "dateWithDay",
                    "typeName"}, new int[]{R.id.name,
                    R.id.timeSlot, R.id.typeName});
            lv.setAdapter(adapter);

            // Display a message if there are no appointments
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.upcomingAppointments);
                tv.setText("You have no upcoming appointments. Visit an organisation's page to book with them.");
            }

            // Display the user's name on the action bar
            getSupportActionBar().setTitle("Welcome, " + firstName);

            // Display notifications
            new Home.Notification().execute();
        }
    }

    /**
     * Display a notification when the next appointment is tomorrow
     */
    private class Notification extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(Home.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID to URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/appointment/nextAppointment.php?client_ID=" + clientID +
                    "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr2);

            if (jsonStr2 != null) {
                try {
                    // Get appointment details as JSON array
                    JSONObject jsonObj2 = new JSONObject(jsonStr2);
                    final JSONArray details = jsonObj2.getJSONArray("records");

                    // Add the details to the array list
                    for (int i = 0; i < details.length(); i++) {
                        JSONObject c = details.getJSONObject(i);
                        date = c.getString("date");
                        name = c.getString("name");
                        startTime = c.getString("startTime");
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

            // If the appointment is tomorrow, display the notification
            Date todayDate = new Date();
            Date tomorrowDate = new Date(todayDate.getTime() + (24 * 60 * 60 * 1000));
            String tomorrow = new SimpleDateFormat("yyyy-MM-dd").format(tomorrowDate);

            /*if (date.equals(tomorrow)) {
                nh = new NotificationHelper(Home.this);
                sendNotification("You have an appointment tomorrow.", name + ": " +
                        startTime);
            }*/
        }
    }
}