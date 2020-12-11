package com.example.datamining;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import net.sharewire.googlemapsclustering.ClusterItem;

class CustomClusterItemHouse implements ClusterItem {

    private final LatLng location;
    private final HouseDetails house;

    CustomClusterItemHouse(@NonNull LatLng location, HouseDetails house) {
        this.location = location;
        this.house = house;
    }

    @Override
    public double getLatitude() {
        return location.latitude;
    }

    @Override
    public double getLongitude() {
        return location.longitude;
    }

    public HouseDetails getHouse() {
        return house;
    }


    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
