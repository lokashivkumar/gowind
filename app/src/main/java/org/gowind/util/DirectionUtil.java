package org.gowind.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.gowind.model.Distance;
import org.gowind.model.Duration;
import org.gowind.model.Ride;
import org.gowind.model.RideStatus;
import org.gowind.model.Route;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shiv.loka on 10/4/16.
 */

public class DirectionUtil {

    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String TAG = DirectionUtil.class.getSimpleName();
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyA3Mo2jhbVxFCG6fkuIM3DPusRj6U-2RiQ";
    //AIzaSyDB5SHA8L8FkLnm8S52wziQp-9tl3CoRRs
    private DirectionFinderListener listener;
    private LatLng originLatLng, destinationLatLng;

    public DirectionUtil(DirectionFinderListener listener, LatLng origin, LatLng destination) {
        this.listener = listener;
        this.originLatLng = origin;
        this.destinationLatLng = destination;
    }

    public void getDirections() {
        String urlString = createDirectionsURL(originLatLng, destinationLatLng);
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlString).newBuilder();
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //TODO: PARSE JSON DATA upon response and update the directions request object.
                //TODO: Run on UI Thread - draw the map between point A and Point B based upon Directions Request POJO.
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Could not create directions request");
                }
                String responseData = response.body().string();
                Log.i(TAG +"**********", responseData);
                try {
                    parseDirectionResponse(responseData);
                } catch (JSONException e) {
                    Log.e(TAG, "Could not parse direction response");
                    e.printStackTrace();
                }
            }
        });
    }

    private void parseDirectionResponse(String directionsResponseData) throws JSONException {
        List<Route> rideRouteList = new ArrayList<>();
        JSONObject jsonData = new JSONObject(directionsResponseData);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        Ride ride = new Ride();
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));

            rideRouteList.add(route);
        }
        ride.setRoute(rideRouteList);
        ride.setBookingTime(new Date());
        ride.setRideStatus(RideStatus.AWAITING_PICKUP);
        ride.setDynamicPricing(true);
        listener.onDirectionFinderSuccess(rideRouteList);
    }

    /**
     * This is to decode the polyline parameter that Google direction API returns.
     * @param poly
     * @return
     */
    private List<LatLng> decodePolyLine(String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int destinationLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += destinationLat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int destinationLong = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += destinationLong;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }

    private String createDirectionsURL(LatLng originLatLng, LatLng destinationLatLng) {
        String encodedOriginLatLng = "";
        String encodedDestinationLatLng = "";
        try {
            encodedOriginLatLng = URLEncoder.encode(originLatLng.latitude + "," + originLatLng.longitude, "utf-8");
            encodedDestinationLatLng = URLEncoder.encode(destinationLatLng.latitude + "," +destinationLatLng.longitude, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = DIRECTIONS_API_URL
                + "?origin=" + encodedOriginLatLng
                + "&destination=" + encodedDestinationLatLng
                + "&key=" + GOOGLE_MAPS_API_KEY;
        System.out.println(url);
        if (url != null && url.length() > 0) {
            return url;
        }
        return "";
    }
}
