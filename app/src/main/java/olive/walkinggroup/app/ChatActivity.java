package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.MessageHelper;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class ChatActivity extends AppCompatActivity {
    private static final int MARGIN = 150;
    private DateFormat dateFormat;
    List<Message> chatLog = new ArrayList<>();
    Model model;
    User currentUser;
    ChatLogAdapter adapter;

    RelativeLayout.LayoutParams sendParams;
    RelativeLayout.LayoutParams receiveParams;

    private String headerText;
    private Boolean readOnly;
    private Boolean toGroup;
    private Group group;

    public static Intent makeIntent(Context context, String headerText, Boolean readOnly, Boolean toGroup, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("headerText", headerText);
        intent.putExtra("readOnly", readOnly);
        intent.putExtra("toGroup", toGroup);
        intent.putExtra("group", group);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        model = Model.getInstance();
        currentUser = model.getCurrentUser();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sendParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        sendParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        sendParams.setMarginStart(MARGIN);
        receiveParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        receiveParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        receiveParams.setMarginEnd(MARGIN);

        adapter = new ChatLogAdapter();
        ListView list = findViewById(R.id.chatActivity_chatLog);
        list.setAdapter(adapter);

        getDataFromIntent();
        getChatLog();
        initializeHeaderText();
        setupSendButton();
    }


    private void getDataFromIntent() {
        Intent intent = getIntent();

        headerText = intent.getStringExtra("headerText");
        readOnly = intent.getBooleanExtra("readOnly", true);
        toGroup = intent.getBooleanExtra("toGroup", false);
        group = (Group) intent.getSerializableExtra("group");
    }

    private void getChatLog() {
        List<Message> messageList = model.getMessageList();
        // Reverse list to make latest message on bottom
        Collections.reverse(messageList);

        for (int i = 0; i < messageList.size(); i++) {
            addMessage(messageList.get(i));
        }
    }

    private void initializeHeaderText() {
        TextView textView = findViewById(R.id.chatActivity_headerText);
        textView.setText(headerText);
    }

    private void setupSendButton() {
        RelativeLayout btn = findViewById(R.id.chatActivity_sendBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.chatActivity_input);
                String inputText = input.getText().toString();

                if (inputText.replaceAll("\\s", "").equals("")) {
                    return;
                }

                String messageText = makeMessageText();
                input.setText("");

                Message message = new Message();
                User dummy = new User();
                dummy.setId(currentUser.getId());
                message.setFromUser(dummy);
                message.setText(messageText);
                message.setTimestamp(Calendar.getInstance().getTime());

                // TODO: let user choose if emergency or not
                message.setEmergency(false);

                addMessage(message);
                // Send message to server
                sendMessage(message);
            }
        });
    }

    private String makeMessageText() {
        EditText input = findViewById(R.id.chatActivity_input);
        String bodyText = input.getText().toString();
        String messageText = "";

        // TODO: move to string.xml
        if (toGroup) {
            messageText = MessageHelper.constructMessageText("To: " + group.getGroupDescription(), bodyText);
        } else {
            messageText = MessageHelper.constructMessageText("To: My Parents & Leaders", bodyText);
        }

        return messageText;
    }

    private void sendMessage(Message message) {
        if (toGroup) {
            Call<List<Message>> caller = model.getProxy().newMessageToGroup(group.getId(), message);
            ProxyBuilder.callProxy(ChatActivity.this, caller, null);
        } else {
            Call<List<Message>> caller = model.getProxy().newMessageToParentsOf(currentUser.getId(), message);
            ProxyBuilder.callProxy(ChatActivity.this, caller, null);
        }
    }

    private void addMessage(Message newMessage) {
        adapter.add(newMessage);
    }


    private class ChatLogAdapter extends ArrayAdapter<Message> {
        public ChatLogAdapter() {
            super(ChatActivity.this, R.layout.list_chat_item, chatLog);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;

            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_chat_item, parent, false);
            }

            Message currentMessage = chatLog.get(position);
            String messageText = currentMessage.getText();
            boolean isSender = isSender(currentMessage);

            RelativeLayout textContainer = itemView.findViewById(R.id.chatItem_textContainer);
            TextView textView = itemView.findViewById(R.id.chatItem_text);
            textView.setText(MessageHelper.parseMessageText(messageText).get(1));

            if (isSender) {
                textContainer.setLayoutParams(sendParams);
                textContainer.setBackground(getDrawable(R.drawable.background_chat_send));
                textView.setTextColor(Color.BLACK);
            } else {
                textContainer.setLayoutParams(receiveParams);
                textContainer.setBackground(getDrawable(R.drawable.background_chat_receive));
                textView.setTextColor(Color.WHITE);
            }

            return itemView;
        }
    }

    private boolean isSender(Message message) {
        return (Objects.equals(message.getFromUser().getId(), currentUser.getId()));
    }

}
