package com.example.lorna.app;

// Imports
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class TimeSlotSelection extends AppCompatActivity {

    // Variables
    String organisationID, staffName, staffID, treatmentName, name, clientID;
    private ListView lv;
    ArrayList<HashMap<String, String>> optionList;
    private String TAG = TimeSlotSelection.class.getSimpleName();
    private ProgressDialog pDialog;

    /**
     * On create method
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_slot_selection);

        // Initialise array list and list view
        optionList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.listTimeSlot);

        // Get values passed through
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            organisationID = extras.getString("organisationID");
            treatmentName = extras.getString("treatmentName");
            name = extras.getString("name");
            staffName = extras.getString("staffName");
            staffID = extras.getString("staffID");
            clientID = extras.getString("clientID");
        }

        // Add the options to the array list
        String theOption = "";
        String[] optionsArray = {"Choose a Time Slot", "Next Available Time Slot"};
        for (int i = 0; i < optionsArray.length; i++) {
            theOption = optionsArray[i];
            HashMap<String, String> option = new HashMap<>();
            option.put("theOption", theOption);
            optionList.add(option);
        }

        // Add the option array list to the list view
        ListAdapter adapter = new SimpleAdapter(
                TimeSlotSelection.this, optionList, R.layout.list_item_simple,
                new String[]{"theOption"},
                new int[]{R.id.textViewSimple});
        lv.setAdapter(adapter);

        // On click listener for the options
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            // If the user taps "Choose a Time Slot"
            if (i == 0) {
                Intent in = new Intent(TimeSlotSelection.this, DateSelection.class);
                in.putExtra("staffID", staffID);
                in.putExtra("organisationID", organisationID);
                in.putExtra("treatmentName", treatmentName);
                in.putExtra("name", name);
                in.putExtra("staffName", staffName);
                in.putExtra("clientID", clientID);
                startActivity(in);
                finish();
            }

            // If the user taps "Next Available Time Slot"
            if (i == 1) {
                Intent in = new Intent(TimeSlotSelection.this, NextAvailableTimeSlot.class);
                in.putExtra("staffID", staffID);
                in.putExtra("organisationID", organisationID);
                in.putExtra("treatmentName", treatmentName);
                in.putExtra("name", name);
                in.putExtra("staffName", staffName);
                in.putExtra("clientID", clientID);
                startActivity(in);
                finish();
            }
            }

        });
    }
}