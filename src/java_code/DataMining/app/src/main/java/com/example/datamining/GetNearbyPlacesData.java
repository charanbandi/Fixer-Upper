package com.example.datamining;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;


//this class returns the nearby businesses close to a coordinate (of a house).
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap mMap;
    String url;
    String type;
    List<HashMap<String, String>> nearbyPlacesList;
    private Context current_context;
    public GetNearbyPlacesData(Context context) {
        this.current_context = context;
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesData", "doInBackground entered");
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            type = (String) params[2];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        //List<HashMap<String, String>> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);
        ShowNearbyPlaces(nearbyPlacesList);
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            switch (type) {
                case "restaurant":
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_restaurant));
                    break;
                case "hospital":
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_hospital));
                    break;
                case "school":
                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_map_school));
                    break;
                case "church":
                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_church));
                    break;
                case "store":
                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school));
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_store));
                    break;
                case "natural_feature":
                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_creek));
                    break;
//                case "input=highway&inputtype=textquery":
//                    markerOptions.icon(bitmapDescriptorFromVector(this.current_context, R.drawable.ic_highway));
//                    break;
            }
            mMap.addMarker(markerOptions);
            //move map camera
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.trans_temp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
