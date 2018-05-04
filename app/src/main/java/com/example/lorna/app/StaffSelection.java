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

public class StaffSelection extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    TextView tv;
    String genderName, clientID, name, organisationID, treatmentName;
    private String TAG = StaffSelection.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> staffList;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_selection);

        // Initialise array list and list view
        staffList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listTreatments);

        // Run the Get Staff method
        new StaffSelection.GetStaff().execute();
    }

    /**
     * Create the action bar icons
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_search_gender, menu);
        return true;
    }

    /**
     * On click for the action bar icons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Filter staff by gender
        switch (item.getItemId()) {
            case R.id.maleSelection:
                genderName = "Male";
                new StaffSelection.SearchStaff().execute();
                return true;
            case R.id.femaleSelection:
                genderName = "Female";
                new StaffSelection.SearchStaff().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Gets staff offering the specified treatment from the database
     */
    private class GetStaff extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(StaffSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                treatmentName = extras.getString("treatmentName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/staffMember/search.php?treatmentName=" + treatmentName + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get staff as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray staffs = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the TimeSlotSelection activity
                            Intent in = new Intent(StaffSelection.this, TimeSlotSelection.class);

                            // Pass through the necessary values
                            for (int j = 0; j < staffs.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = staffs.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String staffID = c.getString("staff_ID");
                                    String title = c.getString("title");
                                    String firstName = c.getString("firstName");
                                    String lastName = c.getString("lastName");
                                    String staffName = title + " " + firstName + " " + lastName;
                                    in.putExtra("staffID", staffID);
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("treatmentName", treatmentName);
                                    in.putExtra("name", name);
                                    in.putExtra("staffName", staffName);
                                    in.putExtra("clientID", clientID);
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

                    // Add the staff to the array list
                    for (int i = 0; i < staffs.length(); i++) {
                        JSONObject c = staffs.getJSONObject(i);
                        String title = c.getString("title");
                        String firstName = c.getString("firstName");
                        String lastName = c.getString("lastName");
                        String staffName = title + " " + firstName + " " + lastName;
                        HashMap<String, String> staff = new HashMap<>();
                        staff.put("staffName", staffName);
                        staffList.add(staff);
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

            // Add the staff array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    StaffSelection.this, staffList,
                    R.layout.list_item_simple, new String[]{"staffName"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no staff
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setText("There are no staff that offer this treatment.");
            }
        }
    }

    /**
     * Gets staff of a specified gender offering a specified treatment from the database
     */
    private class SearchStaff extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(StaffSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                treatmentName = extras.getString("treatmentName");
                name = extras.getString("name");
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/staffMember/searchGender.php?organisation_ID=" + organisationID + "&genderName=" + genderName + "&treatmentName=" + treatmentName + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get the staff list as a JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray staffs = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the TimeSlotSelection activity
                            Intent in = new Intent(StaffSelection.this, TimeSlotSelection.class);

                            // Pass through the necessary values
                            for (int j = 0; j < staffs.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = staffs.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String staffID = c.getString("staff_ID");
                                    String title = c.getString("title");
                                    String firstName = c.getString("firstName");
                                    String lastName = c.getString("lastName");
                                    String staffName = title + " " + firstName + " " + lastName;
                                    in.putExtra("staffID", staffID);
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("treatmentName", treatmentName);
                                    in.putExtra("name", name);
                                    in.putExtra("staffName", staffName);
                                    in.putExtra("clientID", clientID);
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

                    // Empty the array list
                    staffList.clear();

                    // Add the staff to the array list
                    for (int i = 0; i < staffs.length(); i++) {
                        JSONObject c = staffs.getJSONObject(i);
                        String title = c.getString("title");
                        String firstName = c.getString("firstName");
                        String lastName = c.getString("lastName");
                        String staffName = title + " " + firstName + " " + lastName;
                        HashMap<String, String> staff = new HashMap<>();
                        staff.put("staffName", staffName);
                        staffList.add(staff);
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

            // Add the staff array list to the view
            ListAdapter adapter = new SimpleAdapter(
                    StaffSelection.this, staffList,
                    R.layout.list_item_simple, new String[]{"staffName"}, new int[]{R.id.textViewSimple});
            lv.setAdapter(adapter);

            // Display a message if there are no dates
            if (adapter.getCount() < 1) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setText("There are no staff that offer this treatment.");
            }
        }
    }
}