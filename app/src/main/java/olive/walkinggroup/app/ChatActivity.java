package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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

/**
 *
 *
 *
 * Note: Emergency messages must have toGroup set to false.
 */

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
    private Boolean emergency;
    private Boolean toGroup;
    private Group group;

    public static Intent makeIntent(Context context, String headerText, Boolean readOnly, Boolean emergency, Boolean toGroup, Group group) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("headerText", headerText);
        intent.putExtra("readOnly", readOnly);
        intent.putExtra("emergency", emergency);
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

        // Hide Input UI on readOnly mode. (Can make more elegant?)
        if (readOnly) {
            ConstraintLayout inputContainer = findViewById(R.id.chatActivity_inputContainer);
            inputContainer.setVisibility(View.GONE);
        }

        initializeHeaderText();
        setupSendButton();
        setupModeSwitch();
    }


    private void getDataFromIntent() {
        Intent intent = getIntent();

        headerText = intent.getStringExtra("headerText");
        readOnly = intent.getBooleanExtra("readOnly", true);
        emergency = intent.getBooleanExtra("emergency", false);
        toGroup = intent.getBooleanExtra("toGroup", false);
        group = (Group) intent.getSerializableExtra("group");
    }

    private void getChatLog() {
        List<Message> messageList = model.getMessageList();

        if (messageList == null) {
            return;
        }

        // Reverse list to make latest message on bottom
        Collections.reverse(messageList);

        for (int i = 0; i < messageList.size(); i++) {
            addMessage(messageList.get(i));
        }
    }

    private void initializeHeaderText() {
        TextView textView = findViewById(R.id.chatActivity_headerText);
        textView.setText(headerText);

        EditText input = findViewById(R.id.chatActivity_input);
        if (emergency) {
            String helpText = "HELP!";
            input.setText(helpText);
            input.setSelection(helpText.length());
        }
    }

    private void setupSendButton() {
        RelativeLayout btn = findViewById(R.id.chatActivity_sendBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.chatActivity_input);
                String inputText = input.getText().toString();

                if (inputText.replaceAll("\\s", "").equals("") && !emergency) {
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
                message.setEmergency(emergency);
                // Add to local list
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

        // TODO: move string to string.xml
        if (emergency) {
            messageText = MessageHelper.constructMessageText("Emergency!", bodyText);
            return messageText;
        }

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
            // Include case for emergency.
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
                textView.setTextColor(Color.BLACK);

                if (currentMessage.isEmergency()) {
                    textContainer.setBackground(getDrawable(R.drawable.background_chat_send_emergency));
                } else {
                    textContainer.setBackground(getDrawable(R.drawable.background_chat_send));
                }

            } else {
                textContainer.setLayoutParams(receiveParams);
                textView.setTextColor(Color.WHITE);

                if (currentMessage.isEmergency()) {
                    textContainer.setBackground(getDrawable(R.drawable.background_chat_receive_emergency));
                } else {
                    textContainer.setBackground(getDrawable(R.drawable.background_chat_receive));
                }
            }

            return itemView;
        }
    }

    private boolean isSender(Message message) {
        return (Objects.equals(message.getFromUser().getId(), currentUser.getId()));
    }


    private void setupModeSwitch() {
        if (toGroup) {
            ConstraintLayout container = findViewById(R.id.chatActivity_modeSelectContainer);
            container.setVisibility(View.VISIBLE);
        } else {
            return;
        }

        Switch btn = findViewById(R.id.chatActivity_modeSwitch);
        btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    emergency = true;
                } else {
                    emergency = false;
                }
            }
        });

    }
}
