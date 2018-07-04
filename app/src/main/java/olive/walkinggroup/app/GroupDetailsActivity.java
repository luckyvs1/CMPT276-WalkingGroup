package olive.walkinggroup.app;

        import android.content.Intent;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
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

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Group group;
    private List<User> memberList;

    private Model model;
    private User currentUser;

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

        setupJoinGroupBtn();
        initializeText();
        initializeMap();
        populateMemberList();
        setupListHeader();
    }

    private void setupJoinGroupBtn() {
        Button btn = findViewById(R.id.groupDetail_joinGroupBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailsActivity.this, ListUsersActivity.class);
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
        ArrayAdapter<User> adapter = new MemberListAdapter();
        ListView memberList = findViewById(R.id.groupDetail_memberList);
        memberList.setAdapter(adapter);
    }

    private class MemberListAdapter extends ArrayAdapter<User> {
        public MemberListAdapter() {
            super(GroupDetailsActivity.this, R.layout.list_user_item, memberList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_user_item, parent, false);
            }
            User currentMember = memberList.get(position);

            setupMemberNameView(itemView, currentMember);
            setupMemberEmailView(itemView, currentMember);
            displayTag(itemView, currentMember);

            return itemView;
        }
    }

    private void setupMemberNameView(View itemView, User currentMember) {
        TextView nameView = itemView.findViewById(R.id.listMembers_name);
        String nameText = currentMember.getName();
        nameView.setText(nameText);
    }

    private void setupMemberEmailView(View itemView, User currentMember) {
        TextView emailView = itemView.findViewById(R.id.listMembers_email);
        String emailText = currentMember.getEmail();
        emailView.setText(emailText);
    }

    private void displayTag(View itemView, User currentMember) {
        // Hide the youTag if currentUser is not currentMember (compared by Id)
        if (!(Objects.equals(currentUser.getId(), currentMember.getId()))) {
            RelativeLayout youTag = itemView.findViewById(R.id.listUsers_youTag);
            youTag.setVisibility(View.GONE);
        }
        // Hide the monitorTag if currentMember is not on monitor list of currentUser
        if (!(isOnMonitorsUserList(currentUser, currentMember))) {
            RelativeLayout monitorTag = itemView.findViewById(R.id.listUsers_monitorTag);
            monitorTag.setVisibility(View.GONE);
        }
    }

    // Return true if user2 is on List<User> monitorsUsers of user1
    private boolean isOnMonitorsUserList(User user1, User user2) {
        List<User> monitorList = user1.getMonitorsUsers();
        List<Integer> idList = new ArrayList<>();

        for (int i = 0; i < monitorList.size(); i++) {
            Integer id = monitorList.get(i).getId().intValue();
            idList.add(id);
        }

        return (idList.contains(user2.getId().intValue()));
    }

    private void setupListHeader() {
        ListView memberList = findViewById(R.id.groupDetail_memberList);
        View headerView = getLayoutInflater().inflate(R.layout.list_members_header, memberList, false);
        memberList.addHeaderView(headerView);
    }
}
