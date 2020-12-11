package com.example.datamining;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alespero.expandablecardview.ExpandableCardView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;


public class FixUpDetailsSimple extends AppCompatActivity implements Serializable, OnMapReadyCallback {

    HouseDetailsDesc house;

    TextView address, price, type, bed_bath, area_lot, year, rent, hoa, tv_cooling, tv_heating, tv_parking, tv_remodel_main,tv_remodel_desc, tv_desc;

    Button but_pre, but_fut;
    String cooling, heating, parking;
    public static GoogleMap type_map;

    public static ExpandableCardView remodel_expand;

    public static List<Marker> markerList = new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.option2)));
        setContentView(R.layout.activity_fix_up_details_simple);

        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#fbfbfb"));
        }

        house = (HouseDetailsDesc) getIntent().getSerializableExtra("HouseDetails");

        setupMapFragment();

        address = findViewById(R.id.b_address);
        price = findViewById(R.id.b_price);
        type = findViewById(R.id.b_type);
        bed_bath = findViewById(R.id.b_bed_bath);
        area_lot = findViewById(R.id.b_area_lot);
        year = findViewById(R.id.b_year);
        rent = findViewById(R.id.b_rent);
        hoa = findViewById(R.id.b_hoa);
        tv_desc = findViewById(R.id.b_desc);

        tv_cooling = findViewById(R.id.b_cooling);
        tv_heating = findViewById(R.id.b_heating);
        tv_parking = findViewById(R.id.b_parking);
        tv_remodel_main= findViewById(R.id.b_remodel_price_est);
        remodel_expand = (ExpandableCardView) findViewById(R.id.remodel_stats);
        tv_remodel_desc = (TextView) findViewById(R.id.remodel_tv);
        tv_remodel_desc.setMovementMethod(new ScrollingMovementMethod());


        address.setText(String.valueOf(house.getAddress()));
        price.setText("$" + format(house.getPrice().intValue()));
        type.setText(String.valueOf(house.getType()));
        bed_bath.setText((house.getBedrooms().intValue() + " Bed/" + house.getBathrooms().intValue() + " Bath"));
        area_lot.setText((house.getArea().intValue() + " Sqft/" + house.getLot()));
        year.setText(String.valueOf(house.getYear()));
        rent.setText("$" + (house.getEstRent().intValue()));
        hoa.setText("$" + house.getHoa().intValue());
        tv_desc.setText(house.getDescription());

        new FetchFixUpSimple(FixUpDetailsSimple.this, house).execute();

    }


    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_type);
        mapFragment.getView().setClickable(false);//disables map touches and zooms
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(FixUpDetailsSimple.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        type_map = googleMap;
        type_map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.type_map));
        type_map.getUiSettings().setMapToolbarEnabled(false);
        type_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(house.getLatitude(), house.getLongitude()), 11f));
        type_map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(house.getLatitude(), house.getLongitude()), 11f));
        type_map.getUiSettings().setAllGesturesEnabled(false);
        type_map.getUiSettings().setZoomControlsEnabled(true);
        map_reset();
        map_home();
    }


    public void setNearbyRestaurants() {
        //to get nearby restaurants
        String Restaurant = "restaurant";
        //mMap.clear();
        Log.d("onClick", "Button is Clicked with restaurant");
        String url = getUrl(house.getLatitude(), house.getLongitude(), Restaurant);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = type_map;
        DataTransfer[1] = url;
        DataTransfer[2] = Restaurant;
        Log.d("onClick", url);

        //return nearby places to a coordinate corresponding to a house
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(this);
        getNearbyPlacesData.execute(DataTransfer);
    }

    public void setNearbyHospitals() {
        //to get nearby hospitals
        String Hospital = "hospital";
        Log.d("onClick", "Button is Clicked with hospital");
        String url = getUrl(house.getLatitude(), house.getLongitude(), Hospital);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = type_map;
        DataTransfer[1] = url;
        DataTransfer[2] = Hospital;
        Log.d("onClick", url);
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(this);
        getNearbyPlacesData.execute(DataTransfer);
    }

    public void setNearbyCreeks() {
        //to get nearby creeks
        String Creek = "natural_feature";
        Log.d("onClick", "Button is Clicked with creeks");
        String url = getUrl(house.getLatitude(), house.getLongitude(), Creek);
//        Log.d("creek_url",url);
        Object[] DataTransfer = new Object[3];
        DataTransfer[0] = type_map;
        DataTransfer[1] = url;
        DataTransfer[2] = Creek;
        Log.d("onClick", url);
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(this);
        getNearbyPlacesData.execute(DataTransfer);
    }

    public String getUrl(double latitude, double longitude, String nearbyPlace) {
        int PROXIMITY_RADIUS = 10000;

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);

        switch (nearbyPlace) {
            case "natural_feature":
                googlePlacesUrl.append("&keyword=creek");
                break;
        }
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBYDl0hd5HfS8CKWD8mK2bFLsb7MWvLo0Q");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }


    private void map_reset() {
        type_map.clear();
        markerList.clear();
        type_map.setOnMarkerClickListener(null);
        map_home();

    }

    private void map_home() {
        type_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(house.getLatitude(), house.getLongitude()), 11f));
        type_map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(house.getLatitude(), house.getLongitude()), 13f));


        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(house.getLatitude(), house.getLongitude()))
                .alpha(0.7f)
                .icon(bitmapDescriptorFromVector(getBaseContext(), R.drawable.ic_house_option1));
        type_map.addMarker(markerOptions);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.trans_temp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(20, 25, vectorDrawable.getIntrinsicWidth() + 50, vectorDrawable.getIntrinsicHeight() + 50);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    class FetchFixUpSimple extends AsyncTask<String, Void, Void> {

        String data = "";
        HouseDetailsDesc house;
        ArrayList<HouseDetailsDesc> street_house_list, radius_house_list;
        //double[] remodel_table = {0,0,0};//[0]estimate, [1]estimate after area expand,[2] proposed area of expansion
        StringBuilder build_stat = new StringBuilder();
        private Context details_context;

        public FetchFixUpSimple(Context context, HouseDetailsDesc house_received) {
            details_context = context;
            house = house_received;
        }


        @Override
        protected Void doInBackground(String... strings) {


            try {
                String a = house.getAddress();
                System.out.println("FetchDetails address: " + a);

                if (a.contains("#")) {
                    String[] splitAddress = a.split("# ");
                    a = splitAddress[1];
                } else if (a.contains(",")) {
                    String[] splitAddress = a.split(",");
                    a = splitAddress[0];
                } else {
                    a = strings[0];
                    a.replace(" ", "%20");
                }

                URL url = new URL("http://129.174.126.176:8080/api/housing/Details/Address=" + a + "/0");
                System.out.println("URL for getting address details: " + url);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    data = data + line;
                }
                JSONArray JA = new JSONArray(data);

                for (int i = 0; i < JA.length(); i++) {
                    JSONObject JO = (JSONObject) JA.get(i);
                    cooling = (String) JO.get("Cooling");
                    heating = (String) JO.get("Heating");
                    parking = (String) JO.get("Parking");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    street_house_list = get_street_match(house.getAddress());
                    radius_house_list = get_radius_match(house);
                }
            });
            t1.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            Log.d("fixup_debug", house.getHisory2String());


            tv_cooling.setText(cooling);
            tv_heating.setText(heating);
            tv_parking.setText(parking);

            float max_ratio = 0;
            HouseDetailsDesc temp_expand = null;
            if (street_house_list.size() != 0) {
                for (HouseDetailsDesc iter : street_house_list) {
                    if (!iter.getLot().equals("0")) {
                        float percent_diff = what_percent(convert_lot(house), convert_lot(iter));
                        System.out.println("@Match" + percent_diff);
                        if (percent_diff > 50 || percent_diff < 150 && iter.getArea() < convert_acre2sqft(convert_lot(iter))) {
                            float ratio = (float) Math.round(iter.getArea()) / convert_acre2sqft(convert_lot(iter));
                            if (max_ratio < ratio) {
                                temp_expand = iter;
                                max_ratio = ratio;
                            }
                        }
                    }
                }
            }
            double proposed_area = 0, proposed_final = 0;
            if (max_ratio > (float) Math.round(house.getArea() / convert_acre2sqft(convert_lot(house)))) {
                proposed_area = max_ratio * convert_acre2sqft(convert_lot(house));
            }

            if (proposed_area > house.getArea() && !(proposed_area > house.getArea() * 2)) {
                proposed_final = proposed_area;
            }



            double regression_radius_est = 0, regression_zip_est = 0;
            DecimalFormat formatter = new DecimalFormat("#,###");
            if (proposed_final > house.getArea()) {
                house.setArea(proposed_area);
                regression_radius_est = regression_radius(house);
                regression_zip_est = FixUpInit.group_calc(FixUpInit.unique_list, FixUpInit.uniques_houses_2d, house);

                if (regression_radius_est > house.getPrice()) {

                    tv_remodel_main.setText("After Remodeling Value: " + formatter.format(Math.round(regression_radius_est)) + "$");
                    build_stat.append("\nFor a proposed extended area of: " + (int) Math.round(house.getArea()) + " SqFt");
                    build_stat.append("\nAs an observation from: " + temp_expand.getAddress() + " " + "with an expansion ratio of: " + max_ratio);
                    build_stat.append("\nBased on Regression by Radius of Remodelled houses:");
                    for (HouseDetailsDesc temp_house : radius_house_list) {
                        String address = temp_house.getAddress().substring(0, temp_house.getAddress().indexOf(","));
                        build_stat.append("\n" + FixUpAdapter.format(Math.round(temp_house.getPrice())) + "$ \t" + Math.round(temp_house.getArea()) + "Sqft " + details_context.getString(R.string.tab) + details_context.getString(R.string.tab) + address);
                    }

                    //System.out.println("@Match_expand" + build_stat);

                    tv_remodel_desc.setText(build_stat);
//                    remodel_expand.setVisibility(View.VISIBLE);
                    Log.d("excel_log", (int) Math.round(regression_radius_est) + "        " + "Radius Regression Expansion" + "        " + proposed_area);
                } else {
                    tv_remodel_main.setText("After Remodeling Value: " + formatter.format(Math.round(regression_zip_est)) + "$");
                    build_stat.append("\nFor a proposed extended area of: " + (int) Math.round(house.getArea()) + " SqFt");
                    build_stat.append("\nAs an observation from: " + temp_expand.getAddress() + " " + "with an expansion ratio of: " + max_ratio);
                    build_stat.append("\nBased on Regression by Zip of Remodelled houses");
                    tv_remodel_desc.setText(build_stat);
//                    remodel_expand.setVisibility(View.VISIBLE);
                    Log.d("excel_log", (int) Math.round(regression_zip_est) + "        " + "Zip Regression Expansion" + "        " + proposed_area);

                }
            } else {
                regression_radius_est = regression_radius(house);
                regression_zip_est = FixUpInit.group_calc(FixUpInit.unique_list, FixUpInit.uniques_houses_2d, house);
                if (regression_radius_est > house.getPrice()) {
                    //FixUpDetails.f_fixup_est.setText("After Remodeling Value: "+(int) Math.round(regression_zip_est));
                    tv_remodel_main.setText("After Remodeling Value: " + formatter.format(Math.round(regression_radius_est)) + "$");
                    build_stat.append("\nBased on Regression by Radius of Remodelled houses:");
                    for (HouseDetailsDesc temp_house : radius_house_list) {
                        String address = temp_house.getAddress().substring(0, temp_house.getAddress().indexOf(","));
                        build_stat.append("\n" + FixUpAdapter.format(Math.round(temp_house.getPrice())) + "$ \t" + Math.round(temp_house.getArea()) + "Sqft " + details_context.getString(R.string.tab) + details_context.getString(R.string.tab) + address);
                    }
                    //System.out.println("@Match_expand" + build_stat);
                    tv_remodel_desc.setText(build_stat);
//                    remodel_expand.setVisibility(View.VISIBLE);
                    Log.d("excel_log", (int) Math.round(regression_radius_est) + "        " + "Radius Regression");

                } else {
                    tv_remodel_main.setText("After Remodeling Value: " + formatter.format(Math.round(regression_zip_est)) + "$");
                    build_stat.append("\nBased on Regression by Zip of Remodelled houses");
                    //FixUpDetails.f_remodel_desc.setText(build_stat);
                    tv_remodel_desc.setText(build_stat);
//                    remodel_expand.setVisibility(View.VISIBLE);
                    Log.d("excel_log", (int) Math.round(regression_zip_est) + "        " + "Zip Regression");
                }
            }
        }


        private double regression_radius(HouseDetailsDesc house) {
            if (radius_house_list.size() != 0) {
                SimpleRegression regression = new SimpleRegression();
                for (HouseDetailsDesc temp : radius_house_list) {
                    regression.addData(temp.getArea(), temp.getPrice());
                    Log.d("radius_est", "" + temp.getArea() + "  " + temp.getPrice());
                }
                //convert_lot

                double prediction = regression.predict(house.getArea());
                double return_val = FixUpInit.get_lot_added_est(house, prediction);
                Log.d("radius_est", "est: " + return_val);
                return return_val;
            }
            return 0;
        }

        String[] remodel = {"remodeled", "renovate", "updated"};

        ArrayList<HouseDetailsDesc> get_radius_match(HouseDetailsDesc get_house) {
            ArrayList<HouseDetailsDesc> list = new ArrayList<>();

            //http://129.174.126.176:8080/api/housing/Ra=3/Lati=37.208677/Longi=-121.857913/Type=Townhouse
            System.out.println("url_radius: " + "http://129.174.126.176:8080/api/housing/Ra=2/Lati=" + get_house.getLatitude() + "/Longi=" + get_house.getLongitude() + "/Type=" + get_house.getType());
            String link = "http://129.174.126.176:8080/api/housing/Ra=2/Lati=" + get_house.getLatitude() + "/Longi=" + get_house.getLongitude() + "/Type=" + get_house.getType();
            Log.d("radius_tag", "Wait here on debug");

            try {
                URL url = new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                JSONArray jsonArray = new JSONArray(builder.toString());
                Log.d("API_fetch", "Length of array" + jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject history = jsonArray.getJSONObject(i);

                    JSONArray temp_house = history.getJSONArray("History");
                    //Log.d("API_fetch_radius", "Value Of Iterator"+i);
                    for (int j = 0; j < temp_house.length(); j++) {
                        JSONObject house = temp_house.getJSONObject(j);
                        String Address = house.optString("Address");
                        String Type = house.optString("Type");
                        String Locality = house.optString("Locality");
                        String State = house.optString("State");
                        String ZipCode = house.optString("ZipCode");
                        Double Price = house.isNull("Price") ? 0 : house.getDouble("Price");
                        Double Area = house.isNull("AreaSpace_SQFT") ? 0 : house.getDouble("AreaSpace_SQFT");
                        Double EstRent = house.isNull("PredictionRent") ? 0 : house.getDouble("PredictionRent");
                        Double Bedroom = house.isNull("Bedrooms") ? 0 : house.getDouble("Bedrooms");
                        Double Bathroom = house.isNull("Bathrooms") ? 0 : house.getDouble("Bathrooms");
                        Double lat = house.optDouble("Latitude");
                        Double lng = house.optDouble("Longitude");
                        String Lot = house.isNull("Lot") || house.optString("Lot").equals("No Data") || house.optString("Lot").equals("N/A") ? "0" : house.optString("Lot");
                        String temp_house_year = house.isNull("YearBuilt") ? "0" : house.optString("YearBuilt");
                        String hoa_temp = house.isNull("HOAFee") ? "0" : house.getString("HOAFee");
                        Double hoa = 0.0;
                        hoa_temp = (hoa_temp.replaceAll("[^0-9]", ""));
                        if (hoa_temp != null && !hoa_temp.equals("")) {
                            hoa = Double.parseDouble(hoa_temp);
                        }
                        String Description = house.optString("Description");
                        int year = 0;
                        if (temp_house_year.equals("0") || temp_house_year.equals("No Data")) {
                            year = 0;
                        } else {
                            year = Integer.parseInt(temp_house_year);
                        }
                        if (Area != 0 && Price > 0) {
                            //System.out.println("@Match_radius" + "Nope");
                            HouseDetailsDesc temp = new HouseDetailsDesc(Type, Address, Locality, State, ZipCode, Price, Area, EstRent, Bedroom, Bathroom, lat, lng, Lot, year, hoa, Description);
                            if (!temp.getAddress().equals(get_house.getAddress())) {
                                String description = temp.getDescription().toLowerCase();
                                for (String x : remodel) {
                                    if (description.contains(x)) {
                                        float diff_lot = what_percent(convert_lot(temp), convert_lot(get_house));
                                        if (diff_lot < 30 && diff_lot > -30) {
                                            //Log.d("lot_tag","Difference is: \t"+diff_lot+"\t");
                                            list.add(temp);
                                            break;
                                        }
                                        //System.out.println("tagged\t" + " Keywords Found = "+ x+ "\tDesc: "+description );

                                    }
                                }
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//        for (HouseDetailsDesc x : list) {
//            System.out.println("@Match_radius" + x.getAddress());
//        }
//        System.out.println("@Match_radius" + list.size());
            return list;
        }

        ArrayList<HouseDetailsDesc> get_street_match(String address_string) {
            ArrayList<HouseDetailsDesc> list = new ArrayList<>();
            LocalityInfo loc = new LocalityInfo();
            String[] splitUserInput;


            splitUserInput = address_string.split("\\s*,\\s*");
//        System.out.println("street_match "+splitUserInput.length);
//        System.out.println("street_match 0 :"+splitUserInput[0]+" 1 :"+splitUserInput[1]+" 2 :"+splitUserInput[2]);
//        for(String x : splitUserInput) {
//        System.out.println(x+"  ");
//        }

            String street = "";
            if (splitUserInput[0] != null) {
                String[] temp;
                temp = splitUserInput[0].split(" ");//temp[1] and on will contain the street since we split the plot number and the remaining
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < temp.length; i++) {
                    if (i < temp.length - 1) {
                        builder.append(temp[i] + " ");
                    } else {
                        builder.append(temp[i]);
                    }
                }
                street = builder.toString();
            }
//        System.out.println("street_tag for current: #" + street+"#");

            if (splitUserInput[1] != null)
                loc.city = splitUserInput[1];

            String[] splitLoc = splitUserInput[2].split(" ");

            if (splitLoc[0] != null)
                loc.state = splitLoc[0];

            if (splitLoc.length >= 2 && splitLoc[1] != null)
                loc.zipcode = splitLoc[1];

//        String[] splitAddress = address.split(" # ");
            System.out.println("http://129.174.126.176:8080/api/housing/Street/State=" + loc.state + "/Locality=" + loc.city + "/Address=" + street);
            String link = "http://129.174.126.176:8080/api/housing/Street/State=" + loc.state + "/Locality=" + loc.city + "/Address=" + street;
//        Log.d("street_tag", "Wait here on debug" + "Address ");        Log.d("street_tag", "Wait here on debug" + "Address ");

            try {
                URL url = new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder builder = new StringBuilder();
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                JSONArray jsonArray = new JSONArray(builder.toString());
                Log.d("API_fetch", "Length of array" + jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject history = jsonArray.getJSONObject(i);

                    JSONArray temp_house = history.getJSONArray("History");
                    //Log.d("API_fetch", "Value Of Iterator"+i);
                    for (int j = 0; j < temp_house.length(); j++) {
                        JSONObject house = temp_house.getJSONObject(j);
                        String Address = house.optString("Address");
                        String Type = house.optString("Type");
                        String Locality = house.optString("Locality");
                        String State = house.optString("State");
                        String ZipCode = house.optString("ZipCode");
                        Double Price = house.isNull("Price") ? 0 : house.getDouble("Price");
                        Double Area = house.isNull("AreaSpace_SQFT") ? 0 : house.getDouble("AreaSpace_SQFT");
                        Double EstRent = house.isNull("PredictionRent") ? 0 : house.getDouble("PredictionRent");
                        Double Bedroom = house.isNull("Bedrooms") ? 0 : house.getDouble("Bedrooms");
                        Double Bathroom = house.isNull("Bathrooms") ? 0 : house.getDouble("Bathrooms");
                        Double lat = house.optDouble("Latitude");
                        Double lng = house.optDouble("Longitude");
                        String Lot = house.isNull("Lot") || house.optString("Lot").equals("No Data") || house.optString("Lot").equals("N/A") ? "0" : house.optString("Lot");
                        String temp_house_year = house.isNull("YearBuilt") ? "0" : house.optString("YearBuilt");
                        String hoa_temp = house.isNull("HOAFee") ? "0" : house.getString("HOAFee");
                        Double hoa = 0.0;
                        hoa_temp = (hoa_temp.replaceAll("[^0-9]", ""));
                        if (hoa_temp != null && !hoa_temp.equals("")) {
                            hoa = Double.parseDouble(hoa_temp);
                        }
                        String Description = house.optString("Description");
                        int year = 0;
                        if (temp_house_year.equals("0") || temp_house_year.equals("No Data")) {
                            year = 0;
                        } else {
                            year = Integer.parseInt(temp_house_year);
                        }
                        if (Area != 0) {
                            HouseDetailsDesc temp = new HouseDetailsDesc(Type, Address, Locality, State, ZipCode, Price, Area, EstRent, Bedroom, Bathroom, lat, lng, Lot, year, hoa, Description);
                            if (!temp.getAddress().equals(address_string)) {
                                list.add(temp);
                            }
                        }


                    }


                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//        for (HouseDetailsDesc x : list) {
//            System.out.println("@Match" + x.getAddress());
//        }
//        System.out.println("@Match" + list.size());
            return list;
        }


        float convert_lot(HouseDetailsDesc housedet) {//function converts any given format into Acres
            float lot_size = 0;
            String lot = housedet.getLot().replace(",", "");
            String[] splitStr = lot.trim().split("\\s+");
            //Log.d("split", "length is: " + splitStr.length + " String is: " + lot);
            if (splitStr.length > 1) {
                //Log.d("split", "Str 1: " + splitStr[0] + " Str 2: " + splitStr[1]);
                if (splitStr[1].equals("sqft")) {
                    lot_size = (float) ((Long.valueOf(splitStr[0]) * 0.000022956841138659));
                } else if (splitStr[1].equals("acres") || splitStr[1].equals("acre")) {
                    lot_size = Float.valueOf(splitStr[0]);
                } else {
                    lot_size = 0;
                }
            } else {
                lot_size = 0;
            }
            return lot_size;
        }

        float what_percent(float a, float b) {
            float result = 0;
            result = ((b - a) * 100) / a;

            return (int) result;
        }

        float convert_acre2sqft(float acre) {
            float sqft = acre * 43560;
            return sqft;
        }

    }
}

