package olive.walkinggroup.app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
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
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;
/*
*   Display all groups a currentUser is leading and is member of.
*   Launch with intent and attach a User object with tag "currentUser" to show the groups of that currentUser.
*   If no User object is passed through intent, the currentUser from Model is used instead.
*   On list item click, GroupDetailsActivity is launched for the corresponding group.
*/

public class ListGroupsActivity extends AppCompatActivity {
    public static final String TAG = "ListGroupsActivity";
    private Model model;
    private User currentUser;
    private List<Group> userGroups;
    private List<Group> monitorUserGroupsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();

        userGroups = new ArrayList<>();
        monitorUserGroupsList = new ArrayList<>();

        // TODO: get currentUser from model. Now set to Bob just for testing...
        //currentUser = FindGroupsActivity.getBob();

        // Guard against null User object, in case currentUser is null
        if (currentUser != null) {
            updateUserInfo();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (currentUser != null) {
                updateUserInfo();
            }
        }
    }

    private void updateUserInfo() {
        Call<User> caller = model.getProxy().getUserById(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, updatedUser -> onUpdateUserInfoResponse(updatedUser));
    }

    private void onUpdateUserInfoResponse(User updatedUser) {
        currentUser = updatedUser;
        getCurrentUserGroupList();
    }

    private void getCurrentUserGroupList() {
        List<Group> groupList = new ArrayList<>();

        if (currentUser != null) {
            groupList = currentUser.getLeadsGroups();
            List<Group> memberGroup = currentUser.getMemberOfGroups();
            // Prevent duplicates
            for (int i = 0; i < memberGroup.size(); i++) {
                Group currentGroup = memberGroup.get(i);
                if (!(groupIsInList(groupList, currentGroup))) {
                    groupList.add(currentGroup);
                }
            }
        }
        userGroups = groupList;
        getMonitorUserGroupList();
    }

    // Add the groups which the users currentUser monitors are member of to userGroups
    private void getMonitorUserGroupList() {
        //List<User> monitorUsers = currentUser.getMonitorsUsers();
        // TODO: uncomment above and remove below when able to add monitor list to server
        List<User> monitorUsers = FindGroupsActivity.getBob().getMonitorsUsers();

        Log.d(TAG, "getMonitorUserGroupList: monitorUsers " + monitorUsers.toString());
        for (int i = 0; i < monitorUsers.size(); i++) {
            List<Group> monitorUserGroups = monitorUsers.get(i).getMemberOfGroups();

            for (int j = 0; j < monitorUserGroups.size(); j++) {
                Group group = monitorUserGroups.get(j);
                if (!(groupIsInList(userGroups, group))) {
                    userGroups.add(group);
                }

                if (!(groupIsInList(monitorUserGroupsList, group))) {
                    monitorUserGroupsList.add(group);
                }
            }
        }
        getGroupDetails();
    }

    private boolean groupIsInList(List<Group> groupList, Group group) {
        List<Integer> groupListId = new ArrayList<>();
        for (int i = 0; i < groupList.size(); i++) {
            groupListId.add(groupList.get(i).getId().intValue());
        }
        return (groupListId.contains(group.getId().intValue()));
    }

    private void getGroupDetails() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(this, caller, returnedGroups -> onGetGroupDetailsResponse(returnedGroups));
    }

    private void onGetGroupDetailsResponse(List<Group> returnedGroups) {
        List<Group> groupList = new ArrayList<>();
        for (int i = 0; i < returnedGroups.size(); i++) {
            for (int j = 0; j < userGroups.size(); j++) {
                if (Objects.equals(returnedGroups.get(i).getId(), userGroups.get(j).getId())) {
                    groupList.add(returnedGroups.get(i));
                }
            }
        }
        userGroups = groupList;
        populateGroupList();
        registerItemOnClick();
    }

    private void populateGroupList() {
        ArrayAdapter<Group> adapter = new GroupListAdapter();
        ListView groupList = findViewById(R.id.viewMyGroups_groupList);
        groupList.setAdapter(adapter);
    }

    private class GroupListAdapter extends ArrayAdapter<Group> {
        public GroupListAdapter() {
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

            setupGroupDescriptionView(itemView, currentGroup);
            setupNumMembersView(itemView, currentGroup);
            displayTags(itemView, currentGroup);

            return itemView;
        }
    }

    private void setupGroupDescriptionView(View itemView, Group currentGroup) {
        TextView groupDescriptionView = itemView.findViewById(R.id.groupListItem_groupDescription);
        groupDescriptionView.setText(currentGroup.getGroupDescription());
    }

    private void setupNumMembersView(View itemView, Group currentGroup) {
        TextView numMembersView = itemView.findViewById(R.id.groupListItem_numMembers);
        int numMembers = 0;

        if (currentGroup.getMemberUsers() != null) {
            numMembers = currentGroup.getMemberUsers().size();
        }
        String numMembersText = "" + numMembers;
        numMembersView.setText(numMembersText);
    }

    private void displayTags(View itemView, Group currentGroup) {
        RelativeLayout leaderTag = itemView.findViewById(R.id.groupListItem_leaderTag);
        if (currentGroup.getLeader() != null) {
            if (!(Objects.equals(currentUser.getId(), currentGroup.getLeader().getId()))) {
                leaderTag.setVisibility(View.GONE);
            }
        }

        RelativeLayout monitorTag = itemView.findViewById(R.id.groupListItem_monitorTag);
        if (!(groupIsInList(monitorUserGroupsList, currentGroup))) {
            monitorTag.setVisibility(View.GONE);
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
