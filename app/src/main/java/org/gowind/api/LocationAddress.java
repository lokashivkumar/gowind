package org.gowind.api;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shiv.loka on 9/6/16.
 */

public class LocationAddress {
    private static final int NUMBER_OF_ADDRESSES = 3;
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    private static final String TAG = LocationAddress.class.getSimpleName();
    private String errorMessage;
    public List<Address> getAddressFromLocation(Context context, Location location) throws IOException {

        List<Address> addresses = new ArrayList<>();

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 3);
        } catch (IOException ioException) {
            errorMessage = "Service Not Available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid Latitude or Longitude Used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() + ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }


        if(addresses != null && addresses.size() > 0)
            return addresses;

        return null;
    }
}
