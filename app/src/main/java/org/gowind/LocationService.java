//package org.gowind;
//
//import android.app.Service;
//import android.content.Intent;
//import android.location.Location;
//import android.os.Binder;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//
//public class LocationService extends Service implements
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener,
//        LocationListener {
//
//    private boolean currentlyProcessingLocation = false;
//    private LocationRequest locationRequest;
//    private GoogleApiClient googleApiClient;
//
//    public LocationService() {
//    }
//
//    public class LocalBinder extends Binder {
//        public LocationService getServerInstance() {
//            return LocationService.this;
//        }
//    }
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//
////        if (mRequestingLocationUpdates) {
////            startLocationUpdates();
////        }
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//}
