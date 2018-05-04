package com.example.lorna.app;

// Imports
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class Account extends AppCompatActivity {

    // Variables
    String clientID;
    Intent in;
    private ListView lv;
    ArrayList<HashMap<String, String>> optionList;

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
                    in = new Intent(Account.this, Calendar.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_search:
                    in = new Intent(Account.this, Search.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_favourites:
                    in = new Intent(Account.this, Favourites.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_home:
                    in = new Intent(Account.this, Home.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                    return true;
                case R.id.navigation_account:
                    in = new Intent(Account.this, Account.class);
                    in.putExtra("clientID", clientID);
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
        setContentView(R.layout.activity_account);

        // Initialise navigation bar
        BottomNavigationView navigation = (BottomNavigationView)findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_account);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Initialise array list and list view
        optionList = new ArrayList<>();
        lv = (ListView)findViewById(R.id.listAccount);

        // Add the options to the array list
        String theOption = "";
        String[] optionsArray = {"Personal Details", "Delete Account"};
        for (int i = 0; i < optionsArray.length; i++) {
            theOption = optionsArray[i];
            HashMap<String, String> option = new HashMap<>();
            option.put("theOption", theOption);
            optionList.add(option);
        }

        // Add the option array list to the list view
        ListAdapter adapter = new SimpleAdapter(Account.this, optionList,
                R.layout.list_item_simple, new String[]{"theOption"},
                new int[]{R.id.textViewSimple});
        lv.setAdapter(adapter);

        // Get the clientID
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clientID = extras.getString("clientID");
        }

        // On click listener for the options
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // If the user taps "Personal Details"
                if (i == 0) {
                    Intent in = new Intent(Account.this, PersonalDetails.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                }
                // If the user taps "Delete Account"
                else if (i == 1) {
                    Intent in = new Intent(Account.this, DeleteAccount.class);
                    in.putExtra("clientID", clientID);
                    startActivity(in);
                }
            }
        });
    }
}