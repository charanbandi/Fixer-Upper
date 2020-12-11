package com.example.datamining;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.sharewire.googlemapsclustering.Cluster;
import net.sharewire.googlemapsclustering.ClusterManager;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity;

public class FixUpInit extends AsyncTask<Void, Void, Void> {
    public static float avg = 0;
    public static List<Integer> unique_list = new ArrayList<Integer>();
    public static ArrayList<ArrayList<HouseDetailsDesc>> uniques_houses_2d = new ArrayList<ArrayList<HouseDetailsDesc>>();
    Context context_init;
    String location;
    int category, house_type;
    String[] str_house_type = {"Single Family", "Townhouse", "Condo"};
    ArrayList<HouseDetailsDesc> fixup_houselist = new ArrayList<>();
    String gethouses_url = "http://129.174.126.176:8080/api/housing/";
    String status = "rent";
    ArrayList<HouseDetailsDesc> fixup_type_remodel = new ArrayList<>();
    String[] remodel = {"remodel", "renovate", "updated"};
    ArrayList<HouseDetailsDesc> fixup_type_fixerupper = new ArrayList<>();
    //String[] fixerupper = {"TLC","tlc"};
    String[] fixerupper = {"tlc", "with tlc", "with some tlc", "need tlc", "needs tlc", "need some tlc",
            "needs some tlc", "needs lot tlc", "needs lot tlc", "need little tlc", "needs little tlc", "need cosmetic tlc", "needs cosmetic tlc", "tender love care",
            "tender loving care", "tender, love care", "tender love and care", "tender", "need upgrade", "with space for", "needs some work", "needs upgrades",
            "need upgrades", "needs upgrade", "needs repair", "needs repairs", "needs repair", "needs repairs", "need paint", "needs paint", "need redecoration", "needs redecoration",
            "need redesign", "needs redesign", "needs reconstruction", "need reconstruction", "needs work", "need work", "needs maintenance", "need maintenance work",
            "lot development", "custom", "adu", "business opportunity", "investment opportunity", "redevelopment opportunity", "been in the same family", "for investing",
            "for investors", "care", "low price", "price reduce", "price reduced", "reduced price", "reduce price", "original condition", "not working", "potential to", "great potential",
            "good potential", "huge potential", "tons of potential", "extend the", "space to extend", "not upgraded", "contractor", "bring contractor", "contractors gem", "contractor gem",
            "contractor special", "contractors special", "use imagination", "accomplish with little", "accomplish with little paint", "can turn into", "cosmetic", "build equity", "build sweat equity",
            "room for", "room to customize", "possibility to add", "expansion opportunity", "plan expand", "plans to expand", "options to invest", "options to modify", "starter",
            "great starter", "good starter", "exciting business", "possibility to convert", "possibility to converted", "sold as is", "sold as is", "sold asis", "as is",
            "bankruptcy sale", "asap", "as soon as possible", "no repairs will be done", "great bones", "good bones", "diy", "do it yourself'er", "do-it-yourself", "diy buyers",
            "distress sale", "investment rental property", "investor special", "hgtv", "hgtv ideas", "listed under value", "under assessed value", "handyman", "handyman special", "opportunity"};



    String[] words_add = {"adu", "expansion", "extend", "project", "living unit", "extension"};


    public FixUpInit(Context context, String loc, int cat, int type) {
        super();
        this.context_init = context;
        house_type = type;
        location = loc;
        category = cat;
    }


    static double group_calc(List<Integer> unique_list, ArrayList<ArrayList<HouseDetailsDesc>> uniques_houses_2d, HouseDetailsDesc housedet) {

//        if(housedet.getAddress().contains("3114")||housedet.getAddress().contains("169")||housedet.getAddress().contains("254")||housedet.getAddress().contains("3813")){
//            Log.d("estimate","address:" +housedet.getAddress());
//        }

        int zip = Integer.parseInt(housedet.getAddress().substring(housedet.getAddress().length() - 5));
        int type = 0; //0 = none, 1 = regression, 2 = average price/sqft less than two data point, 3 = average price/sqft less slope, 4 = average price/sqft
        double estimate = 0;
        boolean found = false;
        int found_index = -1;
        for (int i = 0; i < unique_list.size(); i++) {
            if (unique_list.get(i) == zip) {
                found_index = i;
                found = true;
                break;
            }
        }
        if (found == true) {

            ArrayList obj = uniques_houses_2d.get(found_index);
            ArrayList<HouseDetailsDesc> temp = obj;
            SimpleRegression regression = new SimpleRegression();
            for (HouseDetailsDesc house : temp) {
                regression.addData(house.getArea(), house.getPrice());
            }
            double slope = regression.getSlope();
            if (temp.size() < 2) {
                estimate = get_average(uniques_houses_2d, housedet);
                //Log.d("estimate", housedet.getAddress()+"Based on an average since less points to regression");
                type = 2;
            } else {
                if (slope < (-100)) {
                    estimate = get_average(uniques_houses_2d, housedet);
                    //Log.d("estimate", housedet.getAddress()+"Based on an average since slope -100");
                    type = 3;
                } else {
                    estimate = regression.predict(housedet.getArea());
                    //Log.d("estimate", housedet.getAddress()+"Based on a regression prediction");
                    type = 1;
                }
            }
        } else {
            estimate = get_average(uniques_houses_2d, housedet);
            //Log.d("estimate", housedet.getAddress()+"Based on an average since no data found");
            type = 4;
        }

        estimate = get_lot_added_est(housedet, estimate);// adds the lot size estimate weight added

        if (estimate < housedet.getPrice() && (type == 2 || type == 3 || type == 4)) {//standard increase of 20 percent if no similar values are found nearby
            Log.d("value_debug", "in the function with type: " + type);
            estimate = (0.2 * housedet.getPrice()) + housedet.getPrice();
        }
//        else if(estimate<housedet.getPrice() && type == 1) {
//            double percentage_diff = ((housedet.getPrice()-estimate)/(((housedet.getPrice()+estimate)/2)))*100;
//            Log.d("value_debug", "Percentage Diff = "+percentage_diff);
//        }

            /*
            https://architectsla.com/how-much-value-does-adu-add/#:~:text=An%20ADU%20Will%20Add%20to,That's%20staggering!
            When done right, detached ADUs, in particular, have the potential to increase your property value by a whopping 20-30%.
            Even more astonishing, your ADU can give you a practically instant return – we’ve seen some projects add double to the property value compared to the building costs.
             */

        String description = housedet.getDescription().toLowerCase();
        if (description.contains("adu") || description.contains("living unit") || description.contains("lots of space")) {
            estimate = (0.3 * estimate) + estimate;
        }


        return estimate;
    }

    static double get_lot_added_est(HouseDetailsDesc housedet, double estimate) {
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
        if (lot_size > 0.1) {
            estimate = (((15 * lot_size) / 100) * estimate) + estimate;//here the first value is the 15 added per acre
        }
        return estimate;
    }

    private static double get_average(ArrayList<ArrayList<HouseDetailsDesc>> uniques_houses_2d, HouseDetailsDesc housedet) {

        ArrayList<Double> avg_vals_area = new ArrayList<>();

        for (ArrayList obj : uniques_houses_2d) {
            ArrayList<HouseDetailsDesc> temp = obj;
            if (temp.size() > 1) {
                SimpleRegression regression = new SimpleRegression();
                for (HouseDetailsDesc house : temp) {
                    regression.addData(house.getArea(), house.getPrice());
                }

                double predict = regression.predict(housedet.getArea());
                if (!Double.isNaN(predict)) {
                    avg_vals_area.add(regression.predict(housedet.getArea()));
                }
            }
        }

        double sum = 0, average_value = 0;
        ;
        for (Double val : avg_vals_area) {
            sum += val;
        }
        average_value = sum / avg_vals_area.size();
        return average_value;
    }

    private void cluster_houses(final ArrayList<HouseDetailsDesc> house_list, int type) {
        for (HouseDetailsDesc house : house_list) {
            String description = house.getDescription().toLowerCase();
            for (String x : remodel) {
                if (description.contains(x)) {
                    //System.out.println("tagged\t" + " Keywords Found = "+ x+ "\tDesc: "+description );
                    //Log.d("remodel_tag1","\t"+house.getAddress()+"\t");
                    fixup_type_remodel.add(house);
                }
            }

            outerloop:
            {     //Breaks two for loops at ones
                for (String y : fixerupper) {
                    if (description.contains(y)) {
                        if (y.equals("opportunity") || y.equals("custom") || y.equals("potential to") || y.equals("room for")) {
                            for (String z : words_add) {
                                if (description.contains(z) && !description.contains("remodel") && !description.contains("renovate")) {
                                    System.out.println(house.getAddress() + "Keywords = " + y + " " + z + "\t\t\tDesc: \t\t\t" + description);

                                    fixup_type_fixerupper.add(house);
                                    break outerloop;
                                }
                            }
                        } else {
                            if (!description.contains("remodel") && !description.contains("renovate") && !description.contains("updated") && !description.contains("replaced") && !description.contains("hardwood") && !description.contains("new dry walls") && !description.contains("luxurious") && !description.contains("restored") && !description.contains("new exterior") && !description.contains("new interior") && !description.contains("newer bathroom") && !description.contains("new bathroom") && !description.contains("new bedroom") && !description.contains("newer windows") && !description.contains("brand new built")) {
                                System.out.println(house.getAddress() + "Keywords = " + y + "\t\t\tDesc: \t\t\t" + description);
                                fixup_type_fixerupper.add(house);
                                break outerloop;
                            }

                        }
                    }

                }
            }


        }

        zip_calc(fixup_type_remodel);

        System.out.println("tagged\t" + "Size of Total Houses: \t" + fixup_houselist.size());
        System.out.println("tagged\t" + "Size of Remodelled Houses: \t" + fixup_type_remodel.size());
        System.out.println("tagged\t" + "Size of Fixer Upper Houses: \t" + fixup_type_fixerupper.size());

        double temp_1 = 0;
        int count = 0;

        for (HouseDetailsDesc house : fixup_type_remodel) {
            temp_1 += house.getPrice() / house.getArea();
            count += 1;
        }
        avg = (float) (temp_1 / count);


//        double temp_2 = 0;
//        int count_th = 0;
//
//        for (HouseDetailsDesc house : fixup_type_th_remodel) {
//            temp_2 += house.getPrice() / house.getArea();
//            count_th += 1;
//        }
//        avg_th = (float) (temp_2 / count_th);
//
//        double temp_3 = 0;
//        int count_cn = 0;
//
//        for (HouseDetailsDesc house : fixup_type_cn_remodel) {
//            temp_3 += house.getPrice() / house.getArea();
//            count_cn += 1;
//        }
//        avg_cn = (float) (temp_3 / count_cn);

        Log.d("tagged\t", "avg of remodel in this locality SingleFamily Price/Sqft = " + avg);
//        Log.d("tagged\t", "avg of remodel in this locality TownHouse Price/Sqft = " + avg_th);
//        Log.d("tagged\t", "avg of remodel in this locality Condo Price/Sqft = " + avg_cn);


//        for (HouseDetailsDesc house : fixup_type_fixerupper) {
//            Log.d("excel_log", house.getAddress() + "                    " + Math.round(house.getPrice()) + "                    " + Math.round(house.getArea()));
//        }
    }

    private void zip_calc(ArrayList<HouseDetailsDesc> remodel_list) {
        unique_list.clear();
        uniques_houses_2d.clear();
        List<Integer> zips_extracted = new ArrayList<Integer>();
        for (HouseDetailsDesc house : remodel_list) {
            int zip = Integer.parseInt(house.getAddress().substring(house.getAddress().length() - 5));

            zips_extracted.add(zip);
        }
        HashSet<Integer> uniques = new HashSet<>(zips_extracted);//sorts all number and only uses unique numbers (sets)


        for (Integer x : uniques) {
            //System.out.println("Zip is: "+x);
            ArrayList<HouseDetailsDesc> temp_houselist = new ArrayList<>();
            for (HouseDetailsDesc house : remodel_list) {
                if (Integer.parseInt(house.getAddress().substring(house.getAddress().length() - 5)) == x) {     //Getting ZIP from last 5 characters of address string
                    temp_houselist.add(house);
                }
            }
            uniques_houses_2d.add(temp_houselist);
        }
        unique_list = new ArrayList<>(uniques);

//        int i=0;
//        for(ArrayList obj:uniques_houses_2d){
//            ArrayList<HouseDetailsDesc> temp = obj;
//            for(HouseDetailsDesc house : temp){
//                System.out.print("\n"+house.getAddress()+" from zip: "+unique_list.get(i));
//            }
//            i++;
//        }
//        group_calc(unique_list,uniques_houses_2d);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String url_edited = get_url(location);
        String type_selected = "";
        switch (house_type) {
            case 0:
                type_selected = "Single Family";
                break;
            case 1:
                type_selected = "Townhouse";
                break;
            case 2:
                type_selected = "Condo";
                break;
        }
        Log.d("debug_type", "type is :" + type_selected);

        Log.d("API_fetch", "In Fetch");
        fixup_houselist.clear();
        int len = 0, count = 0;
        while (count == 0 || len == 500) {

            if (count < 11) {
                url_edited = url_edited.substring(0, url_edited.length() - 1) + count;
            } else {
                url_edited = url_edited.substring(0, url_edited.length() - 2) + count;
            }


            Log.d("API_fetch", "Edited URL = " + url_edited);
            try {
                URL url = new URL(url_edited);
                Log.d("API_fetch", "In Fetch1");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("API_fetch", "In Fetch2");
                InputStream inputStream = httpURLConnection.getInputStream();
                Log.d("API_fetch", "In Fetch3");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                Log.d("API_fetch", "In Fetch4");
                StringBuilder builder = new StringBuilder();
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                JSONArray jsonArray = new JSONArray(builder.toString());
                Log.d("API_fetch", "Length of array" + jsonArray.length());
                len = jsonArray.length();
                count++;


                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject house = jsonArray.getJSONObject(i);

                    //Log.d("API_fetch", "Value Of Iterator"+i);

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
                    Double lat = house.getDouble("Latitude");
                    Double lng = house.getDouble("Longitude");
                    String Lot = house.isNull("Lot") || house.optString("Lot").equals("No Data") || house.optString("Lot").equals("N/A") ? "0" : house.optString("Lot");
                    String temp_house_year = house.isNull("YearBuilt") ? "0" : house.optString("YearBuilt");
                    String hoa_temp = house.isNull("HOAFee") ? "0.0" : house.getString("HOAFee");
                    Double hoa = 0.0;
                    hoa_temp = (hoa_temp.replaceAll("[^0-9]", ""));
                    if (!hoa_temp.equals("")) {
                        hoa = Double.parseDouble(hoa_temp);
                    }

                    String Description = house.optString("Description");


                    int year = 0;
                    if (temp_house_year.equals("0") || temp_house_year.equals("No Data")) {
                        year = 0;
                    } else {
                        year = Integer.parseInt(temp_house_year);
                    }
                    HouseDetailsDesc newHouse = new HouseDetailsDesc(Type, Address, Locality, State, ZipCode, Price, Area, EstRent, Bedroom, Bathroom, lat, lng, Lot, year, hoa, Description);
                    if (Price != 0 && EstRent != 0.0 && Area != 0.0) {
                        if (newHouse.getType().equals(type_selected)) {
                            fixup_houselist.add(newHouse);
                            //Log.d("API_check", newHouse.getAddress());
                        }
                    }
                }
                Log.d("API_fetch", "In Fetch Single Family house list size = " + fixup_houselist.size());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        cluster_houses(fixup_houselist, house_type);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);


        setupListAdapter();
        FixUpMain.load_layout.setVisibility(View.GONE);

        //  Hint Builder, to describe data
        new GuideView.Builder(context_init)
                .setTitle("Hint: Details")
                .setContentText("Estimates shown here are an approximation\n Click on the specific cell in the list\n to get the most accurate values..")
                .setGravity(Gravity.auto) //optional
                .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
                .setTargetView(FixUpMain.property_list)
                .setContentTextSize(12)//optional
                .setTitleTextSize(14)//optional
                .build()
                .show();


        setup_cluster();
//        init_school();

    }


    public static List<Marker> ymarkers = new ArrayList<Marker>();


    private void setup_cluster() {
        net.sharewire.googlemapsclustering.ClusterManager<CustomClusterItemHouseDesc> clusterManager = new net.sharewire.googlemapsclustering.ClusterManager<>(context_init, FixUpMain.mMap);
        List<CustomClusterItemHouseDesc> clusterItems = new ArrayList<>();
        for (HouseDetailsDesc temp_house : fixup_type_fixerupper) {
            clusterItems.add(new CustomClusterItemHouseDesc(new LatLng(temp_house.getLatitude(), temp_house.getLongitude()), temp_house));
            clusterManager.setItems(clusterItems);
        }

        clusterManager.setCallbacks(new ClusterManager.Callbacks<CustomClusterItemHouseDesc>() {
            @Override
            public boolean onClusterClick(@NonNull Cluster<CustomClusterItemHouseDesc> cluster) {
                Log.d("click_registered", "onClusterClick Type 1");
                return false;
            }

            @Override
            public boolean onClusterItemClick(@NonNull CustomClusterItemHouseDesc clusterItem) {
                Log.d("click_registered", "onClusterItemClick Type 2" + clusterItem.getHouse().getAddress());

                //changed here simple
                Intent i = new Intent(context_init, FixUpDetailsSimple.class);
                i.putExtra("HouseDetails", clusterItem.getHouse());
                context_init.startActivity(i);

                return false;
            }
        });
        FixUpMain.mMap.setOnCameraIdleListener(clusterManager);

    }

    private void setupListAdapter() {

        Collections.sort(fixup_type_fixerupper, HouseDetailsDesc.p1);

//        for (HouseDetailsDesc house : fixup_type_fixerupper) {
//            Log.d("excel_log", house.getAddress() + "                    " + Math.round(house.getPrice()) + "                    " + Math.round(house.getArea()));
//        }
        FixUpAdapter fixup_adapter = new FixUpAdapter(context_init, R.layout.fixup_cell, fixup_type_fixerupper);
        FixUpMain.property_list.setAdapter(fixup_adapter);


        FixUpMain.property_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //changed here simple
                Intent i = new Intent(context_init, FixUpDetailsSimple.class);
                i.putExtra("HouseDetails", fixup_type_fixerupper.get(position));
                context_init.startActivity(i);


            }

        });
        FixUpMain.property_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //final MarkerOptions options = new MarkerOptions();

                //clears the existing map
//                FixUpMain.mMap.clear();
                LatLng location = new LatLng(fixup_type_fixerupper.get(position).getLatitude(), fixup_type_fixerupper.get(position).getLongitude());
//                options.position(location)
//                        .title(String.valueOf(fixup_type_fixerupper.get(position).getAddress()))
//                        .alpha(1f)
//                        .icon(BitmapDescriptorFactory.defaultMarker(200f));
//                FixUpMain.mMap.addMarker(options);
                FixUpMain.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
                FixUpMain.mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
                FixUpMain.mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

                return true;
            }
        });

    }

    private String get_url(String location) {
        String query_url = "";
        int numSpaces = 0;

        LocalityInfo loc = new LocalityInfo();
        String[] splitUserInput;
        if (location != null) {
            splitUserInput = location.split(", ");
        } else {
            Log.d("fix_up", "Error no location found");
            return "";
        }

        if (splitUserInput[0] != null)
            loc.city = splitUserInput[0];

        if (splitUserInput[2] != null)
            loc.country = splitUserInput[2];

        String[] splitLoc = splitUserInput[1].split(" ");

        if (splitLoc[0] != null)
            loc.state = splitLoc[0];

        if (splitLoc.length >= 2 && splitLoc[1] != null)
            loc.zipcode = splitLoc[1];

        String[] splitAddress = location.split(" # ");

        //to count the number of spaces in the selected string
        for (char c : location.toCharArray()) {
            if (c == ',') {
                numSpaces = numSpaces + 1;
            }
        }
        switch (numSpaces) {
            case 0:
                String state = splitUserInput[0];
                query_url = gethouses_url + "Status=" + status + "/State=" + state + "/" + 0;
                break;

            case 1: //state

                String state1 = splitUserInput[0];
                if (state1.equals("California")) {
                    state1 = "CA";
                }

                query_url = gethouses_url + "Status=" + status + "/State=" + state1 + "/" + 0;
                break;

            case 2: //locality
                String modifiedLocality1 = upperCaseWords(splitUserInput[0]);
                //query_url = GET_ALL_HOUSES1+"Coordinates/"+ "State=" + loc.state + "/Locality=" + modifiedLocality1 + "/" + 0;
                query_url = gethouses_url + "State=" + loc.state + "/Locality=" + modifiedLocality1 + "/" + 0;
                break;

            case 3: // address
                String address = splitUserInput[0];
                String state2 = splitUserInput[2].split(" ")[0];
                String modifiedLocality2 = upperCaseWords(splitUserInput[1]);
                if (address.matches(".*Drive.*"))
                    address = address.replace("Drive", "Dr");
                else if (address.matches(".*Court.*"))
                    address = address.replace("Court", "Ct");
                else if (address.matches(".*Road.*"))
                    address = address.replace("Road", "Rd");
                else if (address.matches(".*Street.*"))
                    address = address.replace("Street", "St");
                else if (address.matches(".*Lane.*"))
                    address = address.replace("Lane", "Ln");
                else if (address.matches(".*Avenue.*"))
                    address = address.replace("Avenue", "Ave");
                else if (address.matches(".*Circle.*"))
                    address = address.replace("Circle", "Cir");
                else if (address.matches(".*Terrace.*"))
                    address = address.replace("Terrace", "Ter");
                else
                    address = address;

                query_url = gethouses_url + "/State=" + state2 + "/Locality=" + modifiedLocality2 + "/" + address;

                break;


            default:
                System.out.println("numspaces value is not 0 1 or 2");
                break;
        }
        //query_url = query_url + "/" + 0;
        Log.d("fix_up", "Query url FixUP = " + query_url);
        return query_url;
    }

    public static String upperCaseWords(String sentence) {
        String[] words = sentence.replaceAll("\\s+", " ").trim().split(" ");
        StringBuilder newSentence = new StringBuilder();
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                String lower_case = word.substring(i, i + 1).toLowerCase();
                newSentence.append((i == 0) ? word.substring(i, i + 1).toUpperCase() :
                        (i != word.length() - 1) ? lower_case : lower_case.toLowerCase() + " ");
            }
        }
        return newSentence.toString().trim();
    }


    float convert_lot(HouseDetailsDesc housedet) {
        //converts any lot size to acres
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

    float convert_acre2sqft(float acre) {
        float sqft = acre * 43560;
        return sqft;
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
}

class LocalityInfo {
    String city = "";
    String state = "";
    String country = "";
    String zipcode = "";
}