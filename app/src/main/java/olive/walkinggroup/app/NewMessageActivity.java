package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;

import olive.walkinggroup.R;

public class NewMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        setupNewMessageBtn();
        setupToUserDropdown();
    }

    private void setupNewMessageBtn() {
        RelativeLayout btn = findViewById(R.id.newMessage_newMessageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMessageActivity.this, ChatActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupToUserDropdown() {
        Spinner dropdown = findViewById(R.id.newMessage_toUserDropdown);
        dropdown.setSelection(-1, false);

        String[] testList = new String[]{"User 1", "User 2", "User 3"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testList);
        dropdown.setAdapter(adapter);
    }
}
