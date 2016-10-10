package org.gowind;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.gowind.model.Route;
import org.gowind.model.UserLocation;
import org.gowind.util.DirectionFinderListener;
import org.gowind.util.DirectionUtil;
import org.gowind.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        OnMapReadyCallback, DirectionFinderListener {

    @BindView(R.id.requestButton) Button requestButton;
    @BindView(R.id.paymentsButton) ImageButton paymentsButton;
    @BindView(R.id.profileButton) ImageButton profileButton;
    @BindView(R.id.settingsButton) ImageButton settingsButton;
    @BindView(R.id.activity_map_launcher) LinearLayout mapsLinearLayout;

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private ProgressDialog progressDialog;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Marker mOriginMarker;
    private Marker mDestinationMarker;
    private UserLocation userLocation = new UserLocation();
    private LocationRequest mLocationRequest;
    private List<Polyline> polylinePaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(this, this)
                    .build();
        }
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
                if (userLocation.getOriginLatLng() == null
                        && userLocation.getDestinationLatLng() == null) {
                    Snackbar.make(mapsLinearLayout, "Please ensure that the destination and origin addresses are selected.",
                            Snackbar.LENGTH_LONG);
                }
                else {
                    DirectionUtil directionUtil = new DirectionUtil(MapsActivity.this,
                            userLocation.getOriginLatLng(), userLocation.getDestinationLatLng());
                    directionUtil.getDirections();
                }
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
                //Intent userSettingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                //TODO : Create settings activity and pass intent
            }
        });
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        PlaceAutocompleteFragment originAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.origin_autocomplete_fragment);
        originAutoCompleteFragment.setFilter(typeFilter);


        PlaceAutocompleteFragment destinationAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.destination_autocomplete_fragment);
        destinationAutoCompleteFragment.setFilter(typeFilter);

        originAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                userLocation.setOriginLocation(place.getName().toString());
                userLocation.setOriginLatLng(place.getLatLng());
                updateOriginMarker(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        destinationAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                userLocation.setDestinationLocation(place.getName().toString());
                userLocation.setDestinationLatLng(place.getLatLng());
                updateDestinationMarker(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to google location services.");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, R.string.geocoder_not_found, Toast.LENGTH_LONG).show();
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
            //to get current location
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        Location myCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (myCurrentLocation != null) {
            LatLng latLng = new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
            updateOriginMarker(latLng);
        }
        //to receive location updates upon movement.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to Google Location Services Failed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        updateOriginMarker(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void updateOriginMarker(LatLng latLng) {
        if (mOriginMarker != null) {
            mOriginMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Current Position");
        mOriginMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        mMap.animateCamera(zoom);
    }

    private void updateDestinationMarker(LatLng latLng) {
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Destination Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mDestinationMarker = mMap.addMarker(markerOptions);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
        if (polylinePaths != null) {
            for (Polyline polyline: polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        if (progressDialog != null)
            progressDialog.dismiss();
        polylinePaths = new ArrayList<>();

        for (final Route route : routes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
                    PolylineOptions polylineOptions = new PolylineOptions().
                            geodesic(true).
                            color(Color.BLUE).
                            width(25);

                    for (int i = 0; i < route.points.size(); i++)
                        polylineOptions.add(route.points.get(i));

                    polylinePaths.add(mMap.addPolyline(polylineOptions));
                }
            });
        }
    }
}
