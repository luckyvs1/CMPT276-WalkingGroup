package olive.walkinggroup.dataobjects;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * CurrentLocationHelper class allows getting location permissions, and centering a map to the
 * device's current location if found.
 */


public class CurrentLocationHelper
{
    private static final String TAG = "GCLHClass";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 8080;
    private static final float DEFAULT_ZOOM = 1f;


    private Boolean animate;
    private Boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Activity activity;

    public CurrentLocationHelper(Activity activity) {
        this.activity = activity;

        locationPermissionGranted = false;
    }

    public Boolean getLocationPermissionGranted() {
        return locationPermissionGranted;
    }

    public void setLocationPermissionGranted(Boolean locationPermissionGranted) {
        this.locationPermissionGranted = locationPermissionGranted;
    }

    // Request permission from user to locate their current location
    public void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        Log.d(TAG, "getLocationPermission: getting permission...");
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                locationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    public void centerCameraAtCurrentLocationIfFound(GoogleMap map) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        try {
            if (locationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Found location

                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                // Center camera on current location
                                moveCamera(map, currentLatLng);
                            }

                        } else {
                            // Cannot find current location
                            Log.d(TAG, "centerCameraAtCurrentLocationIfFound: onComplete: location not found.");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "centerCameraAtCurrentLocationIfFound: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(GoogleMap map, LatLng latLng) {
        if (animate) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    public void setAnimate(Boolean animate) {
        this.animate = animate;
    }
}
