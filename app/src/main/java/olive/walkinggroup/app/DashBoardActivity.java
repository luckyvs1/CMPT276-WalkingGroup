package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.UploadGpsLocation;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class DashBoardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_VIEW_GROUPS_START_WALK = 0;
    private Model instance;
    private UploadGpsLocation uploadGpsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        uploadGpsLocation = new UploadGpsLocation(this);

        instance = Model.getInstance();

        setupSimpleButtonActivityChange(R.id.toMonitor, MonitorActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toMap, FindGroupsActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toCreateGroup, CreateGroupActivity.class, false);
        setupSimpleButtonActivityChange(R.id.dashBoard_viewMyGroupsBtn, ListGroupsActivity.class, true);
        setupSimpleButtonActivityChange(R.id.btnTracker, TrackerActivity.class, false);
        setupSettingsButton();
        setupMessagesButton();
        // Todo: check new messages, display "!" on R.id.dashBoard_messagesText

        setupLogoutButton();
        setupStopUploadButton();
        updateCurrentUser();
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentUser();
        setupMessagesButton();
    }

    private void updateCurrentUser() {
        Call<User> caller = instance.getProxy().getUserById(instance.getCurrentUser().getId());
        ProxyBuilder.callProxy(DashBoardActivity.this, caller, returnedUser -> getUserById(returnedUser));
    }

    private void getUserById(User userFromEmail) {
        instance.setCurrentUser(userFromEmail);
        displayUserName();
    }

    private void setupStopUploadButton() {
        Button btn = findViewById(R.id.btnStopUpload);
        btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (!instance.activeGroupSelected()) {
                        notifyUserViaLogAndToast("No active walking group selected");
                    } else {
                        if (uploadGpsLocation.hasArrived()) {
                            notifyUserViaLogAndToast("Stopping upload");
                            uploadGpsLocation.stop();
                        } else {
                            notifyUserViaLogAndToast("You have not arrived at destination location");
                        }
                    }
                }

        });

    }

    private void setupSettingsButton() {
        Button btn = (Button) findViewById(R.id.toUserProfile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserDetailsActivity.makeIntent(DashBoardActivity.this, instance.getCurrentUser());
                startActivity(intent);
            }
        });
    }

    private void displayUserName() {
        try {
            String message = "Welcome, " + instance.getCurrentUser().getName() + "!";
            TextView userName = (TextView) findViewById(R.id.txtUserName);
            userName.setText(message);
        } catch (NullPointerException e) {
            Log.d("DashboardActivity", e.getMessage());
        }
    }                   

    private void setupSimpleButtonActivityChange(int buttonId, Class activityName, boolean forResult) {
        Button btn = (Button) findViewById(buttonId);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, activityName);
                if (!forResult) {
                    startActivity(intent);
                } else {
                    startActivityForResult(intent, REQUEST_CODE_VIEW_GROUPS_START_WALK);
                }
            }
        });
    }

    private void setupMessagesButton() {
        RelativeLayout btn = findViewById(R.id.dashBoard_messagesBtn);
        getUnreadMessages();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, MessagesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getUnreadMessages() {
        Call<List<Message>> caller = instance.getProxy().getUnreadMessages(instance.getCurrentUser().getId(), false);
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetUnreadMessagesResponse(returnedList));
    }

    private void onGetUnreadMessagesResponse(List<Message> returnedList) {
        int numUnreadMessages = returnedList.size();
        // Exclude messages sent by self to self
        for (int i = 0; i < returnedList.size(); i++) {
            Message currentMessage = returnedList.get(i);
            if (Objects.equals(currentMessage.getFromUser().getId(), instance.getCurrentUser().getId())) {
                numUnreadMessages--;
            }
        }

        String displayText = "" + numUnreadMessages;
        TextView textView = findViewById(R.id.dashBoard_messagesText);
        textView.setText(displayText);
    }

    private void setupLogoutButton() {
        Button logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nullValue = null;

                // Remove the token from the proxy
                instance.updateProxy(nullValue);

                // Remove the token and email from the shared preferences
                storeToSharedPreferences("Token", nullValue);
                storeToSharedPreferences("UserEmail", nullValue);

                // Stop Gps location upload
                uploadGpsLocation.stop();

                // Clear active group from model
                instance.clearActiveGroup();

                // End the activity
                finish();
            }
        });
    }

    // Remove the login token and user email
    private void storeToSharedPreferences(String keyName, String value) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString(keyName,value);
        editor.commit();
    }

    //https://stackoverflow.com/questions/2354336/android-pressing-back-button-should-exit-the-app
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_VIEW_GROUPS_START_WALK:
                if (resultCode == Activity.RESULT_OK) {
                    Group activeGroup = instance.getActiveGroup();
                    notifyUserViaLogAndToast(getString(R.string.start_walk_group_name) + activeGroup.getGroupDescription());

                    uploadGpsLocation.start();
                }
        }
    }



    private void notifyUserViaLogAndToast(String message) {
        Toast.makeText(DashBoardActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.i("App", message);
    }
}



