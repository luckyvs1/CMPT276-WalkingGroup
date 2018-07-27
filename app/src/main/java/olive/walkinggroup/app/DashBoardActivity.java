package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.UpdateUserPoints;
import olive.walkinggroup.dataobjects.UploadGpsLocation;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class DashBoardActivity extends AppCompatActivity {
    private static final String TAG = "DashBoardActivity";
    private static final int REQUEST_CODE_VIEW_GROUPS_START_WALK = 0;
    private static final int GET_UNREAD_MESSAGES_INTERVAL = 60000;
    private static final int EARTH_RADIUS_METERS = 6371000;
    private Model instance;
    private UploadGpsLocation uploadGpsLocation;
    private UpdateUserPoints updateUserPoints;
    private Handler handler = new Handler();
    private Boolean hasCompletedCurrentWalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        uploadGpsLocation = new UploadGpsLocation(this);
        updateUserPoints = new UpdateUserPoints(this);

        instance = Model.getInstance();
        updateCurrentUser();
        checkIfUserIsParent();
        updateUserPoints();

        setupMessagesButton();
        handler.post(getUnreadMessagesRunnable);

        setupSimpleButtonActivityChange(R.id.toMonitor, MonitorActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toMap, FindGroupsActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toCreateGroup, CreateGroupActivity.class, false);
        setupSimpleButtonActivityChange(R.id.dashBoard_viewMyGroupsBtn, ListGroupsActivity.class, true);

        setupProfileButton();
        setUpStoreButton();
        setUpLeaderBoardButton();
        setupLogoutButton();

        setupTrackerButton();
        setupPanicButton();
        setupStopUploadButton();
    }

    private void setUpLeaderBoardButton() {
        RelativeLayout relativeLayout = findViewById(R.id.dashBoard_leaderboardBtn);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = LeaderboardActivity.makeLaunchIntent(DashBoardActivity.this);
                startActivity(intent);
            }
        });
    }

    private void checkIfUserIsParent() {
        LinearLayout trackUserBtn = findViewById(R.id.btnTracker);
        trackUserBtn.setVisibility(View.GONE);
        List<User> monitorList=  instance.getCurrentUser().getMonitorsUsers();
        int monitorListSize = monitorList.size();

        if(monitorListSize > 0){
            trackUserBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentUser();
        getUnreadMessages();
        checkIfUserIsParent();
        updateUserPoints();
    }

    private void updateCurrentUser() {
        if (instance.getCurrentUser() == null) {
            return;
        }
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

    private Runnable getUnreadMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Getting messages from server...");
            getUnreadMessages();
            handler.postDelayed(getUnreadMessagesRunnable, GET_UNREAD_MESSAGES_INTERVAL);
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

    private void setUpStoreButton() {
        RelativeLayout storeIcon = findViewById(R.id.dashBoard_StoreBtn);
        storeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, StoreActivity.class);
                startActivity(intent);
            }
        });
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

    private void setupProfileButton() {
        LinearLayout btn = findViewById(R.id.toUserProfile);
//        Button btn = findViewById(R.id.toUserProfile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserDetailsActivity.makeIntent(DashBoardActivity.this, instance.getCurrentUser());
                startActivity(intent);
            }
        });
    }

    private void setupSimpleButtonActivityChange(int buttonId, Class activityName, boolean forResult) {
        LinearLayout btn = findViewById(buttonId);
//        Button btn = findViewById(buttonId);
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
        LinearLayout logout = findViewById(R.id.btnLogout);
//        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nullValue = null;

                // Remove the token from the proxy
                instance.updateProxy(nullValue);

                // Remove the token and email from the shared preferences
                storeToSharedPreferences("Token", nullValue);
                storeToSharedPreferences("UserEmail", nullValue);
                storeToSharedPreferences("UserPassword", nullValue);

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

    // no manual stop upload
//    private void setupStopUploadButton() {
//        LinearLayout btn = findViewById(R.id.btnStopUpload);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!instance.activeGroupSelected()) {
//                    notifyUserViaLogAndToast("No active walking group selected");
//                } else {
//                    if (uploadGpsLocation.hasArrived()) {
//                        notifyUserViaLogAndToast("Stopping upload");
//                        uploadGpsLocation.stop();
//                    } else {
//                        notifyUserViaLogAndToast("You have not arrived at destination location");
//                    }
//                }
//            }
//
//        });
//    }

    private void setupStopUploadButton() {
        LinearLayout btn = findViewById(R.id.btnStopUpload);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!instance.activeGroupSelected()) {
                    notifyUserViaLogAndToast("No active walking group selected");
                } else {
                    notifyUserViaLogAndToast("Stopping upload");
                    uploadGpsLocation.stop();

                }
            }

        });
    }


    // Update User Points
    // ---------------------------------------------------------------------------------------------

    private void updateUserPoints() {

        if(instance.getCompletedWalkGroup() != null) {
            Log.d("UpdatePoints", "Walk was completed");
            double distanceWalked = getDistanceBetweenTwoPointsInM(instance.getCompletedWalkGroup().getStartPoint(), instance.getCompletedWalkGroup().getEndPoint());
            updateUserPointsEarned(distanceWalked);
            updateUser();
        }
    }

    private double getDistanceBetweenTwoPointsInM(LatLng firstPoint, LatLng secondPoint) {

        double destinationLatitude = secondPoint.latitude;
        double destinationLongitude = secondPoint.longitude;
        double meetingLatitude = firstPoint.latitude;
        double meetingLongitude = firstPoint.longitude;

        Log.d("UpdatePoints", Double.toString(destinationLatitude));
        Log.d("UpdatePoints", Double.toString(destinationLongitude));
        Log.d("UpdatePoints", Double.toString(meetingLatitude));
        Log.d("UpdatePoints", Double.toString(meetingLongitude));

        //Get the distance of the walk
        // Haversine pseudocode from:https://community.esri.com/groups/coordinate-reference-systems/blog/2017/10/05/haversine-formula
        Double phi_1 = Math.toRadians(destinationLatitude);
        Double phi_2 = Math.toRadians(meetingLatitude);

        Double delta_phi = Math.toRadians(destinationLatitude - meetingLatitude);
        Double delta_lambda = Math.toRadians(destinationLongitude - meetingLongitude);


        //a = sin²(φB - φA/2) + cos φA * cos φB * sin²(λB - λA/2)
        //c = 2 * atan2( √a, √(1−a) )
        //d = R ⋅ c -- distanceInMeters

        Double a = Math.pow(Math.sin(delta_phi / 2.0), 2) + Math.cos(phi_1) * Math.cos(phi_2) * Math.pow(Math.sin(delta_lambda / 2.0), 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        Double distanceInMeters = EARTH_RADIUS_METERS * c;

        Log.d("UpdatePoints", Double.toString(distanceInMeters));

        return distanceInMeters;
    }

    private void updateUserPointsEarned(Double distanceInMeters) {

        Log.d("UpdatePoints", Double.toString(distanceInMeters));

        Long distanceToPoints = (Math.round(distanceInMeters));

        Log.d("UpdatePoints", Long.toString(distanceToPoints));

        // Long to Integer from StackOverflow
        Integer accumulatedPoints = distanceToPoints != null ? distanceToPoints.intValue() : null;

        Log.d("UpdatePoints",Integer.toString(accumulatedPoints));

        Integer currentPoints = instance.getCurrentUser().getCurrentPoints() != null ? instance.getCurrentUser().getCurrentPoints() : 0;
        Integer totalPoints = instance.getCurrentUser().getTotalPointsEarned() != null ? instance.getCurrentUser().getTotalPointsEarned() : 0;

        currentPoints += accumulatedPoints;
        totalPoints += accumulatedPoints;

        Log.d("UpdatePoints", Integer.toString(currentPoints));
        Log.d("UpdatePoints", Integer.toString(totalPoints));

        instance.getCurrentUser().setTotalPointsEarned(totalPoints);
        instance.getCurrentUser().setCurrentPoints(currentPoints);

        Log.d("UpdatePoints", Integer.toString(instance.getCurrentUser().getCurrentPoints()));
        Log.d("UpdatePoints", Integer.toString(instance.getCurrentUser().getTotalPointsEarned()));
    }

    private void updateUser() {
        Log.d("UpdatePoints", "Called Update User");

        Integer currentPoints = instance.getCurrentUser().getCurrentPoints();
        Integer totalPoints = instance.getCurrentUser().getTotalPointsEarned();

        User dummyUser = new User();

        dummyUser.setCurrentPoints(currentPoints);
        dummyUser.setTotalPointsEarned(totalPoints);
        dummyUser.setEmail(instance.getCurrentUser().getEmail());
        dummyUser.setId(instance.getCurrentUser().getId());
        dummyUser.setName(instance.getCurrentUser().getName());

        Call<User> caller = instance.getProxy().updateUser(instance.getCurrentUser().getId(), dummyUser);
        Log.d("UpdatePoints", "Called Update User after caller");

        ProxyBuilder.callProxy(DashBoardActivity.this, caller, updatedUser -> updateUserResponse(updatedUser));
        Log.d("UpdatePoints", "Called Update User after proxy builder");

    }

    private void updateUserResponse(User updatedUserWithPoints) {

        Log.d("UpdatePoints", "The updated user is" + updatedUserWithPoints.toString());

        instance.setCurrentUser(updatedUserWithPoints);
        Log.d("UpdatePoints", Integer.toString(instance.getCurrentUser().getCurrentPoints()));
        Log.d("UpdatePoints", Integer.toString(instance.getCurrentUser().getTotalPointsEarned()));

        Log.d("UpdatePoints", "Set Group To Null");
        instance.setCompletedWalkGroup(null);

        updateCurrentUser();
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

