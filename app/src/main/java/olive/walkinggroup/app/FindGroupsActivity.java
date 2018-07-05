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
    private static final float DEFAULT_ZOOM = 1f;

    private Boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap mMap;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

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

                            if (currentLocation != null) {
                                LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                // Center camera on current location
                                moveCamera(currentLatLng, DEFAULT_ZOOM, animate);
                            }
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
        getGroupListFromServer();

        // Remove this when above is implemented
        //markGroups(tempAddGroups());
    }

    // Temp function in place until method to add group to server exists.
    public static List<Group> tempAddGroups() {
        // Walks from residence to fitness centre.
        User user1 = new User();
        user1.setName("Jim");
        user1.setEmail("Jim@gym.ca");
        user1.setId((long) 1111);

        Group group1 = new Group("Gym Group",
                "Work out together!",
                user1,
                new double[]{49.280628, 49.279460},
                new double[]{-122.928645, -122.922323},
                getTestMemberList());

        // Walks from AQ to Brian's office. Leader is Bob.
        User user2 = new User();
        user2.setName("Ted");
        user2.setEmail("ted@example.org");
        user2.setId((long) 1112);
        List<User> memberList2 = new ArrayList<>();
        User user3 = getBob();

        memberList2.add(user2);
        memberList2.add(user1);
        Group group2 = new Group("Finding Brian",
                "Ask all your questions in Brian's office hours.\nWe have a lot of questions to ask, so this description will also be very lengthy! (Max 3 lines shown here) \n But more can be seen in GroupDetailsActivity",
                user3,
                new double[] {49.278495, 49.276756},
                new double[] {-122.915911, -122.914109},
                memberList2);

        List<Group> groupList = new ArrayList<>();
        groupList.add(group1);
        groupList.add(group2);

        return groupList;
    }

    // Temp function to generate a list of User
    public static List<User> getTestMemberList() {
        List<User> list = new ArrayList<>();

        User user1 = new User();
        user1.setName("Adam");
        user1.setEmail("adam@email.com");
        user1.setId((long) 1001);
        list.add(user1);

        User user2 = getBob();
        list.add(user2);

        User user3 = new User();
        user3.setName("Bob");
        user3.setEmail("bob.junior@bobby.com");
        user3.setId((long) 2223);
        list.add(user3);

        User user4 = new User();
        user4.setName("Chris");
        user4.setEmail("chris@example.com");
        user4.setId((long) 1002);
        list.add(user4);

        User user5 = new User();
        user5.setName("Dr. Brian Fraser");
        user5.setEmail("bfraser@sfu.ca");
        user5.setId((long) 9999);
        list.add(user5);

        return list;
    }

    // Temp function to generate User "Bob"
    public static User getBob() {
        User bob = new User();
        bob.setName("Bob Bobby");
        bob.setId((long) 23);
        bob.setEmail("bob@bobby.com");

        List<User> monitorList = new ArrayList<>();
        User bobJr = new User();
        bobJr.setName("Bob Jr. Bobby");
        bobJr.setEmail("bob.jr@bobby.com");
        bobJr.setId((long) 29);
        Group group1 = new Group();
        group1.setId((long) 8);
        List<Group> groupList1 = new ArrayList<>();
        groupList1.add(group1);
        //bobJr.setMemberOfGroups(groupList1);
        monitorList.add(bobJr);

        User bobby = new User();
        bobby.setName("Bobby Chan");
        bobby.setEmail("bobby@chan.com");
        bobby.setId((long) 32);
        Group group2 = new Group();
        group2.setId((long) 28);
        List<Group> groupList2 = new ArrayList<>();
        groupList2.add(group1);
        groupList2.add(group2);
        //bobby.setMemberOfGroups(groupList2);
        monitorList.add(bobby);

        bob.setMonitorsUsers(monitorList);
        return bob;
    }

    private void getGroupListFromServer() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(FindGroupsActivity.this, caller, returnedList -> markGroups(returnedList));
    }

    private void markGroups(List<Group> returnedList) {
        for (Group group : returnedList) {
            if (group.getRouteLatArray() != null && group.getRouteLngArray() != null) {
                if (group.getRouteLatArray().length >= 2 && group.getRouteLngArray().length >= 2) {
                    addMarker(group);
                    Log.d("TAG", "Adding group to map...");
                }
            }
        }
    }

    private void addMarker(Group group){
        String groupDescription = group.getGroupDescription();
        LatLng endPoint = group.getEndPoint();

        Marker marker = mMap.addMarker(new MarkerOptions().position(endPoint).title(groupDescription));
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
