package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    private static String url;
    TextInputEditText emailSignIn, passwordSignIn;
    Button signInButton, signUpButton;
    private ProgressDialog pDialog;
    private String TAG = SignIn.class.getSimpleName();
    String email, password, response, clientID;
    DatabaseHelper db;

    /**
     * On create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //db = new DatabaseHelper(this);

        // Initialise buttons and add on click listeners
        signInButton = (Button)this.findViewById(R.id.SignInButton);
        signUpButton = (Button)this.findViewById(R.id.SignUpButton);
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

        // Initialise text input
        emailSignIn = (TextInputEditText)findViewById(R.id.emailSignIn);
        passwordSignIn = (TextInputEditText)findViewById(R.id.passwordSignIn);

        // Initialise database helper
        db = new DatabaseHelper(this);

        // Grab all data from the SQLite database
        Cursor res = db.getAllData();
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            buffer.append(res.getString(1));
        }

        // Count the values saved to the database
        int count = db.count();

        // If there is a value saved, the user wishes to stay logged in
        if (count > 0) {
            // Open the Home page and pass through the client ID taken from the database
            Intent in = new Intent(SignIn.this, Home.class);
            in.putExtra("clientID", buffer.toString());
            startActivity(in);
            finish();
        }
    }

    /**
     * On click method
     * @param v
     */
    public void onClick(View v) {
        Intent in;

        // If the user clicked the sign in button
        if (v.getId() == R.id.SignInButton) {

            // Convert email and password to strings
            email = emailSignIn.getText().toString();
            password = passwordSignIn.getText().toString();

            // If the user has not entered an email/password
            if (email.length() == 0 || password.length() == 0) {
                Toast.makeText(SignIn.this, "Please enter an email address and password.", Toast.LENGTH_LONG).show();
            }
            else {
                // Run check user exists function
                new SignIn.CheckUserExists().execute();
            }
        }
        // If the user clicked the sign up button
        else if (v.getId() == R.id.SignUpButton) {
            // Open the Sign Up screen
            in = new Intent(this, SignUp.class);
            startActivity(in);
        }
    }

    /**
     * Check a user exists in the database
     */
    private class CheckUserExists extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(SignIn.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // URL
            url = urlBase + "/client/checkUserExists.php?email=" + email + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get response as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray jsonArray = jsonObj.getJSONArray("records");
                    JSONObject jsonObj2 = jsonArray.getJSONObject(0);
                    response = jsonObj2.getString("value");
                }
                // JSON parsing error
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
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

            // If the user exists
            if (response.equals("1")) {
                // Run the Login method
                new SignIn.Login().execute();
            }
            else {
                // Tell the user their email address does not exist
                Toast.makeText(SignIn.this, "That email does not exist.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Log the user in
     */
    private class Login extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(SignIn.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // URL
            url = urlBase + "/client/login.php?email=" + email + "&password=" + password + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Get clientID as JSON array
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray jsonArray = jsonObj.getJSONArray("records");

                    JSONObject jsonObj2 = jsonArray.getJSONObject(0);
                    clientID = jsonObj2.getString("client_ID");
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

            // Tell the user their password is incorrect if a clientID could not be found
            if (clientID.equals("0")) {

                Toast.makeText(SignIn.this, "Your password is incorrect.", Toast.LENGTH_LONG).show();
            }

            // Sign the user in
            else {

                // Add the user to the db
                db.insertID(clientID);

                // Sign the user in
                Intent in = new Intent(SignIn.this, Home.class);
                in.putExtra("clientID", clientID);
                startActivity(in);
                finish();
            }
        }
    }
}