package org.gowind.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by shiv.loka on 9/22/16.
 */

public class UserLocation {

    private String originLocation;
    private String destinationLocation;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    public String getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(String originLocation) {
        this.originLocation = originLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public LatLng getOriginLatLng() {
        return originLatLng;
    }

    public void setOriginLatLng(LatLng originLatLng) {
        this.originLatLng = originLatLng;
    }

    public LatLng getDestinationLatLng() {
        return destinationLatLng;
    }

    public void setDestinationLatLng(LatLng destinationLatLng) {
        this.destinationLatLng = destinationLatLng;
    }

    @Override
    public String toString() {
        return "User is at: " + originLocation + ": " + originLatLng.latitude + " \'"
                + originLatLng.longitude + " \"";
    }
}
