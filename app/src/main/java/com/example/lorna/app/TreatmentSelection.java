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

public class TreatmentSelection extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    private static String urlSearch, urlCanBook;
    private TextView tv;
    private String organisationID, clientID, value;
    private String TAG = TreatmentSelection.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> treatmentsList;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_selection);

        // Initialise array list and list view
        treatmentsList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.listTreatments);

        // Run the Get Treatments method
        new TreatmentSelection.GetTreatments().execute();
    }

    /**
     * Gets a list of treatments from the database
     */
    private class GetTreatments extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(TreatmentSelection.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
                clientID = extras.getString("clientID");
            }

            // URL
            urlSearch = urlBase + "/treatment/search.php?organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
            urlCanBook = urlBase + "/appointment/canBook.php?client_ID=" + clientID + "&organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh2 = new HttpRequest();
            String jsonStr2 = sh2.makeServiceCall(urlCanBook);
            Log.e(TAG, "Response from url: " + jsonStr2);

            if (jsonStr2 != null) {
                try {
                    // Get response as JSON array
                    JSONObject jsonObj2 = new JSONObject(jsonStr2);
                    final JSONArray treatments = jsonObj2.getJSONArray("records");
                    for (int i = 0; i < treatments.length(); i++) {
                        JSONObject c = treatments.getJSONObject(i);
                        value = c.getString("value");
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
                        Toast.makeText(getApplicationContext(),"Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(urlSearch);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get treatments list as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    final JSONArray treatments = jsonObj.getJSONArray("records");

                    // On click listener for list items
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            // Open the StaffSelection activity
                            Intent in = new Intent(TreatmentSelection.this, StaffSelection.class);

                            // Pass through the necessary values
                            for (int j = 0; j < treatments.length(); j++) {
                                JSONObject c = null;
                                try {
                                    c = treatments.getJSONObject(i);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    String name = c.getString("name");
                                    String organisationID = c.getString("organisation_ID");
                                    String treatmentName = c.getString("treatmentName");
                                    in.putExtra("organisationID", organisationID);
                                    in.putExtra("treatmentName", treatmentName);
                                    in.putExtra("name", name);
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

                    // Add the treatments to the array list
                    for (int i = 0; i < treatments.length(); i++) {
                        JSONObject c = treatments.getJSONObject(i);
                        String treatmentName = c.getString("treatmentName");
                        String name = c.getString("name");
                        HashMap<String, String> treatment = new HashMap<>();
                        treatment.put("treatmentName", treatmentName);
                        treatment.put("name", name);
                        treatmentsList.add(treatment);
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
                        Toast.makeText(getApplicationContext(),"Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
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

            // Add the treatments array list to the list view if the user is allowed to book
            if (value.equals("true")) {
                ListAdapter adapter = new SimpleAdapter(
                        TreatmentSelection.this, treatmentsList,
                        R.layout.list_item_simple, new String[]{"treatmentName"}, new int[]{R.id.textViewSimple});
                lv.setAdapter(adapter);
            }

            // Display a message if the user is not allowed to book
            else if (value.equals("false")) {
                tv = (TextView)findViewById(R.id.textView2);
                tv.setVisibility(View.VISIBLE);
                tv.setText("You cannot make more than one appointment today. Contact the organisation directly through phone or email to book.");
            }
        }
    }
}