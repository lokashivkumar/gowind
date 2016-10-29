package org.gowind;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import org.gowind.model.User;
import org.gowind.model.UserLocation;
import org.gowind.util.DirectionFinder;
import org.gowind.util.DirectionFinderListener;
import org.gowind.util.PermissionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.android.gms.fitness.data.Application.La;

/**
 * Launcher Activity
 * Initializes the views and place picker for origin and dest.
 *
 */
public class MapsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        OnMapReadyCallback, DirectionFinderListener {

    @BindView(R.id.requestButton) Button mRequestButton;
    @BindView(R.id.paymentsButton) ImageButton mPaymentButton;
    @BindView(R.id.profileButton) ImageButton mProfileButton;
    @BindView(R.id.settingsButton) ImageButton mSettingsButton;
    @BindView(R.id.saveTripButton) ImageButton mSaveTripButton;
    @BindView(R.id.activity_map_launcher) LinearLayout mMapsLinearLayout;
    @BindView(R.id.rideDetailPanel) LinearLayout mRideDetailPanel;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String ORIGIN_HINT_STRING = "Your Address";
    private static final String DESTINATION_HINT_STRING = "Destination Address";

    private GoogleMap mMap;
    private ProgressDialog progressDialog;
    private GoogleApiClient mGoogleApiClient;
    private Marker mOriginMarker;
    private Marker mDestinationMarker;
    private UserLocation mUserLocation = new UserLocation();
    private LocationRequest mLocationRequest;
    private List<Polyline> mPolylinePaths = new ArrayList<>();
    private User mUser;

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
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Your request has been created.", Toast.LENGTH_LONG).show();
                showRideDetailsPanel();
                if (mUserLocation.getOriginLatLng() == null
                        && mUserLocation.getDestinationLatLng() == null) {
                    Snackbar.make(mMapsLinearLayout, "Please ensure that the destination and origin addresses are selected.",
                            Snackbar.LENGTH_LONG);
                }
                else {
                    DirectionFinder directionUtil = new DirectionFinder(MapsActivity.this,
                            mUserLocation.getOriginLatLng(), mUserLocation.getDestinationLatLng());
                    directionUtil.getDirections();
                    //TODO : Have to make this visible only if the data is available from the server.
                }
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(MapsActivity.this, UserProfileActivity.class);

                OkHttpClient userProfileClient = new OkHttpClient();
                HttpUrl userProfileUrl = new HttpUrl.Builder()
                        .scheme("http")
                        .host("10.0.2.2")
                        .port(9200)
                        .addPathSegment("user")
                        .addPathSegment("getuser")
                        .addQueryParameter("username", "mUser")
                        .build();
                //String userProfileUrl = GOWIND_SERVICES_URL + "/test";
                Request userProfileRequest = new Request.Builder()
                        .url(userProfileUrl)
                        .build();

                Log.i(TAG + " url is: ", userProfileUrl.toString());
                userProfileClient.newCall(userProfileRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "User not found");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected error " + response);
                        } else {
                            Headers responseHeaders = response.headers();
                            for (int i = 0; i < responseHeaders.size(); i++) {
                                Log.i(TAG, responseHeaders.name(i) + " :" + responseHeaders.value(i));
                            }
                            String responseString = response.body().string();
                            Log.i(TAG + " response: ", responseString);
                            ObjectMapper objectMapper = new ObjectMapper();
                            mUser = objectMapper.readValue(responseString, User.class);
                        }
                    }
                });
                if (mUser != null) {
                    profileIntent.putExtra("user", mUser);
                    startActivity(profileIntent);
                } else {
                    try {
                        throw new IOException("Unable to start User Profile activity as there was an error fetching user data");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent paymentIntent = new Intent(MapsActivity.this, PaymentActivity.class);
                startActivity(paymentIntent);
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent userSettingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                //TODO : Create settings activity and pass intent
            }
        });

        mSaveTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Saving your trip");
            }
        });
    }

    private void showRideDetailsPanel() {
        String foo = "foo";
        Log.i(TAG, foo);
        mRideDetailPanel.setVisibility(View.VISIBLE);
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

        PlaceAutocompleteFragment originAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.origin_autocomplete_fragment);
        originAutoCompleteFragment.setHint(ORIGIN_HINT_STRING);

        PlaceAutocompleteFragment destinationAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.destination_autocomplete_fragment);
        destinationAutoCompleteFragment.setHint(DESTINATION_HINT_STRING);

        originAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                mUserLocation.setOriginLocation(place.getName().toString());
                mUserLocation.setOriginLatLng(place.getLatLng());
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
                mUserLocation.setDestinationLocation(place.getName().toString());
                mUserLocation.setDestinationLatLng(place.getLatLng());
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
        updatePolylineOnMap();
    }

    private void updatePolylineOnMap() {
        if (mPolylinePaths != null) {
            for (Polyline polyline: mPolylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        if (progressDialog != null)
            progressDialog.dismiss();
        mPolylinePaths = new ArrayList<>();

        for (final Route route : routes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 12));
                    PolylineOptions polylineOptions = new PolylineOptions().
                            geodesic(true).
                            color(Color.BLUE).
                            width(30);

                    for (int i = 0; i < route.points.size(); i++)
                        polylineOptions.add(route.points.get(i));

                    mPolylinePaths.add(mMap.addPolyline(polylineOptions));
                }
            });
        }
    }
}
