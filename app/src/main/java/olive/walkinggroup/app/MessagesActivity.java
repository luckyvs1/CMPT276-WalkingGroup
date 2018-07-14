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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.MessageHelper;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class MessagesActivity extends AppCompatActivity {
    //private List<Message> allMessagesList = new ArrayList<>();
    private List<Message> allMyRegularList = new ArrayList<>();
    private List<Message> allEmergencyMessageList = new ArrayList<>();

    private List<List<Message>> myGroupedEmergencyList = new ArrayList<>();
    private List<List<Message>> myGroupedRegularList = new ArrayList<>();
    private List<List<Message>> myGroupedMessagesList = new ArrayList<>();

    // TODO: detailedContactList may not be aligned if server packet transfer has delay. Align if necessary.
    private List<Message> displayList = new ArrayList<>();
    private List<Integer> contactIdList = new ArrayList<>();
    private List<User> detailedContactList = new ArrayList<>();

    private Model model;
    private User currentUser;
    SimpleDateFormat format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();
        format = new SimpleDateFormat("MMM dd (EEE) hh:mmaaa", Locale.getDefault());

        getEmergencyMessages();
        setupNewMessagesBtn();
    }

    // Setup UI elements:
    // ---------------------------------------------------------------------------------------------

    private void setupNewMessagesBtn() {
        RelativeLayout btn = findViewById(R.id.messagesActivity_newMessageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessagesActivity.this, NewMessageActivity.class);
                startActivity(intent);
            }
        });
    }

    // Server calls:
    // ---------------------------------------------------------------------------------------------
    // Get emergency messages sent to currentUser, read and unread
    private void getEmergencyMessages() {
        Call<List<Message>> caller = model.getProxy().getMessages(currentUser.getId(), true);
        ProxyBuilder.callProxy(this, caller, emergencyList -> onGetEmergencyMessagesResponse(emergencyList));
    }

    private void onGetEmergencyMessagesResponse(List<Message> emergencyList) {
        myGroupedEmergencyList = MessageHelper.groupByContact(emergencyList);
        MessageHelper.sortMessageListOfList(myGroupedEmergencyList);

        getToCurrentUserMessages();
    }

    // Get regular messages sent to currentUser, read and unread
    private void getToCurrentUserMessages() {
        Call<List<Message>> caller = model.getProxy().getMessages(currentUser.getId(), false);
        ProxyBuilder.callProxy(this, caller, messagesList -> onGetToCurrentUserMessagesResponse(messagesList));
    }

    private void onGetToCurrentUserMessagesResponse(List<Message> messagesList) {
        myGroupedRegularList = MessageHelper.groupByContact(messagesList);
        MessageHelper.sortMessageListOfList(myGroupedRegularList);

        myGroupedMessagesList.addAll(myGroupedEmergencyList);
        myGroupedMessagesList.addAll(myGroupedRegularList);

        buildContactIdList();
    }

    // Get id of each contact
    private void buildContactIdList() {
        for (int i = 0; i < myGroupedMessagesList.size(); i++) {
            Message currentListHead = myGroupedMessagesList.get(i).get(0);
            long id = MessageHelper.getMessageContactId(currentListHead);

            contactIdList.add((int) id);
        }
        getDetailedContactList();
    }

    // Get detailed info of each contact (User)
    private void getDetailedContactList() {
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
        for (int i = 0; i < myGroupedRegularList.size(); i++) {
            displayList.add(myGroupedRegularList.get(i).get(0));
        }
    }


    // Populate Message ListView:
    // ---------------------------------------------------------------------------------------------
    private void populateList() {
        ListView listView = findViewById(R.id.messagesActivity_messagesList);
        MyMessagesListAdapter adapter = new MyMessagesListAdapter();
        listView.setAdapter(adapter);
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

            TextView contactNameTextView = itemView.findViewById(R.id.listMessageItem_userName);
            TextView messageHeaderTextView = itemView.findViewById(R.id.listMessageItem_headerText);
            TextView latestMessageTextView = itemView.findViewById(R.id.listMessageItem_latestMessage);
            TextView timestampTextView = itemView.findViewById(R.id.listMessageItem_timestamp);

            List<String> parsedMessage = MessageHelper.parseMessageText(currentMessage.getText());
            String headerText = parsedMessage.get(0);
            String bodyText = parsedMessage.get(1);

            contactNameTextView.setText(detailedContactList.get(position).getName());
            messageHeaderTextView.setText(headerText);
            latestMessageTextView.setText(bodyText);
            timestampTextView.setText(format.format(currentMessage.getTimestamp()));

            return itemView;
        }
    }

    private void registerItemOnClick() {
        ListView messageList = findViewById(R.id.messagesActivity_messagesList);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Message> clickedMessageList = myGroupedRegularList.get(position);
                User clickedContact = detailedContactList.get(position);

                Intent intent = ChatActivity.makeIntent(MessagesActivity.this, clickedMessageList, clickedContact);
            }
        });
    }
}
