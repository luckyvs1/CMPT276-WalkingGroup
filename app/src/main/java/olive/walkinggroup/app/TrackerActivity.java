package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.CurrentLocationHelper;
import olive.walkinggroup.dataobjects.GpsLocation;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.dataobjects.UserListHelper;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;



public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<User> listUsers;
    private List<Marker> userMarkers = new ArrayList<>();

    private Model instance;
    private User currentUser;

    private UserListHelper userListHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();


        initializeMap();
        getMonitorUsersFromServer();


    }

    private void getMonitorUsersFromServer() {
        Call<List<User>> caller = instance.getProxy().getMonitorsUsers(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onGetMonitorUsers(returnedUsers));
    }

    private void onGetMonitorUsers(List<User> returnedUsers) {
        listUsers = UserListHelper.sortUsers(returnedUsers);
        hideLoadingCircle();
        populateUserList();

        setupListOnItemClickListeners();
        populateUserMarkers();

    }

    private void populateUserList() {
        userListHelper = new UserListHelper(this, listUsers, currentUser);

        ArrayAdapter<User> adapter = userListHelper.getAdapter();
        ListView listView = findViewById(R.id.listView_trackUsers);
        listView.setAdapter(adapter);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.tracker_map);
        mapFragment.getMapAsync(TrackerActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void populateUserMarkers() {
        for (int i = 0; i < listUsers.size(); i++) {
            User user = listUsers.get(i);
            LatLng location = gpsLocationToLatLng(user.getLastGpsLocation());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(user.getName()));
            userMarkers.add(marker);
        }
    }



    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.trackUsers_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
        }
    }
    
    private LatLng gpsLocationToLatLng(GpsLocation gpsLocation) {
        return new LatLng(gpsLocation.getLat(), gpsLocation.getLng());
    }

    private void setupListOnItemClickListeners() {
        ListView list = findViewById(R.id.listView_trackUsers);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                User user = listUsers.get(position);
                LatLng location = gpsLocationToLatLng(user.getLastGpsLocation());
                moveCamera(location, CurrentLocationHelper.DEFAULT_ZOOM);

            }
        });
    }



    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


}
