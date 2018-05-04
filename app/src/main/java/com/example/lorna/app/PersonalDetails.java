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
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class PersonalDetails extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String clientID;
    Intent in;
    private String TAG = PersonalDetails.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    private static String url;
    ArrayList<HashMap<String, String>> personalDetailsList;
    Button editDetails;
    TextInputEditText email, phone, password, address, town, county, postcode;

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
                    in = new Intent(PersonalDetails.this, Calendar.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(PersonalDetails.this, Search.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(PersonalDetails.this, Account.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(PersonalDetails.this, Favourites.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(PersonalDetails.this, Home.class);
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
        setContentView(R.layout.activity_personal_details);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_account);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        personalDetailsList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.listPersonalDetails);

        // Run the Get Details method
        new PersonalDetails.GetDetails().execute();

        // Intialise edit details button and set on click listener
        editDetails = (Button)findViewById(R.id.editPersonalDetails);
        editDetails.setOnClickListener(this);
    }

    /**
     * On click method
     * @param v
     */
    public void onClick(View v) {
        // Get all values from text input fields
        email = (TextInputEditText)findViewById(R.id.emailDetails);
        phone = (TextInputEditText)findViewById(R.id.phoneDetails);
        password = (TextInputEditText)findViewById(R.id.passwordDetails);
        address = (TextInputEditText)findViewById(R.id.addressDetails);
        town = (TextInputEditText)findViewById(R.id.townDetails);
        county = (TextInputEditText)findViewById(R.id.countyDetails);
        postcode = (TextInputEditText)findViewById(R.id.postcodeDetails);

        // Convert values to strings
        String emailText = email.getText().toString();
        String phoneText = phone.getText().toString();
        String passwordText = password.getText().toString();
        String addressText = address.getText().toString();
        String townText = town.getText().toString();
        String countyText = county.getText().toString();
        String postcodeText = postcode.getText().toString();

        // Validations
        if (phoneText.length() != 11) {
            Toast.makeText(PersonalDetails.this, "Please enter a valid phone number.", Toast.LENGTH_LONG).show();
            phone.setTextColor(ContextCompat.getColor(PersonalDetails.this, R.color.colorPrimaryDark));
        }
        if (postcodeText.length() != 7) {
            Toast.makeText(PersonalDetails.this, "Please enter a valid postcode.", Toast.LENGTH_LONG).show();
            postcode.setTextColor(ContextCompat.getColor(PersonalDetails.this, R.color.colorPrimaryDark));
        }

        if (!emailText.contains("@")) {
            Toast.makeText(PersonalDetails.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
            email.setTextColor(ContextCompat.getColor(PersonalDetails.this, R.color.colorPrimaryDark));
        }

        if (emailText.isEmpty() || phoneText.isEmpty() || passwordText.isEmpty() || addressText.isEmpty() || townText.isEmpty() || countyText.isEmpty() || postcodeText.isEmpty()) {
            Toast.makeText(PersonalDetails.this, "Please fill out every field.", Toast.LENGTH_LONG).show();
        }

        // If everything is correct, run the Edit Details method
        if ((emailText.length()>0) && (phoneText.length()==11) && (passwordText.length()>0) && (addressText.length()>0) &&
                (townText.length()>0) && (countyText.length()>0) && (postcodeText.length()==7)
                && (emailText.contains("@"))) {

            // Create an alert
            AlertDialog alertDialog = new AlertDialog.Builder(PersonalDetails.this).create();
            alertDialog.setTitle("Details Saved");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Continue",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new PersonalDetails.EditDetails().execute();
                            Intent in = new Intent(PersonalDetails.this, PersonalDetails.class);
                            in.putExtra("clientID", clientID);
                            startActivity(in);
                        }
                    });
            alertDialog.show();
        }
    }

    /**
     * Gets the client's details from the database
     */
    private class GetDetails extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(PersonalDetails.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/client/read_one.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
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
                    final JSONArray clientDetails = jsonObj.getJSONArray("records");

                    // Add the details to the array list
                    for (int i = 0; i < clientDetails.length(); i++) {
                        JSONObject c = clientDetails.getJSONObject(i);

                        String phone = c.getString("phone");
                        String email = c.getString("email");
                        String password = c.getString("password");
                        String address = c.getString("address");
                        String town = c.getString("town");
                        String county = c.getString("county");
                        String postcode = c.getString("postcode");

                        HashMap<String, String> client = new HashMap<>();

                        client.put("phone", phone);
                        client.put("email", email);
                        client.put("password", password);
                        client.put("address", address);
                        client.put("town", town);
                        client.put("county", county);
                        client.put("postcode", postcode);

                        personalDetailsList.add(client);
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
                    PersonalDetails.this, personalDetailsList,
                    R.layout.list_item_personal_details, new String[]{"email", "phone", "password", "address", "town", "county", "postcode"},
                    new int[]{R.id.emailDetails, R.id.phoneDetails, R.id.passwordDetails, R.id.addressDetails, R.id.townDetails, R.id.countyDetails, R.id.postcodeDetails});
            lv.setAdapter(adapter);
        }
    }

    /**
     * Update the client's details in the database
     */
    private class EditDetails extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(PersonalDetails.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/client/update.php?client_ID=" + clientID;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get all values from text input fields
            email = (TextInputEditText)findViewById(R.id.emailDetails);
            phone = (TextInputEditText)findViewById(R.id.phoneDetails);
            password = (TextInputEditText)findViewById(R.id.passwordDetails);
            address = (TextInputEditText)findViewById(R.id.addressDetails);
            town = (TextInputEditText)findViewById(R.id.townDetails);
            county = (TextInputEditText)findViewById(R.id.countyDetails);
            postcode = (TextInputEditText)findViewById(R.id.postcodeDetails);

            // Convert values to strings
            String emailText = email.getText().toString();
            String phoneText = phone.getText().toString();
            String passwordText = password.getText().toString();
            String addressText = address.getText().toString();
            String townText = town.getText().toString();
            String countyText = county.getText().toString();
            String postcodeText = postcode.getText().toString();

            // Details for the PUT request
            String[] details = {"client_ID", clientID, "phone", phoneText, "email", emailText,
                    "password", passwordText, "address", addressText, "town", townText,
                    "county", countyText, "postcode", postcodeText, "authorisation", urlAuth};

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
}
