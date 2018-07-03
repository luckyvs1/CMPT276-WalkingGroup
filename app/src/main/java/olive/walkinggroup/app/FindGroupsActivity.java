package olive.walkinggroup.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class FindGroupsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "FindGroupsActivity";

    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 8080;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap mMap;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        model = Model.getInstance();
        setupMyLocationButton();
        getLocationPermission();
    }

    private void setupMyLocationButton() {
        RelativeLayout myLocationButton = findViewById(R.id.joinGroup_myLocationButton);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Centering on current location.");
                getDeviceLocation(true);
            }
        });
    }

    // Request permission from user to locate their current location
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        Log.d(TAG, "getLocationPermission: getting permission...");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                locationPermissionGranted = true;

                initializeMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Refresh activity after user grants permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: permission granted. Restarting activity...");
                    recreate();
                }
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.joinGroup_map);
        mapFragment.getMapAsync(FindGroupsActivity.this);
    }

    private void getDeviceLocation(boolean animate) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Found location
                            Log.d(TAG, "getDeviceLocation: onComplete: location found.");
                            Location currentLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            // Center camera on current location
                            moveCamera(currentLatLng, DEFAULT_ZOOM, animate);
                        } else {
                            // Cannot find current location
                            Log.d(TAG, "getDeviceLocation: onComplete: location not found.");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, boolean animate) {
        if (animate) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (locationPermissionGranted) {
            getDeviceLocation(false);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            populateMapWithMarkers();
            mMap.setOnMarkerClickListener(this);
        }
    }

    private void populateMapWithMarkers() {
        // Commented until able to add group to server
        //getGroupListFromServer();

        // Remove this when above is implemented
        markGroups(tempAddGroups());
    }

    // Temp function in place until method to add group to server exists.
    private List<Group> tempAddGroups() {
        // Walks from residence to fitness centre.
        User user1 = new User();
        user1.setName("Jim");
        Group group1 = new Group("Gym Group",
                "Work out together!",
                user1,
                new double[]{49.280628, 49.279460},
                new double[]{-122.928645, -122.922323},
                null);

        // Walks from AQ to Brian's office
        User user2 = new User();
        user2.setName("Chris");
        Group group2 = new Group("Finding Brian",
                "Ask all your questions in Brian's office hours.",
                user2,
                new double[] {49.278495, 49.276756},
                new double[] {-122.915911, -122.914109},
                null);

        List<Group> groupList = new ArrayList<>();
        groupList.add(group1);
        groupList.add(group2);

        return groupList;
    }

    private void getGroupListFromServer() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(FindGroupsActivity.this, caller, returnedList -> markGroups(returnedList));
    }

    private void markGroups(List<Group> returnedList) {
        for (Group group : returnedList) {
            addMarker(group);
        }
    }

    private void addMarker(Group group){
        String groupName = group.getGroupName();
        LatLng endPoint = group.getEndPoint();

        Marker marker = mMap.addMarker(new MarkerOptions().position(endPoint).title(groupName));
        // Associates a group object with a map Marker
        marker.setTag(group);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Group group = (Group) marker.getTag();

        Intent intent = new Intent(FindGroupsActivity.this, GroupDetailsActivity.class);
        intent.putExtra("group", group);
        startActivity(intent);

        return false;
    }
}