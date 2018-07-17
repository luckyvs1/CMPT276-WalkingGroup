package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final int GET_UNREAD_MESSAGES_INTERVAL = 60000;
    private Model instance;
    private UploadGpsLocation uploadGpsLocation;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        uploadGpsLocation = new UploadGpsLocation(this);
        instance = Model.getInstance();
        updateCurrentUser();

        setupMessagesButton();
        handler.post(GetUnreadMessagesRunnable);

        setupSimpleButtonActivityChange(R.id.toMonitor, MonitorActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toMap, FindGroupsActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toCreateGroup, CreateGroupActivity.class, false);
        setupSimpleButtonActivityChange(R.id.dashBoard_viewMyGroupsBtn, ListGroupsActivity.class, true);
        setupSettingsButton();
        setupLogoutButton();

        setupTrackerButton();
        setupPanicButton();
        setupStopUploadButton();
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentUser();
        getUnreadMessages();
    }

    private void updateCurrentUser() {
        Call<User> caller = instance.getProxy().getUserById(instance.getCurrentUser().getId());
        ProxyBuilder.callProxy(DashBoardActivity.this, caller, returnedUser -> getUserById(returnedUser));
    }

    private void getUserById(User updatedUser) {
        instance.setCurrentUser(updatedUser);
        displayUserName();
    }

    // UI Logic
    // ---------------------------------------------------------------------------------------------

    // Top Bar
    // -------------------------------
    private void setupMessagesButton() {
        RelativeLayout btn = findViewById(R.id.dashBoard_messagesBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, MessagesActivity.class);
                startActivity(intent);
            }
        });
    }

    private Runnable GetUnreadMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            getUnreadMessages();
            handler.postDelayed(GetUnreadMessagesRunnable, GET_UNREAD_MESSAGES_INTERVAL);
        }
    };

    private void getUnreadMessages() {
        Call<List<Message>> caller = instance.getProxy().getUnreadMessages(instance.getCurrentUser().getId(), false);
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetUnreadMessagesResponse(returnedList));
    }

    private void onGetUnreadMessagesResponse(List<Message> returnedList) {
        int numUnreadMessages = returnedList.size();
        // Exclude messages sent from self to self
        for (int i = 0; i < returnedList.size(); i++) {
            Message currentMessage = returnedList.get(i);

            if (Objects.equals(currentMessage.getFromUser().getId(), instance.getCurrentUser().getId())) {
                numUnreadMessages--;
            }
        }
        String displayText = "";

        if (numUnreadMessages > 0) {
            displayText += numUnreadMessages;
        }
        TextView textView = findViewById(R.id.dashBoard_messagesText);
        textView.setText(displayText);

        getEmergencyMessages();
    }

    private void getEmergencyMessages() {
        Call<List<Message>> caller = instance.getProxy().getUnreadMessages(instance.getCurrentUser().getId(), true);
        ProxyBuilder.callProxy(this, caller, returnedList -> onGetEmergencyMessagesResponse(returnedList));
    }

    private void onGetEmergencyMessagesResponse(List<Message> returnedList) {
        ImageView alertIcon = findViewById(R.id.dashBoard_emergencyIcon);
        ImageView messageIcon = findViewById(R.id.dashBoard_messagesIcon);

        if (returnedList.size() == 0) {
            alertIcon.setVisibility(View.INVISIBLE);
            messageIcon.setVisibility(View.VISIBLE);
            return;
        }
        alertIcon.setVisibility(View.VISIBLE);
        messageIcon.setVisibility(View.INVISIBLE);
    }

    // Center Menu
    // -------------------------------
    private void displayUserName() {
        try {
            String message = "Welcome, " + instance.getCurrentUser().getName() + "!";
            TextView userName = (TextView) findViewById(R.id.txtUserName);
            userName.setText(message);
        } catch (NullPointerException e) {
            Log.d("DashboardActivity", e.getMessage());
        }
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

    // Bottom Bar
    // -------------------------------
    private void setupTrackerButton() {
        LinearLayout btn = findViewById(R.id.btnTracker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, TrackerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupPanicButton() {
        LinearLayout btn = findViewById(R.id.dashBoard_panicBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model.setMessageList(new ArrayList<>());
                Intent intent = ChatActivity.makeIntent(DashBoardActivity.this, "Emergency Message", false, true, false, null);
                startActivity(intent);
            }
        });
    }

    private void setupStopUploadButton() {
        LinearLayout btn = findViewById(R.id.btnStopUpload);
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

    // Other logic
    // ---------------------------------------------------------------------------------------------

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



