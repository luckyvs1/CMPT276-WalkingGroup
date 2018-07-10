package olive.walkinggroup.app;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class ChatActivity extends AppCompatActivity {
    private User toUser;
    private static final int MARGIN = 150;
    private DateFormat dateFormat;
    List<Message> chatLog = new ArrayList<>();
    Model model;
    User currentUser;
    ChatLogAdapter adapter;

    RelativeLayout.LayoutParams sendParams;
    RelativeLayout.LayoutParams receiveParams;

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
        initializeHeaderText();
        setupSendButton();
        setupTestButton();
    }


    public void getDataFromIntent() {
        toUser = (User) getIntent().getSerializableExtra("toUser");
    }

    private void initializeHeaderText() {
        TextView textView = findViewById(R.id.chatActivity_headerText);
        textView.setText(toUser.getName());
    }

    private void setupSendButton() {
        RelativeLayout btn = findViewById(R.id.chatActivity_sendBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.chatActivity_input);
                String messageText = input.getText().toString();

                if (messageText.replaceAll("\\s", "").equals("")) {
                    return;
                }
                input.setText("");

                Message message = new Message();
                User dummy = new User();
                dummy.setId(currentUser.getId());
                message.setFromUser(dummy);
                message.setText(messageText);
                message.setTimestamp(Calendar.getInstance().getTime());

                addMessage(message);
            }
        });
    }

    private void setupTestButton() {
        Button btn = findViewById(R.id.chatActivity_testBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = "The time now is: " + dateFormat.format(Calendar.getInstance().getTime());
                Message message = new Message();
                User dummy = new User();
                dummy.setId(currentUser.getId());
                User dummy2 = new User();
                dummy2.setId((long) 202011);
                message.setFromUser(dummy2);
                message.setToUser(dummy);
                message.setText(messageText);
                message.setTimestamp(Calendar.getInstance().getTime());
                addMessage(message);
            }
        });
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
            textView.setText(messageText);

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
