package olive.walkinggroup.app;

        import android.app.Activity;
        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
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

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Objects;

        import olive.walkinggroup.R;
        import olive.walkinggroup.dataobjects.Group;
        import olive.walkinggroup.dataobjects.Model;
        import olive.walkinggroup.dataobjects.User;
        import olive.walkinggroup.dataobjects.UserListHelper;
        import olive.walkinggroup.proxy.ProxyBuilder;
        import retrofit2.Call;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_ADD = 6568;
    private static final int REQUEST_CODE_REMOVE = 8269;
    public static final String TAG = "GroupDetailsActivity";
    public static final float DEFAULT_ZOOM = 1f;

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
        updateGroupDetails();

        setupAddUserButton();
        setupRemoveUserButton();
        initializeText();
        initializeMap();
        setupListHeader();
    }

    private void updateGroupDetails() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(this, caller, returnedGroups -> onUpdateGroupDetailsProxyResponse(returnedGroups));
    }

    private void onUpdateGroupDetailsProxyResponse(List<Group> returnedGroups) {
        for (int i = 0; i < returnedGroups.size(); i++) {
            if (Objects.equals(group.getId(), returnedGroups.get(i).getId())) {
                group = returnedGroups.get(i);
                break;
            }
        }

        buildMemberList();
    }

    private void buildMemberList() {
        memberList = new ArrayList<>();
        List<User> userList = group.getMemberUsers();

        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);

            Call<User> caller = model.getProxy().getUserById(user.getId());
            ProxyBuilder.callProxy(this, caller, detailedUser -> onBuildMemberListProxyResponse(detailedUser));
        }
    }

    private void onBuildMemberListProxyResponse(User detailedUser) {
        if (detailedUser != null) {
            Log.d(TAG, "Showing user on Member list: " + detailedUser.toString());

            memberList.add(detailedUser);
            populateMemberList();
        }
    }

    private void setupAddUserButton() {
        RelativeLayout btn = findViewById(R.id.groupDetail_addBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectUserActivity.makeIntent(GroupDetailsActivity.this, group, currentUser, group.getGroupName(), "add");
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });
    }

    private void setupRemoveUserButton() {
        RelativeLayout btn = findViewById(R.id.groupDetail_removeBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectUserActivity.makeIntent(GroupDetailsActivity.this, group, currentUser, group.getGroupName(), "remove");
                startActivityForResult(intent, REQUEST_CODE_REMOVE);
            }
        });
    }

    private void initializeText() {
        TextView groupName = findViewById(R.id.groupDetail_groupName);
        groupName.setText(group.getGroupDescription());
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
        moveCamera(group.getStartPoint(), DEFAULT_ZOOM);
    }

    private void populateMemberList() {
        userListHelper = new UserListHelper(this, memberList, currentUser);
        ArrayAdapter<User> adapter = userListHelper.getAdapter();
        ListView memberListView = findViewById(R.id.groupDetail_memberList);
        memberListView.setAdapter(adapter);
    }

    private void setupListHeader() {

        if (memberList == null) {
            forceShowHeader();
        }
        ListView memberListView = findViewById(R.id.groupDetail_memberList);
        View headerView = getLayoutInflater().inflate(R.layout.list_members_header, memberListView, false);
        memberListView.addHeaderView(headerView);
    }

    // Force list header to display with null memberList
    private void forceShowHeader() {
        memberList = new ArrayList<>();
        memberList.add(new User());
        populateMemberList();
        memberList.remove(0);
        populateMemberList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD:
                if (resultCode == Activity.RESULT_OK) {
                    User userToAdd = (User) data.getSerializableExtra(SelectUserActivity.SELECT_USER_ACTIVITY_RETURN);
                    memberList.add(userToAdd);
                    populateMemberList();

                    // TODO: Uncomment when testing with server is ready
                    addNewMemberToServer(userToAdd);
                    break;
                }

            case REQUEST_CODE_REMOVE:
                if (resultCode == Activity.RESULT_OK) {
                    User userToRemove = (User) data.getSerializableExtra(SelectUserActivity.SELECT_USER_ACTIVITY_RETURN);
                    int index = group.getMemberListIndex(userToRemove);
                    if (index >= 0) {
                        memberList.remove(index);
                    }
                    populateMemberList();

                    // TODO: Uncomment when testing with server is ready
                    removeMemberFromGroup(userToRemove);
                    break;
                }

            default:
                break;
        }
    }

    private void addNewMemberToServer(User user) {
        Call<List<User>> caller = model.getProxy().addGroupMember(group.getId(), user);
        ProxyBuilder.callProxy(this, caller, listOfMembers -> onAddNewMemberResponse(listOfMembers));
    }

    private void onAddNewMemberResponse(List<User> listOfMembers) {
        updateGroupDetails();
        Log.d(TAG, "Added user to group. New group member list:\n\n\n" + listOfMembers.toString());
    }

    private void removeMemberFromGroup(User user) {
        Call<Void> caller = model.getProxy().removeGroupMember(group.getId(), user.getId());
        ProxyBuilder.callProxy(this, caller, returnNothing -> onRemoveMemberResponse(returnNothing));
    }

    private void onRemoveMemberResponse(Void returnNothing) {
        updateGroupDetails();
        Log.d(TAG, "Removed user from group.");
    }
}
