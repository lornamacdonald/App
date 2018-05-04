package com.example.lorna.app;

// Imports
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    private static String url;
    String response, clientID;
    private String TAG = SignUp.class.getSimpleName();
    private ProgressDialog pDialog;
    Button createAccountButton;
    TextInputEditText firstName, lastName, dob, gender, email, confirmEmail, phone, password, confirmPassword, address, town, county, postcode;

    /**
     * On Create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Create account button
        createAccountButton = (Button) this.findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(this);

        // Get all values from text input fields
        firstName = (TextInputEditText)findViewById(R.id.firstName);
        lastName = (TextInputEditText)findViewById(R.id.lastName);
        dob = (TextInputEditText)findViewById(R.id.dob);
        gender = (TextInputEditText)findViewById(R.id.gender);
        email = (TextInputEditText)findViewById(R.id.email);
        confirmEmail = (TextInputEditText)findViewById(R.id.confirmEmail);
        phone = (TextInputEditText)findViewById(R.id.phone);
        password = (TextInputEditText)findViewById(R.id.password);
        confirmPassword = (TextInputEditText)findViewById(R.id.confirmPassword);
        address = (TextInputEditText)findViewById(R.id.address);
        town = (TextInputEditText)findViewById(R.id.town);
        county = (TextInputEditText)findViewById(R.id.county);
        postcode = (TextInputEditText)findViewById(R.id.postcode);
    }

    /**
     * On click listener for the create account button
     * @param v
     */
    public void onClick(View v) {
        // Convert values to strings
        String firstNameText = firstName.getText().toString();
        String lastNameText = lastName.getText().toString();
        String dobText = dob.getText().toString();
        String genderText = gender.getText().toString();
        String emailText = email.getText().toString();
        String confirmEmailText = confirmEmail.getText().toString();
        String phoneText = phone.getText().toString();
        String passwordText = password.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();
        String addressText = address.getText().toString();
        String townText = town.getText().toString();
        String countyText = county.getText().toString();
        String postcodeText = postcode.getText().toString();

        // Validations
        if (!emailText.equals(confirmEmailText)) {
            Toast.makeText(SignUp.this, "Email addresses do not match.", Toast.LENGTH_LONG).show();
            email.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
            confirmEmail.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (!passwordText.equals(confirmPasswordText)) {
            Toast.makeText(SignUp.this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            password.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
            confirmPassword.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (phoneText.length() != 11) {
            Toast.makeText(SignUp.this, "Please enter a valid phone number.", Toast.LENGTH_LONG).show();
            phone.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (postcodeText.length() != 7) {
            Toast.makeText(SignUp.this, "Please enter a valid postcode.", Toast.LENGTH_LONG).show();
            postcode.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (!genderText.equals("Male") && !genderText.equals("Female")) {
            Toast.makeText(SignUp.this, "Please enter 'Male' or 'Female'.", Toast.LENGTH_LONG).show();
            gender.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (!emailText.contains("@")) {
            Toast.makeText(SignUp.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
            email.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (!confirmEmailText.contains("@")) {
            Toast.makeText(SignUp.this, "Please enter a valid email address.", Toast.LENGTH_LONG).show();
            confirmEmail.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }
        if (firstNameText.isEmpty() || lastNameText.isEmpty() || dobText.isEmpty() || genderText.isEmpty() || emailText.isEmpty() || confirmEmailText.isEmpty() || phoneText.isEmpty() || passwordText.isEmpty() ||
                confirmPasswordText.isEmpty() || addressText.isEmpty() || townText.isEmpty() || countyText.isEmpty() || postcodeText.isEmpty()) {
            Toast.makeText(SignUp.this, "Please fill out every field.", Toast.LENGTH_LONG).show();
        }
        if (!dobText.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})")) {
            Toast.makeText(SignUp.this, "Please enter a date in the format: YYYY-MM-DD.", Toast.LENGTH_LONG).show();
            dob.setTextColor(ContextCompat.getColor(SignUp.this, R.color.red));
        }

        // If everything is correct, run the check user exists method
        if (emailText.equals(confirmEmailText) && passwordText.equals(confirmPasswordText)
                && (firstNameText.length()>0) && (lastNameText.length()>0) && (dobText.length()>0) && (genderText.length()>0)
                && (emailText.length()>0) && (confirmEmailText.length()>0) && (phoneText.length()==11) && (passwordText.length()>0) && (confirmPasswordText.length()>0)
                && (addressText.length()>0) && (townText.length()>0) && (countyText.length()>0) && (postcodeText.length()==7)
                && (emailText.contains("@") && (confirmEmailText.contains("@")))
                && (dobText.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})"))
                && (genderText.equals("Male") || (genderText.equals("Female"))))  {
            new SignUp.CheckUserExists().execute();
        }
    }

    /**
     * Checks if a user exists in the database
     */
    private class CheckUserExists extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(SignUp.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add the email address entered onto the URL
            String emailText = email.getText().toString();
            url = urlBase + "/client/checkUserExists.php?email=" + emailText + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            // Get the response (1 or 0)
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray jsonArray = jsonObj.getJSONArray("records");
                    JSONObject jsonObj2 = jsonArray.getJSONObject(0);
                    response = jsonObj2.getString("value");
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
                                "Couldn't get json from server.Check LogCat for possible errors!",
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

            // The email address already exists
            if (response.equals("1")) {
                Toast.makeText(SignUp.this, "This email already exists.", Toast.LENGTH_LONG).show();
            }

            // The email address does not exist - create account
            else if (response.equals("0")) {
                // Create a dialog
                AlertDialog alertDialog = new AlertDialog.Builder(SignUp.this).create();
                alertDialog.setTitle("Account Created");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Continue",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Run the Create Account method
                                dialog.dismiss();
                                new SignUp.CreateAccount().execute();
                                Intent in = new Intent(SignUp.this, SignIn.class);
                                startActivity(in);
                                finish();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    /**
     * Adds a client to the database
     */
    private class CreateAccount extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(SignUp.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            url = urlBase + "/client/create.php";
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Convert text inputs to strings
            String firstNameText = firstName.getText().toString();
            String lastNameText = lastName.getText().toString();
            String dobText = dob.getText().toString();
            String genderText = gender.getText().toString();
            String emailText = email.getText().toString();
            String phoneText = phone.getText().toString();
            String passwordText = password.getText().toString();
            String addressText = address.getText().toString();
            String townText = town.getText().toString();
            String countyText = county.getText().toString();
            String postcodeText = postcode.getText().toString();

            // Details for the POST request
            String[] details = {"password", passwordText, "genderName", genderText, "email", emailText,
                    "firstName", firstNameText, "lastName", lastNameText, "dob", dobText,
                    "phone", phoneText, "address", addressText, "town", townText,
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