package olive.walkinggroup.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.CurrentLocationHelper;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

/**
 * FindGroupsActivity starts with a map, centred on user's current location, with markers showing
 * locations where existing walking groups walk to. Clicking on one of these red Markers will open
 * a group details page (GroupDetailsActivity), where user can interact with the group.
 * User can also create group here using the button on top.
 *
 * Part of the code used is from a Google Maps API video course, modified to fit this use case.
 * https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
 * by CodingWithMitch
 *
 * After clicking the create group button and coming back, the activity reloads the markers after
 * RELOAD_DELAY milliseconds. (Increase this time if new marker does not show up after creation)
 */

public class FindGroupsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "FindGroupsActivity";
    private static final int RELOAD_DELAY = 1000;

    private CurrentLocationHelper currentLocationHelper;
    private GoogleMap mMap;
    private Model model;
    private Boolean isReload = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        model = Model.getInstance();
        initializeMap();
        setupMyLocationButton();
        setupCreateGroupButton();
        currentLocationHelper = new CurrentLocationHelper(this);
        currentLocationHelper.getLocationPermission();
        updateCurrentUser();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isReload) {
            showLoadingCircle();
            handler.postDelayed(reloadMapRunnable, RELOAD_DELAY);
        }
    }

    private void updateCurrentUser() {
        Call<User> caller = model.getProxy().getUserById(model.getCurrentUser().getId());
        ProxyBuilder.callProxy(FindGroupsActivity.this, caller, returnedUser -> getUserById(returnedUser));
    }

    private void getUserById(User userFromEmail) {
        model.setCurrentUser(userFromEmail);
    }

    private void setupMyLocationButton() {
        RelativeLayout myLocationButton = findViewById(R.id.findGroup_myLocationBtn);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Centering on current location.");
                currentLocationHelper.setAnimate(true);
                currentLocationHelper.centerCameraAtCurrentLocationIfFound(mMap);
            }
        });
    }

    private void setupCreateGroupButton() {
        RelativeLayout createGroupButton = findViewById(R.id.findGroup_createGroupBtn);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindGroupsActivity.this, CreateGroupActivity.class);
                startActivity(intent);
                isReload = true;
            }
        });
    }

    // Refresh activity after user grants permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        currentLocationHelper.setLocationPermissionGranted(false);

        switch (requestCode) {
            case CurrentLocationHelper.LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentLocationHelper.setLocationPermissionGranted(true);
                    Log.d(TAG, "onRequestPermissionsResult: permission granted. Restarting activity...");
                    recreate();
                }
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.findGroup_map);
        mapFragment.getMapAsync(FindGroupsActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocationHelper.getLocationPermissionGranted()) {
            currentLocationHelper.setAnimate(false);
            currentLocationHelper.centerCameraAtCurrentLocationIfFound(mMap);

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

    private Runnable reloadMapRunnable = new Runnable() {
        @Override
        public void run() {
            populateMapWithMarkers();
        }
    };

    private void populateMapWithMarkers() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(FindGroupsActivity.this, caller, returnedList -> markGroups(returnedList));
    }

    private void markGroups(List<Group> returnedList) {
        for (Group group : returnedList) {
            if (group.getRouteLatArray() != null && group.getRouteLngArray() != null && group.getLeader() != null) {
                if (group.getRouteLatArray().length >= 2 && group.getRouteLngArray().length >= 2) {
                    addMarker(group);
                    Log.d("TAG", "Adding group to map...");
                }
            }
        }
        hideLoadingCircle();
        isReload = true;
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
        isReload = false;
        return false;
    }

    private void showLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.findGroup_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.findGroup_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
        }
    }
}
