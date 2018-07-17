package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
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
*   Display all groups a currentUser is leading, is member of, and all groups that the
*   users monitored by currentUser are members of.
*   On list item click, GroupDetailsActivity is launched for the corresponding group.
*/

public class ListGroupsActivity extends AppCompatActivity {
    public static final String TAG = "ListGroupsActivity";
    private Model model = Model.getInstance();
    private User currentUser = model.getCurrentUser();
    private List<Group> userGroups;
    private List<Group> monitorUserGroupsList;
    private boolean isReload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_groups);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();
        userGroups = new ArrayList<>();
        monitorUserGroupsList = new ArrayList<>();

        if (currentUser != null) {
            updateUserInfo();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isReload) {
            if (currentUser != null) {
                showLoadingCircle();
                updateUserInfo();
            }
        }
    }

    private void updateUserInfo() {
        Log.d(TAG, "updateUserInfo");
        Call<User> caller = model.getProxy().getUserById(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, updatedUser -> onUpdateUserInfoResponse(updatedUser));
    }

    private void onUpdateUserInfoResponse(User updatedUser) {
        Log.d(TAG, "onUpdateUserInfoResponse");
        currentUser = updatedUser;

        if (currentUser.getMonitorsUsers().size() == 0) {
            getGroupDetails();
        }
        getCurrentUserGroupList();
    }

    private void getCurrentUserGroupList() {
        Log.d(TAG, "getCurrentUserGroupList");
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
        Log.d(TAG, "getCurrentUserGroupList: assign userGroups");
        userGroups = groupList;

        if (currentUser != null) {
            if (currentUser.getMonitorsUsers() != null) {
                if (currentUser.getMonitorsUsers().size() == 0) {
                    // Skip getting monitor user details if currentUser does not monitor anyone
                    getGroupDetails();
                }
                // TODO: For issue #55: currently omitting monitor tag.
                //getMonitorsUserList();
                getGroupDetails();
            }
        }
    }

    private boolean groupIsInList(List<Group> groupList, Group group) {
        List<Integer> groupListId = new ArrayList<>();

        for (int i = 0; i < groupList.size(); i++) {
            groupListId.add(groupList.get(i).getId().intValue());
        }
        return (groupListId.contains(group.getId().intValue()));
    }

    private void getMonitorsUserList() {
        Log.d(TAG, "getMonitorsUserList");

        Call<List<User>> caller = model.getProxy().getMonitorsUsers(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, returnedList -> getMonitorsUserListWithFullDetails(returnedList));
    }

    private void getMonitorsUserListWithFullDetails(List<User> monitorsUserIdList) {
        Log.d(TAG, "getMonitorsUserListWithFullDetails");
        monitorUserGroupsList = new ArrayList<>();

        for (int i = 0; i < monitorsUserIdList.size(); i++) {
            Call<User> caller = model.getProxy().getUserById(monitorsUserIdList.get(i).getId());
            ProxyBuilder.callProxy(this, caller, detailedUser -> getMonitorUserGroupList(detailedUser));
        }
    }

    private void getMonitorUserGroupList(User detailedUser) {
        Log.d(TAG, "getMonitorUserGroupList");
        List<Group> monitorUserGroups = detailedUser.getMemberOfGroups();

        for (int i = 0; i < monitorUserGroups.size(); i++) {
            Group group = monitorUserGroups.get(i);

            if (!(groupIsInList(userGroups, group))) {
                userGroups.add(group);
            }

            if (!(groupIsInList(monitorUserGroupsList, group))) {
                Log.d(TAG, "getMonitorUserGroupList: putting group into monitorUserGroupList");
                monitorUserGroupsList.add(group);
            }
        }
        getGroupDetails();
    }

    private void getGroupDetails() {
        Log.d(TAG, "getGroupDetails");
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(this, caller, returnedGroups -> onGetGroupDetailsResponse(returnedGroups));
    }

    private void onGetGroupDetailsResponse(List<Group> returnedGroups) {
        Log.d(TAG, "onGetGroupDetailsResponse");
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
        Log.d(TAG, "populateGroupList");
        ArrayAdapter<Group> adapter = new GroupListAdapter();
        ListView groupList = findViewById(R.id.listGroups_groupList);
        groupList.setAdapter(adapter);
        hideLoadingCircle();
        isReload = true;
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

            setupButtons(itemView, currentGroup);
            setupGroupDescriptionView(itemView, currentGroup);
            setupNumMembersView(itemView, currentGroup);
            displayTags(itemView, currentGroup);

            return itemView;
        }
    }

    private void setupButtons(View itemView, Group currentGroup) {
        Button btn = itemView.findViewById(R.id.btn_setActiveGroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);

                model.setActiveGroup(currentGroup);

                finish();
            }
        });
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
        ListView groupList = findViewById(R.id.listGroups_groupList);
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

    private void showLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.listGroups_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.listGroups_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
        }
    }
}
