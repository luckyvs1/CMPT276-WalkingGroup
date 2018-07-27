package olive.walkinggroup.dataobjects;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.R;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

/**
 * UploadGpsLocation class allow starting and stopping of uploading the gps location of the current user.  The
 * class also auto stops the timer if the user has arrived within a fixed distance from their active walking
 * group's destination location.
 */

public class UploadGpsLocation {
    private CurrentLocationHelper currentLocationHelper;
    private Activity activity;
    private Model instance;
    private User user;
    private Timer uploadTimer = new Timer();
    private Timer autoStopTimer = new Timer();

    private GpsLocation currentUserLocation = new GpsLocation();
    private GpsLocation activeGroupDestLocation = new GpsLocation();
    private Group activeGroup;
    private boolean hasArrived;

    private static final int EARTH_RADIUS_METERS = 6371000;
    private static final int DISTANCE_WITHIN_LOCATION_METERS = 100;

    private static final int NUM_MS_IN_S = 1000;
    private static final int NUM_S_IN_MIN = 60;

    private static final int UPLOAD_RATE_S = 30;
    private static final int UPLOAD_DELAY_S = 0;
    private static final int STOP_UPLOAD_DELAY_MIN = 10;

    public UploadGpsLocation(Activity activity) {
        currentLocationHelper = new CurrentLocationHelper(activity);
        this.activity = activity;
        hasArrived = false;
        instance = Model.getInstance();
        user = instance.getCurrentUser();

    }

    public void start() {
        cancelTimers();
        hasArrived = false;
        activeGroup = instance.getActiveGroup();

        if (instance.activeGroupSelected()) {

            setGroupDestLocation();
            currentLocationHelper.getLocationPermission();
            uploadTimer = new Timer();
            uploadTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    getLocationAndUploadToServer();
                    if (hasArrivedAtDestLocation() && !hasArrived) {
                        Log.i("MyApp", "UploadGpsLocation arrived in destination location");
                        instance.setCompletedWalkGroup(activeGroup);

                        hasArrived = true;
                        startAutoStopTimer();
                    }


                }
            }, UPLOAD_DELAY_S, UPLOAD_RATE_S * NUM_MS_IN_S);
        } else {
            Log.i("MyApp", "UploadGpsLocation (start() -> Group is null)");
        }


    }

    private void setGroupDestLocation() {
        if (instance.activeGroupSelected()) {
            activeGroupDestLocation.setLat(activeGroup.getEndPoint().latitude);
            activeGroupDestLocation.setLng(activeGroup.getEndPoint().longitude);

            Log.i("MyApp: ", "UploadGpsLocation: " + activeGroup.getEndPoint().toString());
        } else {
            Log.i("MyApp", "UploadGpsLocation (setGroupDestLocation() -> Group is null)");
        }
    }

    private boolean hasArrivedAtDestLocation() {
        // Using a fixed distance instead of a range
        Boolean arrivedAtLocation = false;

        // Haversine pseudocode from:https://community.esri.com/groups/coordinate-reference-systems/blog/2017/10/05/haversine-formula
        Double phi_1 = Math.toRadians(activeGroupDestLocation.getLat());
        Double phi_2 = Math.toRadians(currentUserLocation.getLat());

        Double delta_phi = Math.toRadians(activeGroupDestLocation.getLat() - currentUserLocation.getLat());
        Double delta_lambda = Math.toRadians(activeGroupDestLocation.getLng() - currentUserLocation.getLng());


        //a = sin²(φB - φA/2) + cos φA * cos φB * sin²(λB - λA/2)
        //c = 2 * atan2( √a, √(1−a) )
        //d = R ⋅ c -- distanceInMeters

        Double a = Math.pow(Math.sin(delta_phi / 2.0), 2) + Math.cos(phi_1) * Math.cos(phi_2) * Math.pow(Math.sin(delta_lambda / 2.0), 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        Double distanceInMeters = EARTH_RADIUS_METERS * c;

        if(distanceInMeters <= DISTANCE_WITHIN_LOCATION_METERS) {
            arrivedAtLocation = true;
        }
        return arrivedAtLocation;
    }

    private void startAutoStopTimer() {
        Log.i("MyApp", "UploadGpsLocation: startAutoStopTimer");

        autoStopTimer = new Timer();
        autoStopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stop();
            }
        },STOP_UPLOAD_DELAY_MIN*NUM_S_IN_MIN*NUM_MS_IN_S);
    }


    public void stop() {
        Log.i("MyApp", "UploadGpsLocation: stop upload.");
        cancelTimers();
        hasArrived = false;
        instance.clearActiveGroup();
    }

    private void cancelTimers() {
        autoStopTimer.cancel();
        uploadTimer.cancel();
    }

    private void getLocationAndUploadToServer() {
        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        try {
            if (currentLocationHelper.getLocationPermissionGranted()) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                                LatLng currentLatLng = new LatLng(0,0);
                                
                                currentUserLocation.setLat(currentLatLng.latitude);
                                currentUserLocation.setLng(currentLatLng.longitude);

                                uploadGpsLocationToServer(currentLatLng);

                                // TODO: retrieve updated user information from server instead?
                                instance.getCurrentUser().setLastGpsLocation(currentUserLocation);
                            }
                        } else {
                            // Cannot find current location
                            Log.d("MyApp", "UploadGpsLocation: onComplete: location not found.");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("MyApp", "UploadGpsLocation: SecurityException: " + e.getMessage());
        }

    }

    private void uploadGpsLocationToServer(LatLng latLng) {
        String timeStamp = new SimpleDateFormat(GetLastUpdated.PATTERN, GetLastUpdated.LOCALE).format(new Date());
        GpsLocation lastGpsLocation = new GpsLocation(latLng.latitude, latLng.longitude, timeStamp);
        Call<GpsLocation> caller = instance.getProxy().setLastGpsLocation(user.getId(), lastGpsLocation);
        ProxyBuilder.callProxy(activity, caller, returnedGpsLocation -> setLastGpsLocationReturned(returnedGpsLocation));

        Log.i("MyApp", "UploadGpsLocation: upload to server");


    }

    public boolean hasArrived() {
        return hasArrived;
    }

    private void setLastGpsLocationReturned(GpsLocation returnedGpsLocation) {
        // callback
    }

}
