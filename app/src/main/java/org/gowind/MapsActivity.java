package org.gowind;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.gowind.api.LocationAddress;
import org.gowind.util.PermissionUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    //TODO: Use butterknife


    private static final String LOGTAG = "Gowind-MapsActivity";
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    GoogleMap mMap;
    @BindView(R.id.requestButton) Button requestButton;
    @BindView(R.id.originAutoComplete) AutoCompleteTextView originAutoComplete;
    //AutoCompleteTextView originAutoComplete;
    @BindView(R.id.paymentsButton) ImageButton paymentsButton;
    @BindView(R.id.profileButton) ImageButton profileButton;
    @BindView(R.id.settingsButton) ImageButton settingsButton;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private boolean isMapReady;
    private Location mCurrentLocation;
    private Marker mMarker;
    private LinearLayout mapsLayout;
    private boolean mPermissionDenied = false;
    private List<String> stringAddressList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

    //    originAutoComplete = (AutoCompleteTextView) findViewById(R.id.originAutoComplete);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupBottomPanelButtonListeners();
    }

    private void setupBottomPanelButtonListeners() {

        originAutoComplete.setOnItemSelectedListener(this);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Your request has been created.", Toast.LENGTH_LONG).show();
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MapsActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        paymentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent paymentIntent = new Intent(MapsActivity.this, PaymentActivity.class);
                startActivity(paymentIntent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent userSettingsIntent = new Intent(MapsActivity.this, UserProfileActivity.class);
                //TODO : Create settings activity and pass intent
            }
        });
    }

    //TODO: Offload locationServices to a different Thread / asyncTask
    //TODO: Reduce weight on the main thread.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.updateProfile:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Log.i(LOGTAG, "User Menu selected");
                Intent userProfileIntent = new Intent(MapsActivity.this, UserProfileActivity.class);
                startActivity(userProfileIntent);
                return true;

            case R.id.addPayment:

                Log.i(LOGTAG, "Add payment selected");
                Intent userPaymentIntent = new Intent(MapsActivity.this, PaymentActivity.class);
                startActivity(userPaymentIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        Log.e(LOGTAG, "Disconnected from Google Locaiton services");
        super.onStop();
    }


    /** START MAP/LOCATION RELATED CODE **/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        isMapReady = true;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        enableMyLocation();
        return false;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOGTAG, "Inside on Connected method of Location Services");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
        mRequestingLocationUpdates = true;
        enableMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.w(LOGTAG, "A previous connection was suspended, reconnecting.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOGTAG, "Connection to Google Location Services Failed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        String hello = "I am in Location Changed";
        Log.i(LOGTAG, hello);
        new UpdateUILocationTask().execute(mCurrentLocation);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private class UpdateUILocationTask extends AsyncTask<Location, Integer, LatLng> {
        protected LatLng doInBackground(Location... locations) {
            Log.i(LOGTAG, "Inside Async task updating location address");
            List<Address> addressList;
            LatLng latLng = null;
            Location userLocation = locations[0];
            if (userLocation != null) {
                String lastLocationUpdateTime = DateFormat.getTimeInstance().format(new Date());

                Log.i(LOGTAG, userLocation.getLatitude() + " :: " +
                        userLocation.getLongitude() + " : update time: " +lastLocationUpdateTime);
                latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                try {
                    LocationAddress locAddress = new LocationAddress();
                    addressList = locAddress.getAddressFromLocation(MapsActivity.this, userLocation);

                    if (addressList != null && addressList.size() > 0) {
                        for (Address address : addressList) {
                            Log.i(LOGTAG, ":::" + address.getCountryCode() + address.getFeatureName());
                            stringAddressList.add(getAddressAsString(address));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mMarker != null) {
                                    mMarker.remove();
                                }
                                ArrayAdapter<String> autoCompleteTextViewAdapter =
                                        new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_dropdown_item_1line, stringAddressList);
                                autoCompleteTextViewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                originAutoComplete.setAdapter(autoCompleteTextViewAdapter);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return latLng;
        }

        /**
         * Helper method to convert a given Address Object to String -
         * returns Location, city, state, feature name, country code, postal code.
         * @param address
         * @return
         */
        private String getAddressAsString(Address address) {
            StringBuffer sb = new StringBuffer();
            if (address.getFeatureName().isEmpty()) {
                sb.append(address.getAddressLine(0)).append(" ")
                        .append(address.getAdminArea()).append(" ")
                        .append(address.getCountryCode());
            }
            sb.append(address.getFeatureName()).append(" ")
                    .append(address.getAddressLine(0)).append(" ")
                    .append(address.getLocality()).append(" ")
                    .append(address.getAdminArea()).append(" ")
                    .append(address.getCountryCode()).append(" ");

            return sb.toString();
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            final LatLng latLong = latLng;
           if (latLng != null) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       MarkerOptions markerOptions = new MarkerOptions()
                               .position(latLong)
                               .title("Current Position")
                               .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                       mMarker = mMap.addMarker(markerOptions);
                       mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
                   }
               });
           }
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    /** END MAP/LOCATION RELATED CODE **/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
