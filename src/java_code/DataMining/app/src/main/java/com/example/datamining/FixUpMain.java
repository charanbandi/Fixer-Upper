package com.example.datamining;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class FixUpMain extends AppCompatActivity implements OnMapReadyCallback {

    String[] category = {"All Keywords", "Unclassified", "Standard sale / Equity seller", "Foreclosure / REO Sale", "Short Sale", "Fixer Upper", "Bankruptcy Sale", "Creative/remodel", "Auction/Online Auction", "Starter Home", "Reduced Price", "Contractor", "Investment"};

    int sel_cat, sel_type;
    String sel_locality;
    public static GoogleMap mMap;
    Spinner category_spinner;
    //TextView location_tv;
    public static ListView property_list;
    LatLng sel_loc = new LatLng(37.335480, -121.893028);
    public static LinearLayout load_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fix_up_main);
        //viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        getSupportActionBar().hide();


        Bundle extras = getIntent().getExtras();

        if (extras == null) {
            Log.d("fix_up", "No location Received");
            return;
        } else {
            sel_locality = extras.getString("selectedLoc");
            sel_cat = extras.getInt("selectedCat");
            sel_type = extras.getInt("selectedType");
            Log.d("fix_up", "received= " + sel_cat + "\nPicked location= " + sel_locality);
        }


        load_layout = (LinearLayout) findViewById(R.id.loading_layout);
        load_layout.setVisibility(View.VISIBLE);


        new FixUpInit(this, sel_locality, sel_cat, sel_type).execute();
        init_contents();


    }


    private void init_contents() {
        setupMapFragment();
        property_list = (ListView) findViewById(R.id.fixup_listView);
        property_list.setLongClickable(true);
        category_spinner = (Spinner) findViewById(R.id.category_spinner);
//        location_tv = (TextView) findViewById(R.id.location_tv);
//        location_tv.setText(sel_locality);


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(dataAdapter);

        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View viewx, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fixup_map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.style_json));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        FixUpMain.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sel_loc, 11f));
        FixUpMain.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sel_loc, 11f));
    }
}