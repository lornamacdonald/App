package com.example.lorna.app;

// Imports
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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

public class ReviewBooking extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String dayOfWeek, timeSlotID, treatmentID, activity, name, staffName, staffID, date, organisationID, treatmentName, startTime, endTime, clientID;
    private static String urlCreate, urlIsBooked, url, urlCanBook;
    private String TAG = ReviewBooking.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private Button cancel, confirm;
    ArrayList<HashMap<String, String>> reviewBookingList;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_booking);

        // Initialise array list and list view
        reviewBookingList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.list3);

        // Initialise the buttons and add on click listeners
        cancel = (Button)findViewById(R.id.cancelButton);
        confirm = (Button)findViewById(R.id.confirmButton);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);

        // Run the Get Review method
        new ReviewBooking.GetReview().execute();
    }

    /**
     * Create the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_info, menu);
        return true;
    }

    /**
     * On click for the action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Open the ViewOrganisationBooking activity
        Intent in = new Intent(ReviewBooking.this, ViewOrganisationBooking.class);
        in.putExtra("clientID", clientID);
        in.putExtra("organisationID", organisationID);
        startActivity(in);
        return super.onOptionsItemSelected(item);
    }

    /**
     * On click method
     * @param v
     */
    public void onClick(View v) {
        // If the user tapped the cancel button
        if (v.getId() == R.id.cancelButton) {
            // Create an alert
            AlertDialog alertDialog = new AlertDialog.Builder(ReviewBooking.this).create();
            alertDialog.setTitle("Cancel Booking");
            alertDialog.setMessage("Are you sure you want to cancel this booking?");

            // Yes button - cancel booking process
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent in = new Intent(ReviewBooking.this, ViewOrganisation.class);
                            in.putExtra("clientID",clientID);
                            in.putExtra("organisationID", organisationID);
                            startActivity(in);
                            finish();

                        }
                    });

            // No button - do nothing
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        // If the user tapped the confirm button
        else if (v.getId() == R.id.confirmButton) {
            // Create an alert
            AlertDialog alertDialog = new AlertDialog.Builder(ReviewBooking.this).create();
            alertDialog.setTitle("Appointment Created");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Continue",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Save the appointment
                            dialog.dismiss();
                            Intent in = new Intent(ReviewBooking.this, ViewOrganisation.class);
                            in.putExtra("clientID", clientID);
                            in.putExtra("organisationID", organisationID);
                            new ReviewBooking.CreateAppointment().execute();
                            startActivity(in);
                            finish();
                        }
                    });
            alertDialog.show();
        }
    }

    /**
     * Creates an appointment
     */
    private class CreateAppointment extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ReviewBooking.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
                treatmentID = extras.getString("treatmentID");
                organisationID = extras.getString("organisationID");
            }

            // URLs
            urlCreate = urlBase + "/appointment/create.php";
            urlIsBooked = urlBase + "/timeSlot/isBooked.php?timeSlot_ID=" + timeSlotID + "&authorisation=" + urlAuth;
            urlCanBook = urlBase + "/appointment/canBook.php?client_ID=" + clientID + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Details for the POST request
            String[] details = {"treatment_ID", treatmentID, "client_ID", clientID,
                    "timeSlot_ID", timeSlotID, "authorisation", urlAuth};

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

    /**
     * Display appointment review
     */
    private class GetReview extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ReviewBooking.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                staffID = extras.getString("staffID");
                date = extras.getString("date");
                organisationID = extras.getString("organisationID");
                treatmentName = extras.getString("treatmentName");
                startTime = extras.getString("startTime");
                endTime = extras.getString("endTime");
                staffName = extras.getString("staffName");
                name = extras.getString("name");
                treatmentID = extras.getString("treatmentID");
                clientID = extras.getString("clientID");
                dayOfWeek = extras.getString("dayOfWeek");

                url = urlBase + "/timeSlot/getTimeSlot.php?staff_ID=" + staffID + "&startTime=" + startTime + "&endTime=" + endTime + "&date=" + date + "&authorisation=" + urlAuth;

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
                }
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr2);

            if (jsonStr2 != null) {
                try {
                    // Get details as JSON array
                    JSONObject jsonObj2 = new JSONObject(jsonStr2);
                    final JSONArray details = jsonObj2.getJSONArray("records");

                    // Add the details to the array list
                    for (int i = 0; i < details.length(); i++) {
                        JSONObject c = details.getJSONObject(i);
                        timeSlotID = c.getString("timeSlot_ID");
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

            // Add the details to the array list
            String[] array1 = {name};
            String[] array2 = {dayOfWeek + ", " + date + ", " + startTime + " - " + endTime};
            String[] array3 = {staffName};
            String[] array4 = {treatmentName};

            for (int i = 0; i < array1.length; i++) {
                String where = array1[i];
                String when = array2[i];
                String who = array3[i];
                String what = array4[i];

                HashMap<String, String> review = new HashMap<>();
                review.put("where", where);
                review.put("when", when);
                review.put("who", who);
                review.put("what", what);
                reviewBookingList.add(review);
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

            // Add the booking array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    ReviewBooking.this, reviewBookingList,
                    R.layout.list_item_full_appointment,
                    new String[]{"where", "when", "who", "what"},
                    new int[]{R.id.where, R.id.when, R.id.who,
                    R.id.what});
            lv.setAdapter(adapter);
        }
    }
}