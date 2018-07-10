package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.dataobjects.UserListHelper;

public class NewMessageActivity extends AppCompatActivity {
    private Model model;
    private User currentUser;
    private Spinner dropdown;
    private List<User> contactList;
    private List<String> contactLabelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        dropdown = findViewById(R.id.newMessage_toUserDropdown);
        model = Model.getInstance();
        currentUser = model.getCurrentUser();
        contactList = getTestList();
        buildContactLabelList();

        setupNewMessageBtn();
        setupToUserDropdown();
    }

    private List<User> getTestList() {
        List<User> returnList = new ArrayList<>();
        User user1 = new User();
        user1.setId((long) 9990);
        user1.setName("Test Subject1");
        user1.setEmail("sub1@test.com");
        returnList.add(user1);

        User user2 = new User();
        user2.setId((long) 9991);
        user2.setName("Test Subject2");
        user2.setEmail("sub2@test.com");
        returnList.add(user2);

        return returnList;
    }

    private void buildContactLabelList() {
        contactLabelList = new ArrayList<>();

        for (int i = 0; i < contactList.size(); i++) {
            User currUser = contactList.get(i);
            contactLabelList.add(currUser.getName());
        }
    }

    private void setupNewMessageBtn() {
        RelativeLayout btn = findViewById(R.id.newMessage_newMessageBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User selectedUser = contactList.get(dropdown.getSelectedItemPosition());

                Intent intent = new Intent(NewMessageActivity.this, ChatActivity.class);
                intent.putExtra("toUser", selectedUser);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupToUserDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, contactLabelList);
        dropdown.setAdapter(adapter);
    }
}
