package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.CurrentLocationHelper;
import olive.walkinggroup.dataobjects.GetLastUpdated;
import olive.walkinggroup.dataobjects.GpsLocation;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.dataobjects.UserListHelper;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;



public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private List<User> listTrackUsers = new ArrayList<>();
    private List<Marker> userMarkers = new ArrayList<>();

    private Timer updateMarkersTimer = new Timer();
    List<User> monitorUsers = new ArrayList<>();


    private Model instance;
    private User currentUser;

    private UserListHelper userListHelper;

    private static final int UPDATE_DELAY_S = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();


        initializeMap();
        getUsersFromServer();
        setupUpdateMarkersTimer();


    }

    private void setupUpdateMarkersTimer() {
        updateMarkersTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (listTrackUsers != null) {
                    updateGpsLocationFromServer();
                }
            }
        }, 0, UPDATE_DELAY_S*1000);
    }

    private void updateGpsLocationFromServer() {
        for (int i = 0; i < listTrackUsers.size(); i++) {
            Call<GpsLocation> caller = instance.getProxy().getLastGpsLocation(listTrackUsers.get(i).getId());
            int position = i;
            ProxyBuilder.callProxy(this, caller, gpsLocation -> onGetLastGpsLocation(gpsLocation, position));

        }

    }

    private void onGetLastGpsLocation(GpsLocation gpsLocation, int position) {
        if (gpsLocation.getTimestamp() != null) {

            LatLng location = gpsLocationToLatLng(gpsLocation);
            Marker marker = userMarkers.get(position);
            marker.setPosition(location);
            marker.setVisible(true);
        }



        updateLastUpdatedTextView(gpsLocation, position);
    }

    private void updateLastUpdatedTextView(GpsLocation gpsLocation, int position) {
        ListView listView = findViewById(R.id.listView_trackUsers);
        View view = listView.getChildAt(position - listView.getFirstVisiblePosition());

        if (view == null) {
            return;
        }

        TextView textView = view.findViewById(R.id.trackUser_lastUpdated);
        GetLastUpdated getLastUpdated = new GetLastUpdated(this);
        String timestamp = gpsLocation.getTimestamp();
        textView.setText(getLastUpdated.getLastUpdatedString(timestamp));
    }


    private void getUsersFromServer() {
        Call<List<User>> caller = instance.getProxy().getUsers();
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onGetUsers(returnedUsers));
    }

    private void onGetUsers(List<User> returnedUsers) {
        // Get monitored users of current user
        monitorUsers = new ArrayList<>();
        for (int i = 0; i < returnedUsers.size(); i++) {
            User user = returnedUsers.get(i);
            if (UserListHelper.isOnMonitorsUserList(currentUser, user)) {
                monitorUsers.add(user);
            }
        }

        // Get leaders of current user
        List<User> leadersOfMonitorUsers = new ArrayList<>();
        for (int i = 0; i < monitorUsers.size(); i++) {
            for (int j = 0; j < returnedUsers.size(); j++) {
                if (UserListHelper.isGroupLeaderForCurrentUser(monitorUsers.get(i),returnedUsers.get(j))) {
                    leadersOfMonitorUsers.add(returnedUsers.get(j));
                }
            }
        }

        // Remove dupes using LinkedHasSet
        Set<User> listTrackUsersTemp = new LinkedHashSet<>();
        listTrackUsersTemp.addAll(monitorUsers);
        listTrackUsersTemp.addAll(leadersOfMonitorUsers);

        listTrackUsers.addAll(listTrackUsersTemp);
        listTrackUsers = UserListHelper.sortUsers(listTrackUsers);

        hideLoadingCircle();
        populateUserList();
        setupListOnItemClickListeners();
        populateUserMarkers();


    }

    private void populateUserList() {
        userListHelper = new UserListHelper(this, listTrackUsers, currentUser, monitorUsers);

        ArrayAdapter<User> adapter = userListHelper.getTrackerListAdapter();
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
        for (int i = 0; i < listTrackUsers.size(); i++) {
            User user = listTrackUsers.get(i);

            Marker marker;
            if (user.getLastGpsLocation().getTimestamp() != null) {
                 LatLng location = gpsLocationToLatLng(user.getLastGpsLocation());
                 marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(user.getName())
                     .visible(true));

            } else {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(0,0))
                        .title(user.getName())
                        .visible(false));
            }
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
                Marker marker = userMarkers.get(position);
                if (marker.isVisible()) {
                    moveCamera(userMarkers.get(position).getPosition(), CurrentLocationHelper.DEFAULT_ZOOM);
                }
            }
        });
    }



    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onBackPressed() {
      updateMarkersTimer.cancel();
      finish();
    }


}
