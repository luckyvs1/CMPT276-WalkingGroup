package olive.walkinggroup.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.MessageHelper;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class MessagesActivity extends AppCompatActivity {
    public static final String TAG = "MessagesActivity";
    private static final int AUTO_REFRESH_PERIOD = 60000;

    private List<List<Message>> myGroupedMessagesList = new ArrayList<>();
    private List<Message> displayList = new ArrayList<>();
    private List<Integer> contactIdList = new ArrayList<>();
    private List<User> detailedContactList = new ArrayList<>();

    private List<Group> userLeadList = new ArrayList<>();
    private List<Group> detailedLeadList = new ArrayList<>();
    private List<String> dropdownLabelList = new ArrayList<>();

    private Model model;
    private User currentUser;
    private Spinner dropdown;
    private Boolean isMemberOrChild;
    private SimpleDateFormat format;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();
        format = new SimpleDateFormat("MMM dd (EEE) hh:mm aaa", Locale.getDefault());
        dropdown = findViewById(R.id.messagesActivity_toUserDropdown);

        handler.post(ReloadUIRunnable);
        showLoadingCircle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingCircle();
        reloadUI();
    }

    private Runnable ReloadUIRunnable = new Runnable() {
        @Override
        public void run() {
            reloadUI();
            handler.postDelayed(ReloadUIRunnable, AUTO_REFRESH_PERIOD);
        }
    };

    private void reloadUI() {
        myGroupedMessagesList = new ArrayList<>();
        displayList = new ArrayList<>();
        contactIdList = new ArrayList<>();
        detailedContactList = new ArrayList<>();
        userLeadList = new ArrayList<>();
        detailedLeadList = new ArrayList<>();
        dropdownLabelList = new ArrayList<>();

        getMyMessages();
        setupNewMessagesBtn();
        setupDropdownList();
    }

    // Setup UI elements:
    // ---------------------------------------------------------------------------------------------
    private void setupDropdownList() {
        // TODO: If currentUser is not a member / leader of any groups, are they still able to send any messages?
        // (Currently they cannot. Change the following methods if otherwise)
//        if (currentUser.getMemberOfGroups().size() == 0 && currentUser.getLeadsGroups().size() == 0) {
//            return;
//        }

        buildDetailedGroupList();
    }

    private void buildDetailedGroupList() {
        userLeadList = currentUser.getLeadsGroups();
        // Skip getting group name if currentUser does not lead any group
        if (userLeadList.size() == 0) {
            buildDropdownLabelList();
            return;
        }

        for (int i = 0; i < userLeadList.size(); i++) {
            Call<Group> caller = model.getProxy().getGroupById(userLeadList.get(i).getId());
            ProxyBuilder.callProxy(this, caller, detailedGroup -> onBuildDetailedGroupListResponse(detailedGroup));
        }
    }

    private void onBuildDetailedGroupListResponse(Group detailedGroup) {
        detailedLeadList.add(detailedGroup);

        if (detailedLeadList.size() == userLeadList.size()) {
            buildDropdownLabelList();
        }
    }

    private void buildDropdownLabelList() {
        isMemberOrChild = false;

        if (currentUser.getMemberOfGroups().size() > 0 || currentUser.getMonitoredByUsers().size() > 0) {
            dropdownLabelList.add("My Parents & Leaders");
            isMemberOrChild = true;

            Log.d(TAG, "User is a member of groups");
        }

        for (int i = 0; i < detailedLeadList.size(); i++) {
            dropdownLabelList.add(detailedLeadList.get(i).getGroupDescription());
        }
        populateDropdownList();
    }

    private void populateDropdownList() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dropdownLabelList);
        dropdown.setAdapter(adapter);
    }

    private void setupNewMessagesBtn() {
        RelativeLayout btn = findViewById(R.id.messagesActivity_newMessageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model.setMessageList(new ArrayList<>());
                int dropdownSelectedPosition = dropdown.getSelectedItemPosition();
                // If dropdown list is empty, disable onClick
                if (dropdownSelectedPosition < 0) {
                    return;
                }

                // List would have 1 more item if currentUser is a child or member of a group
                int positionDisplacement = 0;
                if (isMemberOrChild) {
                    positionDisplacement = 1;
                }

                if (isMemberOrChild && dropdownSelectedPosition == 0) {
                    // Send to "My Parents & Leaders"
                    Intent intent = ChatActivity.makeIntent(MessagesActivity.this, "My Parents & Leaders", false, false, false, null);
                    startActivity(intent);
                    return;
                }

                // Send to a group currentUser leads
                Group sendToGroup = detailedLeadList.get(dropdownSelectedPosition - positionDisplacement);
                Intent intent = ChatActivity.makeIntent(MessagesActivity.this, sendToGroup.getGroupDescription(), false, false, true, sendToGroup);
                startActivity(intent);
            }
        });
    }

    // Message List server calls:
    // ---------------------------------------------------------------------------------------------

    private void getMyMessages() {
        Call<List<Message>> caller = model.getProxy().getMessages(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetMyMessagesResponse(returnedList));
    }

    private void onGetMyMessagesResponse(List<Message> returnedList) {
        myGroupedMessagesList = MessageHelper.groupByContact(removeSelfMessages(returnedList));
        myGroupedMessagesList = MessageHelper.sortMessageListOfList(myGroupedMessagesList);

        buildContactIdList();
    }

    // Remove messages sent by currentUser to currentUser
    private List<Message> removeSelfMessages(List<Message> messageList) {
        List<Message> returnList = new ArrayList<>();

        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            // Mark self-messages as read in background
            if (Objects.equals(message.getFromUser().getId(), currentUser.getId())) {
                Call<Message> caller = model.getProxy().markMessageAsRead(message.getId(), true);
                ProxyBuilder.callProxy(MessagesActivity.this, caller, null);
            } else {
                returnList.add(message);
            }
        }

        return returnList;
    }

    // Get id of each contact
    private void buildContactIdList() {
        contactIdList = new ArrayList<>();

        for (int i = 0; i < myGroupedMessagesList.size(); i++) {
            Message currentListHead = myGroupedMessagesList.get(i).get(0);
            long id = MessageHelper.getMessageContactId(currentListHead);
            contactIdList.add((int) id);
        }
        getDetailedContactList();
    }

    // Get detailed info of each contact (User)
    private void getDetailedContactList() {
        detailedContactList = new ArrayList<>();

        for (int i = 0; i < contactIdList.size(); i++) {
            Call<User> caller = model.getProxy().getUserById((long) contactIdList.get(i));
            ProxyBuilder.callProxy(this, caller, detailedUser -> onGetDetailedContactListResponse(detailedUser));
        }
    }

    private void onGetDetailedContactListResponse(User detailedUser) {
        detailedContactList.add(detailedUser);

        if (detailedContactList.size() == contactIdList.size()) {
            buildDisplayList();
        }
    }

    // Get latest Message from each contact, from index 0 of sorted Message Lists.
    private void buildDisplayList() {
        for (int i = 0; i < myGroupedMessagesList.size(); i++) {
            displayList.add(myGroupedMessagesList.get(i).get(0));
        }

        populateList();
    }

    // Populate Message ListView:
    // ---------------------------------------------------------------------------------------------
    private void populateList() {
        ListView listView = findViewById(R.id.messagesActivity_messagesList);
        MyMessagesListAdapter adapter = new MyMessagesListAdapter();
        listView.setAdapter(adapter);

        registerItemOnClick();
        hideLoadingCircle();
    }

    //PRECOND: displayList contains full User details
    private class MyMessagesListAdapter extends ArrayAdapter<Message> {
        public MyMessagesListAdapter() {
            super(MessagesActivity.this, R.layout.list_message_item, displayList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;

            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_message_item, parent, false);
            }
            Message currentMessage = displayList.get(position);
            Boolean isRead = currentMessage.isRead();

            TextView contactNameTextView = itemView.findViewById(R.id.listMessageItem_userName);
            TextView messageHeaderTextView = itemView.findViewById(R.id.listMessageItem_headerText);
            TextView latestMessageTextView = itemView.findViewById(R.id.listMessageItem_latestMessage);
            TextView timestampTextView = itemView.findViewById(R.id.listMessageItem_timestamp);
            ImageView emergencyIcon = itemView.findViewById(R.id.listMessageItem_alertIcon);

            List<String> parsedMessage = MessageHelper.parseMessageText(currentMessage.getText());
            String headerText = parsedMessage.get(0);
            String bodyText = parsedMessage.get(1);

            contactNameTextView.setText(MessageHelper.getContactNameFromDetailedContactList(MessageHelper.getMessageContactId(currentMessage), detailedContactList));
            messageHeaderTextView.setText(headerText);
            latestMessageTextView.setText(bodyText);
            timestampTextView.setText(format.format(currentMessage.getTimestamp()));

            if (currentMessage.isEmergency()) {
                emergencyIcon.setVisibility(View.VISIBLE);
            }

            if (!isRead) {
                contactNameTextView.setTypeface(contactNameTextView.getTypeface(), Typeface.BOLD);
                messageHeaderTextView.setTypeface(messageHeaderTextView.getTypeface(), Typeface.BOLD);
                latestMessageTextView.setTypeface(latestMessageTextView.getTypeface(), Typeface.BOLD);
            }

            return itemView;
        }
    }

    private void registerItemOnClick() {
        ListView messageList = findViewById(R.id.messagesActivity_messagesList);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Message> clickedMessageList = myGroupedMessagesList.get(position);
                User clickedContact = detailedContactList.get(position);

                // Mark all unread messages as read
                markMessagesRead(clickedMessageList);

                // Open chat activity in read-only mode, displaying the messages
                Model.setMessageList(clickedMessageList);
                Log.d(TAG, "clicked message list:\n" + clickedMessageList);
                Intent intent = ChatActivity.makeIntent(MessagesActivity.this, clickedContact.getName(), true, false, false, null);
                startActivity(intent);
            }
        });
    }

    private void markMessagesRead(List<Message> messageList) {
        for (int i = 0; i < messageList.size(); i++) {
            Call<Message> caller = model.getProxy().markMessageAsRead(messageList.get(i).getId(), true);
            ProxyBuilder.callProxy(MessagesActivity.this, caller, null);
        }
    }

    private void showLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.messagesActivity_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.messagesActivity_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
        }
    }
}
