package org.gowind;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.gowind.util.PermissionUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    //TODO: Use butterknife


    private static final String LOGTAG = "Gowind-MapsActivity";
    private static final int NUMBER_OF_ADDRESSES = 3;
    private static final int MINIMUM_DROPDOWN_THRESHOLD = 3;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    GoogleMap mMap;
    @BindView(R.id.requestButton) Button requestButton;
    @BindView(R.id.originAutoComplete) AutoCompleteTextView originAutoComplete;
    @BindView(R.id.destinationAutoComplete) AutoCompleteTextView destinationAutoComplete;
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

    private LatLng mCurrentLatLng;

    public LatLng getmCurrentLatLng() {
        return mCurrentLatLng;
    }

    public void setmCurrentLatLng(LatLng mCurrentLatLng) {
        this.mCurrentLatLng = mCurrentLatLng;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

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

        setUpViewListeners();
    }

    private void setUpViewListeners() {

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

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            //to get current location
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        Location myCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (myCurrentLocation != null) {
            new UpdateMarkerTask().execute(myCurrentLocation);
        }
        //to receive location updates upon movement.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        new UpdateMarkerTask().execute(mCurrentLocation);
        Log.i(LOGTAG, getmCurrentLatLng().latitude + " : " + getmCurrentLatLng().longitude);
        new UpdateTextViewAdapterTask().execute(getmCurrentLatLng());
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * This class has the following functions:
     * 1. upon change in location, the onLocationChanged method passes the location to this class
     * 2. this class takes the location parameter and passes the LatLng to onPostExecute method
     * 3. The onPostExecute() updates the view (marker and map) on the UI thread with the latlng.
     */

    private class UpdateMarkerTask extends AsyncTask<Location, Integer, LatLng> {
        @Override
        protected LatLng doInBackground(Location... locations) {
            Thread.currentThread().setName("LocationUpdater");
            LatLng latLng = null;
            Location userLocation = locations[0];
            if (userLocation != null) {
                String lastLocationUpdateTime = DateFormat.getTimeInstance().format(new Date());
                Log.i(LOGTAG, userLocation.getLatitude() + " : " +
                        userLocation.getLongitude() + " : update time: " + lastLocationUpdateTime);
                latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            }
            return latLng;
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            final LatLng latLong = latLng;

            setmCurrentLatLng(latLong);
            if (mMarker != null) {
                mMarker.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLong)
                    .title("Current Position");
            mMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);
         }
    }

    private class UpdateTextViewAdapterTask extends AsyncTask<LatLng, Void, List<String>> {
        @Override
        protected List<String> doInBackground(LatLng... latLngs) {
            Thread.currentThread().setName("TextViewAdapterUpdater");

            LatLng latLng = latLngs[0];
            List<Address> addresses = new ArrayList<>();
            String errorMessage;
            List<String> stringAddresses = new ArrayList<>();
            if (latLng != null) {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, NUMBER_OF_ADDRESSES);
                } catch (IOException ioException) {
                    errorMessage = "Service Not Available";
                    Log.e(LOGTAG, errorMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    errorMessage = "Invalid Latitude or Longitude Used";
                    Log.e(LOGTAG, errorMessage + ". " +
                            "Latitude = " + latLng.latitude + ", Longitude = " +
                            latLng.longitude, illegalArgumentException);
                }
            }
            if (addresses != null && addresses.size() > 0) {
                for (Address address : addresses) {
                    stringAddresses.add(address.getAddressLine(0));
                }
            }
            return stringAddresses;
        }

        @Override
        protected void onPostExecute(List<String> stringAddresses) {

            if (stringAddresses.size() > 0) {
                ArrayAdapter<String> autoCompleteTextViewAdapter = new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_dropdown_item_1line, stringAddresses);
                autoCompleteTextViewAdapter.setNotifyOnChange(true);
                autoCompleteTextViewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                originAutoComplete.setAdapter(autoCompleteTextViewAdapter);
                originAutoComplete.setThreshold(MINIMUM_DROPDOWN_THRESHOLD);
                autoCompleteTextViewAdapter.notifyDataSetChanged();
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

}