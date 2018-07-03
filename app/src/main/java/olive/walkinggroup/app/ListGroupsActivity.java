package olive.walkinggroup.app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class ListGroupsActivity extends AppCompatActivity {
    private Model model;
    private User user;
    private List<Group> userGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);

        setupTitle();

        model = Model.getInstance();
        user = (User) getIntent().getSerializableExtra("user");
        // Make user default to Model.currentUser if no User object is passed by intent
        if (user == null) {
            user = model.getCurrentUser();
        }

        // For testing only. Remove when able to add groups to user
        user = new User();
        user.setId((long) 1111);
        userGroups = makeTestGroups();

        // Guard against null User object, in case currentUser is null
        if (user != null) {
            // Commented until able to add groups to user
            // userGroups = getGroupList();

            populateGroupList();
            registerItemOnClick();
        }
    }

    private void setupTitle() {
        TextView title = findViewById(R.id.listGroups_headerTitle);
        String text = getText(R.string.listGroups_headerTitle).toString();

        // Added until issue #17 (Bug) is fixed
        text = "My " + text;

        // Commented until issue #17 (Bug) is fixed
//        if (model.getCurrentUser() != null) {
//            if (Objects.equals(user.getId(), model.getCurrentUser().getId())) {
//                text = "My " + text;
//            } else {
//                text = user.getName() + "'s" +text;
//            }
//        }

        title.setText(text);
    }

    private List<Group> getGroupList() {
        List<Group> leaderGroup = new ArrayList<>();

        if (user != null) {
            leaderGroup = user.getLeadsGroups();
            List<Group> memberGroup = user.getMemberOfGroups();
            leaderGroup.addAll(memberGroup);
        }
        return leaderGroup;
    }

    private List<Group> makeTestGroups() {
        // Walks from residence to fitness centre.
        User user1 = new User();
        user1.setName("Jim");
        user1.setId((long) 1111);
        List<User> memberList1 = new ArrayList<User>();
        memberList1.add(user1);

        Group group1 = new Group("Gym Group",
                "Work out together!",
                user1,
                new double[]{49.280628, 49.279460},
                new double[]{-122.928645, -122.922323},
                null);

        // Walks from AQ to Brian's office
        User user2 = new User();
        user2.setName("Chris");
        user2.setId((long) 2222);
        List<User> memberList2 = new ArrayList<User>();
        memberList2.add(user2);
        memberList2.add(user1);
        Group group2 = new Group("Finding Brian",
                "Ask all your questions in Brian's office hours.\nWe have a lot of questions to ask, so this description will also be very lengthy! (Max 3 lines shown here) \n But more can be seen in GroupDetailsActivity",
                user2,
                new double[] {49.278495, 49.276756},
                new double[] {-122.915911, -122.914109},
                memberList2);

        List<Group> groupList = new ArrayList<>();
        groupList.add(group1);
        groupList.add(group2);

        return groupList;
    }

    private void populateGroupList() {
        ArrayAdapter<Group> adapter = new MyListAdapter();
        ListView groupList = findViewById(R.id.viewMyGroups_groupList);
        groupList.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<Group> {
        public MyListAdapter() {
            super(ListGroupsActivity.this, R.layout.list_groups_item, userGroups);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_groups_item, parent, false);
            }
            Group currentGroup = userGroups.get(position);

            setupGroupNameView(itemView, currentGroup);
            setupGroupDescriptionView(itemView, currentGroup);
            setupNumMembersView(itemView, currentGroup);
            displayLeaderTag(itemView, currentGroup);

            return itemView;
        }
    }

    private void setupGroupNameView(View itemView, Group currentGroup) {
        TextView groupNameView = itemView.findViewById(R.id.groupListItem_groupName);
        groupNameView.setText(currentGroup.getGroupName());
    }

    private void setupGroupDescriptionView(View itemView, Group currentGroup) {
        TextView groupDescriptionView = itemView.findViewById(R.id.groupListItem_groupDescription);
        groupDescriptionView.setText(currentGroup.getGroupDescription());
    }

    private void setupNumMembersView(View itemView, Group currentGroup) {
        TextView numMembersView = itemView.findViewById(R.id.groupListItem_numMembers);
        int numMembers = 0;

        if (currentGroup.getMembers() != null) {
            numMembers = currentGroup.getMembers().size();
        }
        String numMembersText = "" + numMembers;
        numMembersView.setText(numMembersText);
    }

    private void displayLeaderTag(View itemView, Group currentGroup) {
        RelativeLayout leaderTag = itemView.findViewById(R.id.groupListItem_leaderTag);
        // Hide leader tag if user is not leader of currentGroup
        if (!(Objects.equals(user.getId(), currentGroup.getLeader().getId()))) {
            leaderTag.setVisibility(View.INVISIBLE);
        }
    }

    private void registerItemOnClick() {
        ListView groupList = findViewById(R.id.viewMyGroups_groupList);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group clickedGroup = userGroups.get(position);

                Intent intent = new Intent(ListGroupsActivity.this, GroupDetailsActivity.class);
                intent.putExtra("group", clickedGroup);
                startActivity(intent);
            }
        });
    }
}
