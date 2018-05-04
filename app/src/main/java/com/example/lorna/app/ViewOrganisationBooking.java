package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewOrganisationBooking extends AppCompatActivity {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String organisationID, name;
    private String TAG = ViewOrganisationBooking.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> organisationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_organisation_booking);

        // Intialise array list and list view
        organisationList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.list2);

        // Run the Get Organisation method
        new ViewOrganisationBooking.GetOrganisation().execute();
    }

    /**
     * Gets an organisation from the database
     */
    private class GetOrganisation extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(ViewOrganisationBooking.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add values onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                organisationID = extras.getString("organisationID");
            }

            // URL
            url = urlBase + "/organisation/read_one.php?organisation_ID=" + organisationID + "&authorisation=" + urlAuth;
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
                    final JSONArray organisations = jsonObj.getJSONArray("records");

                    // Add the details to the array list
                    for (int i = 0; i < organisations.length(); i++) {
                        JSONObject c = organisations.getJSONObject(i);

                        String organisation_ID = c.getString("organisation_ID");
                        String typeName = c.getString("typeName");
                        String address = c.getString("address");
                        String town = c.getString("town");
                        String postcode = c.getString("postcode");
                        String description = c.getString("description");
                        String email = c.getString("email");
                        String phone = c.getString("phone");
                        String fullAddress = address + ", " + town + ", " + postcode;
                        name = c.getString("name");
                        String openingTimes = c.getString("openingTimes");

                        HashMap<String, String> organisation = new HashMap<>();

                        organisation.put("organisation_ID", organisation_ID);
                        organisation.put("typeName", typeName);
                        organisation.put("fullAddress", fullAddress);
                        organisation.put("description", description);
                        organisation.put("email", email);
                        organisation.put("phone", phone);
                        organisation.put("openingTimes", openingTimes);
                        organisation.put("name", name);
                        organisation.put("openingTimes", openingTimes);

                        organisationList.add(organisation);
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

            // Add the details array list to the list view
            ListAdapter adapter = new SimpleAdapter(
                    ViewOrganisationBooking.this, organisationList,
                    R.layout.list_item_organisation, new String[]{"typeName", "fullAddress", "phone",
                    "email", "description", "openingTimes"},
                    new int[]{R.id.orgTypeName, R.id.orgAddress, R.id.orgPhone,
                            R.id.orgEmail, R.id.orgDescription, R.id.orgOpeningTimes});
            lv.setAdapter(adapter);

            // Set the action bar title
            getSupportActionBar().setTitle(name);
        }
    }
}