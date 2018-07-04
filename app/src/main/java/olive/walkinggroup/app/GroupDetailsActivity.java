package olive.walkinggroup.app;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.RelativeLayout;
        import android.widget.TextView;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;

        import java.util.List;

        import olive.walkinggroup.R;
        import olive.walkinggroup.dataobjects.Group;
        import olive.walkinggroup.dataobjects.Model;
        import olive.walkinggroup.dataobjects.User;
        import olive.walkinggroup.dataobjects.UserListHelper;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Group group;
    private List<User> memberList;

    private Model model;
    private User currentUser;
    private UserListHelper userListHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        model = Model.getInstance();
        // Commented until bug #17 is fixed
        //currentUser = model.getCurrentUser();

        // Temporary currentUser for testing (Bob)
        currentUser = FindGroupsActivity.getBob();

        group = (Group) getIntent().getSerializableExtra("group");
        memberList = group.getMembers();

        userListHelper = new UserListHelper(this, memberList, currentUser);

        setupAddUserButton();
        setupRemoveUserButton();
        initializeText();
        initializeMap();
        setupListHeader();
        populateMemberList();
    }

    private void setupAddUserButton() {
        RelativeLayout btn = findViewById(R.id.groupDetail_addBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectUserActivity.makeIntent(GroupDetailsActivity.this, group, currentUser, group.getGroupName(), "add");
                startActivity(intent);
            }
        });
    }

    private void setupRemoveUserButton() {
        RelativeLayout btn = findViewById(R.id.groupDetail_removeBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectUserActivity.makeIntent(GroupDetailsActivity.this, group, currentUser, group.getGroupName(), "remove");
                startActivity(intent);
            }
        });
    }

    private void initializeText() {
        TextView groupName = findViewById(R.id.groupDetail_groupName);
        TextView groupLeader = findViewById(R.id.groupDetail_groupLeader);
        TextView groupDescription = findViewById(R.id.groupDetail_groupDescription);

        groupName.setText(group.getGroupName());
        String leaderText = "Leader: "+ group.getLeader().getName();
        groupLeader.setText(leaderText);
        groupDescription.setText(group.getGroupDescription());
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.groupDetail_map);
        mapFragment.getMapAsync(GroupDetailsActivity.this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Mark meet-up location on map as Green Marker
        Marker startPoint = mMap.addMarker(new MarkerOptions()
                .position(group.getStartPoint())
                .title("Meet-up")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        startPoint.showInfoWindow();
        // Mark walk destination on map as Red Marker
        Marker endPoint = mMap.addMarker(new MarkerOptions()
                .position(group.getEndPoint())
                .title("Destination"));
        // Focus camera on meet-up location
        moveCamera(group.getStartPoint(), 15f);
    }

    private void populateMemberList() {
        ArrayAdapter<User> adapter = userListHelper.getAdapter();
        ListView memberList = findViewById(R.id.groupDetail_memberList);
        memberList.setAdapter(adapter);
    }

    private void setupListHeader() {
        ListView memberList = findViewById(R.id.groupDetail_memberList);
        View headerView = getLayoutInflater().inflate(R.layout.list_members_header, memberList, false);
        memberList.addHeaderView(headerView);
    }
}
