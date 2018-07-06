package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

/**
 * Display a listView of User to choose from based on currentUser and group
 * headerText shows the group name on top.
 * addOrRemove must be either "add" or "remove"
 */

public class SelectUserActivity extends AppCompatActivity {

    public static final String SELECT_USER_ACTIVITY_RETURN = "SelectUserActivity: return selected User";
    private Group group;
    private User currentUser;
    private List<User> userIdOnlyList;
    private List<User> userDetailedList;
    private String headerText;
    private String addOrRemove;
    private UserListHelper userListHelper;
    private Model model;

    public static Intent makeIntent(Context context, Group group, User currentUser, String headerText, String addOrRemove) {
        Intent intent = new Intent(context, SelectUserActivity.class);
        intent.putExtra("group", group);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("headerText", headerText);
        intent.putExtra("addOrRemove", addOrRemove);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        model = Model.getInstance();
        userDetailedList = new ArrayList<>();

        getDataFromIntent();
        initializeHeaderText();
        updateGroupDetails();
    }

    private void updateGroupDetails() {
        Call<List<Group>> caller = model.getProxy().getGroups();
        ProxyBuilder.callProxy(this, caller, returnedGroups -> onUpdateGroupDetailsRespond(returnedGroups));
    }

    private void onUpdateGroupDetailsRespond(List<Group> returnedGroups) {
        for (int i = 0; i < returnedGroups.size(); i++) {
            if (Objects.equals(group.getId(), returnedGroups.get(i).getId())) {
                group = returnedGroups.get(i);
                break;
            }
        }

        getUserList();
        setupCancelButton();
        initializeMessage();
        registerItemOnClick();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        group = (Group) intent.getSerializableExtra("group");
        currentUser = (User) intent.getSerializableExtra("currentUser");
        headerText = intent.getStringExtra("headerText");
        addOrRemove = intent.getStringExtra("addOrRemove");
    }

    private void getUserList() {
        switch (addOrRemove) {
            case "add":
                List<User> addableUsers = new ArrayList<>();
                if (!(group.isMember(currentUser))) {
                    if (!Objects.equals(group.getLeader().getId(), currentUser.getId())) {
                        addableUsers.add(currentUser);
                    }
                }

                for (int i = 0; i < currentUser.getMonitorsUsers().size(); i++) {
                    User user = currentUser.getMonitorsUsers().get(i);
                    if (!(group.isMember(user))) {
                        if (!Objects.equals(group.getLeader().getId(), user.getId())) {
                            addableUsers.add(user);
                        }
                    }
                }

                userIdOnlyList = addableUsers;
                getDetailedUserList();
                break;

            case "remove":
                List<User> removableUsers = new ArrayList<>();
                if (group.isLeader(currentUser)) {
                    removableUsers = group.getMemberUsers();
                } else {
                    if (group.isMember(currentUser)) {
                        removableUsers.add(currentUser);
                    }
                    for (int i = 0; i < currentUser.getMonitorsUsers().size(); i++) {
                        User user = currentUser.getMonitorsUsers().get(i);
                        if (group.isMember(user)) {
                            removableUsers.add(user);
                        }
                    }
                }

                userIdOnlyList = removableUsers;
                getDetailedUserList();
                break;

            default:
                userIdOnlyList = new ArrayList<>();
                break;
        }
    }

    private void getDetailedUserList() {
        for (int i = 0; i < userIdOnlyList.size(); i++) {
            Call<User> caller = model.getProxy().getUserById(userIdOnlyList.get(i).getId());
            ProxyBuilder.callProxy(this, caller, detailedUser -> onGetDetailedUserListResponse(detailedUser));
        }
    }

    private void onGetDetailedUserListResponse(User detailedUser) {
        userDetailedList.add(detailedUser);
        populateUserList();
    }

    private void setupCancelButton() {
        Button btn = findViewById(R.id.selectUser_cancelBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeHeaderText() {
        TextView headerTextView = findViewById(R.id.selectUser_headerText);
        headerTextView.setText(headerText);
    }

    private void initializeMessage() {
        TextView titleTextView = findViewById(R.id.selectUser_titleText);
        String titleText;
        if (userIdOnlyList.size() == 0) {
            titleText = "No available user to " + addOrRemove + ".";
        } else {
            titleText = "Select user to " + addOrRemove + ":";
        }
        titleTextView.setText(titleText);
    }

    private void populateUserList() {
        // Sort user list
        userDetailedList = UserListHelper.sortUsers(userDetailedList);

        userListHelper = new UserListHelper(SelectUserActivity.this, userDetailedList, currentUser);
        ArrayAdapter<User> adapter = userListHelper.getAdapter();
        ListView userListView = findViewById(R.id.selectUser_userList);
        userListView.setAdapter(adapter);
    }

    private void registerItemOnClick() {
        ListView userListView = findViewById(R.id.selectUser_userList);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User userClicked = userDetailedList.get(position);

                // Pass back userClicked to GroupDetailsActivity
                Intent intent = new Intent();
                intent.putExtra(SELECT_USER_ACTIVITY_RETURN, userClicked);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
