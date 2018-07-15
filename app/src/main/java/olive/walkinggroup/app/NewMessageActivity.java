package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.MessageHelper;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class NewMessageActivity extends AppCompatActivity {
    private Model model;
    private User currentUser;
    private Spinner dropdown;

    // Note: contactIdList may not be aligned with the other 2 contact lists
    private List<Integer> contactIdList = new ArrayList<>();
    private List<Integer> groupIdList = new ArrayList<>();
    private List<Group> groupDetailedList = new ArrayList<>();
    private List<User> contactDetailedList = new ArrayList<>();
    private List<String> contactLabelList = new ArrayList<>();

    private List<Integer> messageContactIdList = new ArrayList<>();
    private List<List<Message>> contactMessagesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        dropdown = findViewById(R.id.messagesActivity_toUserDropdown);
        model = Model.getInstance();
        currentUser = model.getCurrentUser();
        getContactIdList();
    }

    // Contacts include:
    // - Users who I monitor/ monitors me
    // - Users who are leaders of groups I'm in
    // - Members of groups I lead are in another list
    private void getContactIdList() {
        List<User> monitorList = currentUser.getMonitoredByUsers();
        monitorList.addAll(currentUser.getMonitorsUsers());

        for (int i = 0; i < monitorList.size(); i++) {
            Integer id = monitorList.get(i).getId().intValue();
            if (!contactIdList.contains(id)) {
                contactIdList.add(id);
            }
        }
        List<Group> memberOfGroups = currentUser.getMemberOfGroups();

        for (int i = 0; i < memberOfGroups.size(); i++) {
            groupIdList.add(memberOfGroups.get(i).getId().intValue());
        }

        getGroupDetailedList();
    }

    private void getGroupDetailedList() {
        for (int i = 0; i < groupIdList.size(); i++) {
            long id = groupIdList.get(i);
            Call<Group> caller = model.getProxy().getGroupById(id);
            ProxyBuilder.callProxy(this, caller, returnedGroup -> onGetGroupDetailedListResponse(returnedGroup));
        }
    }

    private void onGetGroupDetailedListResponse(Group returnedGroup) {
        groupDetailedList.add(returnedGroup);
        contactIdList.add(returnedGroup.getLeader().getId().intValue());

        if (groupDetailedList.size() == groupIdList.size()) {
            getContactDetailedList();
        }
    }

    private void getContactDetailedList() {
        for (int i = 0; i < contactIdList.size(); i++) {
            Call<User> caller = model.getProxy().getUserById((long) contactIdList.get(i));
            ProxyBuilder.callProxy(this, caller, returnedUser -> onGetContactListResponse(returnedUser));
        }
    }

    private void onGetContactListResponse(User returnedUser) {
        contactDetailedList.add(returnedUser);

        if (contactDetailedList.size() == contactIdList.size()) {
            buildContactLabelList();
        }
    }

    private void buildContactLabelList() {
        for (int i = 0; i < contactDetailedList.size(); i++) {
            contactLabelList.add(contactDetailedList.get(i).getName());
        }

        buildContactMessagesList();
    }

    private void buildContactMessagesList() {
        Call<List<Message>> caller = model.getProxy().getMessages();
        ProxyBuilder.callProxy(this, caller, fullMessagesList -> onBuildContactMessagesListResponse(fullMessagesList));
    }

    private void onBuildContactMessagesListResponse(List<Message> fullMessagesList) {
        for (int i = 0; i < fullMessagesList.size(); i++) {
            Message message = fullMessagesList.get(i);
            long messageContactLongId = MessageHelper.getMessageContactId(message);
            Integer messageContactId = (int) (long) messageContactLongId;

            if (messageContactIdList.contains(messageContactId)) {
                List<Message> list = contactMessagesList.get(messageContactIdList.indexOf(messageContactId));
                list.add(message);
            } else {
                List<Message> newList = new ArrayList<>();
                newList.add(message);
                contactMessagesList.add(newList);
                messageContactIdList.add(messageContactId);
            }
        }

        setupToUserDropdown();
        setupNewMessageBtn();
    }

    private void setupNewMessageBtn() {
        RelativeLayout btn = findViewById(R.id.messagesActivity_newMessageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // contactDetailedList and contactLabelList are aligned
                User selectedUser = contactDetailedList.get(dropdown.getSelectedItemPosition());
                Integer selectedUserId = selectedUser.getId().intValue();
                List<Message> messageList = contactMessagesList.get(messageContactIdList.indexOf(selectedUserId));

                //Intent intent = ChatActivity.makeIntent(NewMessageActivity.this, messageList, selectedUser);
                //startActivity(intent);
                finish();
            }
        });
    }

    private void setupToUserDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, contactLabelList);
        dropdown.setAdapter(adapter);
    }
}
