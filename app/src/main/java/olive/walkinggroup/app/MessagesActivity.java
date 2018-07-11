package olive.walkinggroup.app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class MessagesActivity extends AppCompatActivity {

    private List<Message> allMessagesList = new ArrayList<>();
    private List<Message> displayList = new ArrayList<>();
    private List<User> detailedContactList = new ArrayList<>();

    // List of List of Messages, grouped by contact
    private List<List<Message>> myMessagesList = new ArrayList<>();

    private Model model;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();

        getAllMessages();

        setupNewMessagesBtn();
    }

    // Get all messages from server
    private void getAllMessages() {
        Call<List<Message>> caller = model.getProxy().getMessages();
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetAllMessagesResponse(returnedList));
    }

    private void onGetAllMessagesResponse(List<Message> returnedList) {
        allMessagesList = returnedList;
        buildMyMessagesList();
    }

    // Get only messages related to user, put in myMessagesList, grouped by contact
    private void buildMyMessagesList() {
        for (int i = 0; i < allMessagesList.size(); i++) {
            Message currentMessage = allMessagesList.get(i);

            // Add to list if message is related to currentUser
            if (getMessageContactId(currentMessage) > 0) {
                addToMyMessagesList(currentMessage);
            }
        }

        // Sort list chronologically, latest on top
        myMessagesList = sortMessageListOfList(myMessagesList);
        buildDisplayList();
        populateList();
    }

    // Put message to list corresponding to contact
    private void addToMyMessagesList(Message message) {
        long messageContactId = getMessageContactId(message);

        // Search for List<Messages> associated with contact in myMessagesList
        for (int i = 0; i < myMessagesList.size(); i++) {
            List<Message> currentList = myMessagesList.get(i);
            Message currentListHead = currentList.get(0);

            if (getMessageContactId(currentListHead) == messageContactId) {
                currentList.add(message);
                return;
            }
        }

        // Contact not yet exist. Create new list with contact Messages
        List<Message> newList = new ArrayList<>();
        newList.add(message);
        myMessagesList.add(newList);
    }

    // Get the id of the contact associated with message.
    // Contact can be either sender or receiver, but cannot be currentUser.
    private long getMessageContactId(Message message) {
        if (Objects.equals(message.getToUser().getId(), currentUser.getId())) {
            // If currentUser is receiver
            return message.getFromUser().getId();
        } else if (Objects.equals(message.getFromUser().getId(), currentUser.getId())){
            // If currentUser is sender
            return message.getToUser().getId();
        } else {
            // Message is not related to currentUser
            return -1;
        }
    }

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

    // Sort message list in chronological order
    private List<Message> sortMessageList(List<Message> listToSort) {
        Collections.sort(listToSort, new MessageComparator());
        return listToSort;
    }

    private static class MessageComparator implements Comparator<Message> {
        @Override
        public int compare(Message o1, Message o2) {
            Date time1 = o1.getTimestamp();
            Date time2 = o2.getTimestamp();

            if (time1 != null && time2 != null) {
                return time2.compareTo(time1);
            }

            return 0;
        }
    }

    // Sort list of list of message in chronological order of latest message
    private List<List<Message>> sortMessageListOfList(List<List<Message>> listToSort) {
        for (int i = 0; i < listToSort.size(); i++) {
            List<Message> currentList = sortMessageList(listToSort.get(i));
            listToSort.set(i, currentList);
        }

        Collections.sort(listToSort, new MessageListComparator());
        return listToSort;
    }

    private static class MessageListComparator implements Comparator<List<Message>> {
        @Override
        public int compare(List<Message> o1, List<Message> o2) {
            Date time1 = o1.get(0).getTimestamp();
            Date time2 = o2.get(0).getTimestamp();

            if (time1 != null && time2 != null) {
                return time2.compareTo(time1);
            }

            return 0;
        }
    }


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

            TextView contactNameTextView = itemView.findViewById(R.id.listMessageItem_userName);
            TextView latestMessageTextView = itemView.findViewById(R.id.listMessageItem_latestMessage);
            TextView timestampTextView = itemView.findViewById(R.id.listMessageItem_timestamp);



            return itemView;
        }
    }
}
