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

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.GetCurrentLocationHelper;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class FindGroupsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "FindGroupsActivity";

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 8080;

    private GetCurrentLocationHelper getCurrentLocationHelper;

    private GoogleMap mMap;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        model = Model.getInstance();
        initializeMap();
        setupMyLocationButton();
        setupCreateGroupButton();
        getCurrentLocationHelper = new GetCurrentLocationHelper(this);
        getCurrentLocationHelper.getLocationPermission();
    }

    private void setupMyLocationButton() {
        RelativeLayout myLocationButton = findViewById(R.id.findGroup_myLocationBtn);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Centering on current location.");
                getCurrentLocationHelper.setAnimate(true);
                getCurrentLocationHelper.getDeviceLocation(mMap);
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
            }
        });
    }

    // Refresh activity after user grants permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getCurrentLocationHelper.setLocationPermissionGranted(false);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationHelper.setLocationPermissionGranted(true);
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

        if (getCurrentLocationHelper.getLocationPermissionGranted()) {
            getCurrentLocationHelper.setAnimate(false);
            getCurrentLocationHelper.getDeviceLocation(mMap);

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
