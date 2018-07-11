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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    private List<Message> allMessagesList = new ArrayList<>();

    private List<List<Message>> myMessagesList = new ArrayList<>();
    private List<Message> displayList = new ArrayList<>();
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

        getAllMessages();
        setupNewMessagesBtn();
    }

    // Get all messages from server
    private void getAllMessages() {
        Call<List<Message>> caller = model.getProxy().getMessages();
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetAllMessagesResponse(returnedList));
    }

    private void onGetAllMessagesResponse(List<Message> returnedList) {
        //allMessagesList = returnedList;
        allMessagesList = MessageHelper.makeTestList();
        buildMyMessagesList();
    }

    // Get only messages related to user, put in myMessagesList, grouped by contact
    private void buildMyMessagesList() {
        for (int i = 0; i < allMessagesList.size(); i++) {
            Message currentMessage = allMessagesList.get(i);

            // Add to list if message is related to currentUser
            if (MessageHelper.getMessageContactId(currentMessage) > 0) {
                addToMyMessagesList(currentMessage);
            }
        }

        // Sort list chronologically, latest on top
        myMessagesList = MessageHelper.sortMessageListOfList(myMessagesList);
        buildDisplayList();
        populateList();
    }

    // Put message to list corresponding to contact
    private void addToMyMessagesList(Message message) {
        long messageContactId = MessageHelper.getMessageContactId(message);

        // Search for List<Messages> associated with contact in myMessagesList
        for (int i = 0; i < myMessagesList.size(); i++) {
            List<Message> currentList = myMessagesList.get(i);
            Message currentListHead = currentList.get(0);

            if (MessageHelper.getMessageContactId(currentListHead) == messageContactId) {
                currentList.add(message);
                return;
            }
        }

        // Contact not yet exist. Create new list with contact Messages
        List<Message> newList = new ArrayList<>();
        newList.add(message);
        myMessagesList.add(newList);
    }

    // Get latest Message from each contact, from index 0 of sorted Message Lists.
    private void buildDisplayList() {
        for (int i = 0; i < myMessagesList.size(); i++) {
            displayList.add(myMessagesList.get(i).get(0));
        }
    }

    private void populateList() {
        ListView listView = findViewById(R.id.messagesActivity_messagesList);
        MyMessagesListAdapter adapter = new MyMessagesListAdapter();
        listView.setAdapter(adapter);
    }

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
            TextView latestMessageTextView = itemView.findViewById(R.id.listMessageItem_latestMessage);
            TextView timestampTextView = itemView.findViewById(R.id.listMessageItem_timestamp);

            String messageText = "";

            if (Objects.equals(currentMessage.getToUser().getId(), currentUser.getId())) {
                // If currentUser is receiver
                contactNameTextView.setText(currentMessage.getFromUser().getName());
            } else if (Objects.equals(currentMessage.getFromUser().getId(), currentUser.getId())) {
                // If currentUser is sender
                contactNameTextView.setText(currentMessage.getToUser().getName());
                messageText += getResources().getString(R.string.messagesActivity_youTag);
            }
            messageText += currentMessage.getText();
            latestMessageTextView.setText(messageText);
            timestampTextView.setText(format.format(currentMessage.getTimestamp()));

            return itemView;
        }
    }

    private void registerItemOnClick() {
        ListView messageList = findViewById(R.id.messagesActivity_messagesList);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<Message> clickedMessageList = myMessagesList.get(position);
                User clickedContact = detailedContactList.get(position);

                Intent intent = ChatActivity.makeIntent(MessagesActivity.this, clickedMessageList, clickedContact);
            }
        });
    }
}
