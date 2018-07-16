package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.CurrentLocationHelper;
import olive.walkinggroup.dataobjects.GpsLocation;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.dataobjects.UserListHelper;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;



public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<User> listUsers;
    private List<Marker> userMarkers = new ArrayList<>();

    private Timer updateMarkersTimer = new Timer();

    private Model instance;
    private User currentUser;

    private UserListHelper userListHelper;

    private static final int UPDATE_MARKERS_DELAY_S = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();


        initializeMap();
        getMonitorUsersFromServer();
        setupUpdateMarkersTimer();


    }

    private void setupUpdateMarkersTimer() {
        updateMarkersTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (listUsers != null) {
                    updateGpsLocationFromServer();
                    Log.i("MyApp", "Update markers");
                }
            }
        }, 0, UPDATE_MARKERS_DELAY_S*1000);
    }

    private void updateGpsLocationFromServer() {
        for (int i = 0; i < listUsers.size(); i++) {
            Call<GpsLocation> caller = instance.getProxy().getLastGpsLocation(listUsers.get(i).getId());
            int position = i;
            ProxyBuilder.callProxy(this, caller, gpsLocation -> onGetLastGpsLocation(gpsLocation, position));

        }

    }

    private void onGetLastGpsLocation(GpsLocation gpsLocation, int position) {
        LatLng location = gpsLocationToLatLng(gpsLocation);
        userMarkers.get(position).setPosition(location);

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
                moveCamera(userMarkers.get(position).getPosition(), CurrentLocationHelper.DEFAULT_ZOOM);

            }
        });
    }



    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


}
