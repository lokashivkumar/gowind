package org.gowind.api;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shiv.loka on 9/6/16.
 */

public class LocationAddress {
    private static final int NUMBER_OF_ADDRESSES = 1;

    public List<Address> getAddressFromLocation(Context context, Location location) {
        List<Address> addresses = new ArrayList<>();

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), NUMBER_OF_ADDRESSES);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }


}
