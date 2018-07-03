package olive.walkinggroup.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class ViewMyGroupsActivity extends AppCompatActivity {
    private Model model;
    private User currentUser;
    private List<Group> userGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_groups);

        model = Model.getInstance();
        //currentUser = model.getCurrentUser();
        //userGroups = currentUser.getMemberOfGroups();
        currentUser = new User();
        currentUser.setId((long) 1111);
        userGroups = makeTestGroups();

        populateGroupList();
        registerItemOnClick();
    }

    private List<Group> makeTestGroups() {
        // Walks from residence to fitness centre.
        User user1 = new User();
        user1.setName("Jim");
        user1.setId((long) 1111);
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
                "Ask all your questions in Brian's office hours.",
                user2,
                new double[] {49.278495, 49.276756},
                new double[] {-122.915911, -122.914109},
                null);

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
            super(ViewMyGroupsActivity.this, R.layout.view_my_groups_group_list_item, userGroups);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.view_my_groups_group_list_item, parent, false);
            }
            Group currentGroup = userGroups.get(position);

            TextView groupNameView = itemView.findViewById(R.id.groupListItem_groupName);
            TextView groupDescriptionView = itemView.findViewById(R.id.groupListItem_groupDescription);

            groupNameView.setText(currentGroup.getGroupName());
            groupDescriptionView.setText(currentGroup.getGroupDescription());

            return itemView;
        }
    }

    private void registerItemOnClick() {
        ListView groupList = findViewById(R.id.viewMyGroups_groupList);
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
