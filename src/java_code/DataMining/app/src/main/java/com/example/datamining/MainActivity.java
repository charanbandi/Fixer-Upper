package com.example.datamining;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button location_search;
    LinearLayout autocomplete_layout;
    EditText zip_et;
    AutocompleteSupportFragment autocompleteSupportFragment;
    PlacesClient placesClient;
    String locality=null;
    String[] str_selection = {"Search By locality", "Search By Zipcode", "Search By Current Location"};
    String[] str_type = {"Single Family", "Townhouse", "Condo"};
    NumberPicker np_selection,np_type;
    LatLng current_address;
    TextView error_invalid_entry;
    int category_val = 0;
    String apikey = "AIzaSyAtHx0MSOd3-4KJ-NZwwOlgJL8548dwWzI";
    int type = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C55CD6")));
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apikey);
        }
        init_contents();

        location_search.setOnClickListener(v -> {

            int cat = np_selection.getValue();
            type = np_type.getValue();//type of house to display
            System.out.println("Type selected is: "+ str_type[type]);
            if (cat == 0) {
                if (locality == null) {
                    error_invalid_entry.setText("Empty Input, please enter a City or Locality and try again");
                    error_invalid_entry.setVisibility(View.VISIBLE);
                    error_invalid_entry.startAnimation(shakeError());
                } else {
                    final String[] splitUserInput = locality.split(", ");
                    if (splitUserInput.length < 3) {
                        error_invalid_entry.setText("Please try adding a valid City or Locality");
                        error_invalid_entry.setVisibility(View.VISIBLE);
                        error_invalid_entry.startAnimation(shakeError());

                    } else {
                        Log.d("set_value", "user Input: " + locality);
                        error_invalid_entry.setVisibility(View.INVISIBLE);
                        Intent i = new Intent(getApplicationContext(), FixUpMain.class);
                        i.putExtra("selectedCat", category_val);
                        i.putExtra("selectedLoc", locality);
                        i.putExtra("selectedType", type);
                        startActivity(i);
                    }
                }


            }
            if (cat == 1) {
                if ((String.valueOf(zip_et.getText())).equals("") || (String.valueOf(zip_et.getText())).length() != 5) {
                    error_invalid_entry.setText("Please add a valid Zip Code and try again");
                    error_invalid_entry.setVisibility(View.VISIBLE);
                    error_invalid_entry.startAnimation(shakeError());
                } else {
                    final int code = Integer.parseInt(String.valueOf(zip_et.getText()));
                    final String[] passable_address = {null};
                    Log.d("google_geocode", "Entered Code is: " + code);

                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                //Your code goes here
                                passable_address[0] = getLocationInfo(code);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }//waits for query to return

                    if (passable_address[0] == null) {
                        error_invalid_entry.setText("Invalid ZIP");
                        error_invalid_entry.setVisibility(View.VISIBLE);
                        error_invalid_entry.startAnimation(shakeError());
                    } else {
                        locality = passable_address[0];
                        error_invalid_entry.setVisibility(View.INVISIBLE);

                        Intent i = new Intent(getApplicationContext(), FixUpMain.class);
                        i.putExtra("selectedCat", category_val);
                        i.putExtra("selectedLoc", locality);
                        i.putExtra("selectedType", type);
                        startActivity(i);
                    }
                }

            }

            if (cat == 2) {
                final String[] passable_address = {null};
                if (current_address == null) {
                    error_invalid_entry.setText("Unable to get Current Location");
                    error_invalid_entry.setVisibility(View.VISIBLE);
                    error_invalid_entry.startAnimation(shakeError());
                } else {
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                //Your code goes here
                                passable_address[0] = getLocationInfo1(current_address);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }//waits for query to return
                    locality = passable_address[0];
                    error_invalid_entry.setVisibility(View.INVISIBLE);

                    Intent i = new Intent(getApplicationContext(), FixUpMain.class);
                    i.putExtra("selectedCat", category_val);
                    i.putExtra("selectedLoc", locality);
                    i.putExtra("selectedType", type);
                    startActivity(i);
                }
            }
        });

    }

    private void init_contents() {
        placesClient = Places.createClient(this);
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fixup_autocomplete_fragment);
        autocompleteSupportFragment.setHint("Enter a City or Locality");
        autocompleteSupportFragment.getView().setBackgroundColor(Color.WHITE);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                locality = place.getAddress();
            }
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), "Please try again" + status, Toast.LENGTH_LONG).show();
            }
        });

        zip_et = (EditText) findViewById(R.id.fixup_zip_fragment);
        autocomplete_layout = (LinearLayout) findViewById(R.id.fixup_autocomplete_layout);
        location_search = (Button) findViewById(R.id.location_search);
        error_invalid_entry = (TextView) findViewById(R.id.fixup_error_msg_invalid);

        np_selection = (NumberPicker) findViewById(R.id.fixup_np);
        np_selection.setMinValue(0);
        np_selection.setMaxValue(2);
        np_selection.setDisplayedValues(str_selection);
        np_selection.setOnValueChangedListener(onValueChangeListener);

        np_type = (NumberPicker) findViewById(R.id.fixup_type);
        np_type.setMinValue(0);
        np_type.setMaxValue(2);
        np_type.setDisplayedValues(str_type);


    }

    NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int i, int i1) {
            autocompleteSupportFragment.setText("");
            Log.d("google_geocode", "Picked: " + numberPicker.getValue());
            switch (numberPicker.getValue()) {
                case 0:
                    Log.d("google_geocode", "case :0");
                    zip_et.setVisibility(View.INVISIBLE);
                    autocomplete_layout.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    Log.d("google_geocode", "case :1");
                    zip_et.setVisibility(View.VISIBLE);
                    autocomplete_layout.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    Log.d("google_geocode", "case :2");
                    zip_et.setVisibility(View.INVISIBLE);
                    autocomplete_layout.setVisibility(View.INVISIBLE);


                    double longitude = 0.0, latitude = 0.0;
                    LocationFinder finder = new LocationFinder(MainActivity.this);
                    if (finder.canGetLocation()) {
                        latitude = finder.getLatitude();
                        longitude = finder.getLongitude();
                        Log.d("google_geocode", "Lat-long: " + latitude + "  " + longitude);
                        current_address = new LatLng(latitude, longitude);
                    } else {
                        finder.showSettingsAlert();
                    }
                    break;
            }
        }
    };

    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(5));
        return shake;
    }

    public String getLocationInfo(int zipcode) {
        String address = null;

        try {


            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyAtHx0MSOd3-4KJ-NZwwOlgJL8548dwWzI&components=postal_code:" + zipcode);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(builder.toString());
            //Log.d("google_geocode", "number" + jsonObject.length());
            address = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            //Log.d("google_geocode", "address is: " + address);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return address;
    }

    public String getLocationInfo1(LatLng cur_loc) {
        String address = null;

        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyAtHx0MSOd3-4KJ-NZwwOlgJL8548dwWzI&latlng=" + cur_loc.latitude + "," + cur_loc.longitude);
            //Log.d("google_geocode","https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyAtHx0MSOd3-4KJ-NZwwOlgJL8548dwWzI&latlng="+cur_loc.latitude+","+cur_loc.longitude);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line + "\n");
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(builder.toString());
            //Log.d("google_geocode", "number" + jsonObject.length());
            String temp = jsonObject.getJSONObject("plus_code").getString("compound_code");
            String[] temp_1 = temp.split(" ", 2);
            address = temp_1[1];
            //Log.d("google_geocode", "address is: " + address);


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return address;
    }
}