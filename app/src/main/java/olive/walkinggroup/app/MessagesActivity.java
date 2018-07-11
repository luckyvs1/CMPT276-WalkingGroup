package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import olive.walkinggroup.R;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        setupNewMessagesBtn();
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
}
