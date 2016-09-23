package org.gowind.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import org.gowind.R;
import org.gowind.api.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shiv.loka on 9/21/16.
 */

public class FetchAddressService extends IntentService {

    private static final String TAG = FetchAddressService.class.getSimpleName();
    private static final int NUMBER_OF_ADDRESSES = 3;
    protected ResultReceiver mReceiver;

    public FetchAddressService() {
        super("FetchAddressService");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FetchAddressService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(TAG, "In handle intent");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        List<Address> addressesList = null;
        String errorMessage = new String();
        try {
            addressesList = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    NUMBER_OF_ADDRESSES);
        } catch (IOException ioException) {
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addressesList == null || addressesList.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            ArrayList<String> stringAddresses = new ArrayList<>();
            for (Address address : addressesList) {
                stringAddresses.add(address.getAddressLine(0));
            }
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT, stringAddresses);
        }
    }

    private void deliverResultToReceiver(int resultCode, ArrayList<String> message) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}