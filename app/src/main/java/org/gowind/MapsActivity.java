package org.gowind;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.gowind.api.LocationAddress;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String LOGTAG = "Gowind-MapsActivity";
    private GoogleMap mMap;
    private Button requestButton;
    private AutoCompleteTextView originAutoComplete;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = false;
    private boolean isMapReady;
    private Location mCurrentUserLocation;
    private ImageButton paymentsButton;
    private ImageButton profileButton;
    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_maps);

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

        originAutoComplete = (AutoCompleteTextView) findViewById(R.id.originAutoComplete);
        originAutoComplete.setOnItemSelectedListener(this);

        requestButton = (Button) findViewById(R.id.requestButton);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Your request has been created.", Toast.LENGTH_LONG).show();
            }
        });

        profileButton = (ImageButton) findViewById(R.id.profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MapsActivity.this, UserProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        paymentsButton = (ImageButton) findViewById(R.id.paymentsButton);
        paymentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent paymentIntent = new Intent(MapsActivity.this, PaymentActivity.class);
                startActivity(paymentIntent);
            }
        });

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent userSettingsIntent = new Intent(MapsActivity.this, UserProfileActivity.class);
                //TODO : Create settings activity and pass intent
            }
        });



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                //final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        mRequestingLocationUpdates = true;
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this, 1);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


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
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        Log.e(LOGTAG, "Disconnected from Google Locaiton services");
        super.onStop();
    }

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
        isMapReady = true;
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOGTAG, "Successfully connected to the Google Location Services API");
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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
        mCurrentUserLocation = location;
        updateUI();
    }

    private void updateUI() {
        LocationAddress locAddress = new LocationAddress();
        List<Address> addressList = locAddress.getAddressFromLocation(this, mCurrentUserLocation);
        List<String> stringAddresses = new ArrayList<>();
        LatLng lastLoc = new LatLng(mCurrentUserLocation.getLatitude(), mCurrentUserLocation.getLongitude());
        for (Address address : addressList) {
            stringAddresses.add(getAddressAsString(address));
        }

        if (isMapReady) {
            mMap.addMarker(new MarkerOptions().position(lastLoc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLoc));
        }
        if (addressList.size() != 0) {
            ArrayAdapter<String> autoCompleteTextViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stringAddresses);
            autoCompleteTextViewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            originAutoComplete.setAdapter(autoCompleteTextViewAdapter);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * Helper method to convert a given Address Object to String -
     * returns Location, city, state, feature name, country code, postal code.
     * @param address
     * @return
     */
    private String getAddressAsString(Address address) {

        StringBuffer sb = new StringBuffer();
        sb.append(address.getFeatureName()).append(" ")
                .append(address.getAddressLine(0)).append(" ")
                .append(address.getLocality()).append(" ")
                .append(address.getAdminArea()).append(" ")
                .append(address.getLocality()).append(" ")
                .append(address.getCountryCode()).append(" ")
                .append(address.getPostalCode());

        return sb.toString();
    }
}
