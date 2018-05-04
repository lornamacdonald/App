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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DeleteAccount extends AppCompatActivity implements View.OnClickListener {

    // Variables
    private static String urlBase = "http://ec2-34-244-207-123.eu-west-1.compute.amazonaws.com";
    private static String urlAuth = "$2y$10$60xO2MVjdSHM1XRQ0cx5WeRfspfhAVlM3iJRZ/y/L65wMmYjIhEEq";
    String organisationID, date, name, timeSlotID, clientID;
    private Intent in;
    private String TAG = DeleteAccount.class.getSimpleName();
    private ProgressDialog pDialog;
    private static String url;
    Button cancelAppt;
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
                    in = new Intent(DeleteAccount.this, Calendar.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(DeleteAccount.this, Search.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(DeleteAccount.this, Account.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(DeleteAccount.this, Favourites.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(DeleteAccount.this, Home.class);
                    in.putExtra("clientID",clientID);
                    startActivity(in);
                    return true;
            }
            return false;
        }
    };

    /**
     * On Create method
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_account);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Cancel appointment button
        cancelAppt = (Button)findViewById(R.id.deleteAccountButton);
        cancelAppt.setOnClickListener(this);

        // Initialise Database Helper
        db = new DatabaseHelper(this);
    }

    /**
     * On click listener for cancel appointment button
     * @param v
     */
    public void onClick(View v) {

        // Get the clientID
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clientID = extras.getString("clientID");
        }

        // Create an alert
        AlertDialog alertDialog = new AlertDialog.Builder(DeleteAccount.this).create();
        alertDialog.setTitle("Delete Account");
        alertDialog.setMessage("Are you sure you want to delete your account?");

        // Yes button - delete account
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        db.deleteData(clientID);
                        Intent in = new Intent(DeleteAccount.this, SignIn.class);
                        in.putExtra("clientID", clientID);
                        new DeleteAccount.DeleteAppointments().execute();
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
    }

    /**
     * Deletes all the client's appointments from the database
     */
    private class DeleteAppointments extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(DeleteAccount.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/appointment/deleteAll.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            // Run the Delete Favourites method
            new DeleteAccount.DeleteFavourites().execute();
        }
    }

    /**
     * Deletes all the client's favourites from the database
     */
    private class DeleteFavourites extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(DeleteAccount.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/favourites/deleteAll.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the progress dialog
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            // Run the Delete Account method
            new DeleteAccount.Delete().execute();
        }
    }

    /**
     * Deletes a client from the database
     */
    private class Delete extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Create a progress dialog
            pDialog = new ProgressDialog(DeleteAccount.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            // Add clientID onto URL
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                clientID = extras.getString("clientID");
            }

            // URL
            url = urlBase + "/client/delete.php?client_ID=" + clientID + "&authorisation=" + urlAuth;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Making a request to url and getting response
            HttpRequest sh = new HttpRequest();
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);
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