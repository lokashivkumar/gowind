package org.gowind;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import io.fabric.sdk.android.Fabric;
import com.google.android.gms.maps.model.MarkerOptions;

import org.gowind.api.LocationAddress;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private static final String LOGTAG = "Gowind-MapsActivity";
    private static final int MAP_LOCATION_CONSTANT = 12;
    private GoogleMap mMap;
    private Button requestButton;
    private AutoCompleteTextView locationDropDown;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isMapReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_maps);

        //Setting up the action/toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

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

        locationDropDown = (AutoCompleteTextView) findViewById(R.id.locationDropDown);
        locationDropDown.setOnItemSelectedListener(this);
        requestButton = (Button) findViewById(R.id.requestButton);

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Your request has been created.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
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
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000);
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

        //Getting current user location.
        Location lastUserLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationAddress currentAddress = new LocationAddress();

        LatLng lastLoc = new LatLng(lastUserLocation.getLatitude(), lastUserLocation.getLongitude());

        //Updating the map with a marker when ready.
        if (isMapReady) {
            mMap.addMarker(new MarkerOptions().position(lastLoc));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLoc));
        }

        List<Address> addressList = currentAddress.getAddressFromLocation(this, lastUserLocation);
        if (addressList.size() != 0) {
            String userLocation = getAddressAsString(addressList.get(0));
            locationDropDown.setText(userLocation);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        LocationAddress locAddress = new LocationAddress();
        List<Address> addressList = locAddress.getAddressFromLocation(this, location);
        List<String> stringAddresses = new ArrayList<>();

        for (Address address : addressList) {
            stringAddresses.add(getAddressAsString(address));
        }

        if (stringAddresses.size() != 0) {
            Log.i(LOGTAG, "Location changed to: " + stringAddresses.get(0));
        }
//        if (isMapReady) {
//            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
//                    location.getLongitude())).title("Marker in " + location.getLatitude()));
//        }
//        ArrayAdapter<String> autCompleteTextViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stringAddresses);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        locationDropDown.setAdapter(spinnerAdapter);
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
