package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.dataobjects.UserListHelper;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;


public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<User> monitorUsers;

    private Model instance;
    private User currentUser;

    private UserListHelper userListHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();

        getMonitorUsersFromServer();
        initializeMap();

    }

    private void getMonitorUsersFromServer() {
        Call<List<User>> caller = instance.getProxy().getMonitorsUsers(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onGetMonitorUsers(returnedUsers));
    }

    private void onGetMonitorUsers(List<User> returnedUsers) {
        monitorUsers = UserListHelper.sortUsers(returnedUsers);

        populateUserList();
    }

    private void populateUserList() {
        userListHelper = new UserListHelper(this, monitorUsers, currentUser);

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
}
