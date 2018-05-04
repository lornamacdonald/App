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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ViewAppointment extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String appointmentID, organisationID, date, startTime, name, timeSlotID, clientID;
    private Intent in;
    private String TAG = ViewAppointment.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url, urlNotBooked;
    ArrayList<HashMap<String, String>> appointmentList;
    Button cancelAppt;

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
                    in = new Intent(ViewAppointment.this, Calendar.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(ViewAppointment.this, Search.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(ViewAppointment.this, Account.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(ViewAppointment.this, Favourites.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(ViewAppointment.this, Home.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
            }
            return false;
        }
    };

    /**
     * On cr
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_calendar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        appointmentList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.list3);

        // Run the Get Appointment method
        new ViewAppointment.GetAppointment().execute();

        // Initialise cancel button and add on click listener
        cancelAppt = (Button)findViewById(R.id.cancelApptButton);
        cancelAppt.setOnClickListener(this);

        // Display a message if the appointment date is today or before today (meaning it's happened)
        int year = Integer.parseInt(date.substring(0,4));
        int month = Integer.parseInt(date.substring(5,7));
        int day = Integer.parseInt(date.substring(8,10));

        Date formattedDate = new Date((year - 1900), (month - 1), day);
        Date today = new Date();

        if (formattedDate.before(today) || formattedDate.equals(today)) {
            cancelAppt.setVisibility(View.GONE);
        }
    }

    /**
     * On click method
     * @param v
     */
    public void onClick(View v) {

        // Get the clientID
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clientID = extras.getString("clientID");
        }

        // Create an alert
        AlertDialog alertDialog = new AlertDialog.Builder(ViewAppointment.this).create();
        alertDialog.setTitle("Cancel Appointment");
        alertDialog.setMessage("Are you sure you want to cancel this appointment?");

        // Yes button - delete appointment
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // Create an alert
                        AlertDialog alertDialog2 = new AlertDialog.Builder(ViewAppointment.this).create();
                        alertDialog2.setTitle("Appointment Deleted");
                        alertDialog2.setButton(AlertDialog.BUTTON_NEUTRAL, "Continue",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent in = new Intent(ViewAppointment.this, Home.class);
                                        in.putExtra("clientID", clientID);
                                        new ViewAppointment.GetAppointment().execute();
                                        new ViewAppointment.DeleteAppointment().execute();
                                        startActivity(in);
                                        finish();
                                    }
                                });
                        alertDialog2.show();

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
    }

    /**
     * Display action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_info, menu);
        return true;
    }

    /**
     * On click listener for action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent in = new Intent(ViewAppointment.this, ViewOrganisation.class);
        in.putExtra("clientID", clientID);
        in.putExtra("organisationID", organisationID);
        in.putExtra("appointmentID", appointmentID);
        startActivity(in);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes an appointment from the database
     */
    private class DeleteAppointment extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewAppointment.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                appointmentID = extras.getString("appointmentID");
                date = extras.getString("date");
                startTime = extras.getString("startTime");
                clientID = extras.getString("clientID");
            }

            // URLs
            url = urlBase + "/appointment/delete.php?appointment_ID=" + appointmentID + "&authorisation=" + urlAuth;
            urlNotBooked = urlBase + "/timeSlot/notBooked.php?timeSlot_ID=" + timeSlotID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Delete appointment
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            // Free time slot up
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(urlNotBooked);
            Log.e(TAG, "Response from url: " + jsonStr2);

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

    /**
     * Get details of an appointment from the database
     */
    private class GetAppointment extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewAppointment.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                appointmentID = extras.getString("appointmentID");
                date = extras.getString("date");
                startTime = extras.getString("startTime");
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/appointment/read_one.php?appointment_ID=" + appointmentID + "&authorisation=" + urlAuth;
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
                    final JSONArray appointments = jsonObj.getJSONArray("records");

                    // Add details to the array list
                    for (int i = 0; i < appointments.length(); i++) {
                        JSONObject c = appointments.getJSONObject(i);
                        String appointment_ID = c.getString("appointment_ID");
                        date = c.getString("date");
                        String startTime = c.getString("startTime");
                        String endTime = c.getString("endTime");
                        name = c.getString("name");
                        String treatmentName = c.getString("treatmentName");
                        String title = c.getString("title");
                        String firstName = c.getString("firstName");
                        String lastName = c.getString("lastName");
                        String when = date + ", " + startTime + " - " + endTime;
                        String who = title + " " + firstName + " " + lastName;
                        organisationID = c.getString("organisation_ID");
                        String dayOfWeek = c.getString("dayOfWeek");
                        timeSlotID = c.getString("timeSlot_ID");

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

                        String dateWithDay = dayOfWeek + ", " + when;
                        HashMap<String, String> appointment = new HashMap<>();
                        appointment.put("name", name);
                        appointment.put("treatmentName", treatmentName);
                        appointment.put("who", who);
                        appointment.put("when", dateWithDay);
                        appointment.put("appointment_ID", appointment_ID);
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

            // Add the details array to the list view
            ListAdapter adapter = new SimpleAdapter(
                    ViewAppointment.this, appointmentList,
                    R.layout.list_item_full_appointment, new String[]{"when", "name", "treatmentName",
                    "who"},
                    new int[]{R.id.when, R.id.where, R.id.what,
                            R.id.who});
            lv.setAdapter(adapter);
        }
    }
}