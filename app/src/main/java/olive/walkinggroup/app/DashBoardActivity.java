package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Message;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.PermissionRequest;
import olive.walkinggroup.dataobjects.PointsHelper;
import olive.walkinggroup.dataobjects.Rewards;
import olive.walkinggroup.dataobjects.UploadGpsLocation;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class DashBoardActivity extends AppCompatActivity {
    private static final String TAG = "DashBoardActivity";
    private static final int REQUEST_CODE_VIEW_GROUPS_START_WALK = 0;
    private static final int GET_UNREAD_MESSAGES_INTERVAL = 60000;
    private Model instance;
    private UploadGpsLocation uploadGpsLocation;
    private Handler handler = new Handler();
    private PointsHelper pointsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        uploadGpsLocation = new UploadGpsLocation(this);

        instance = Model.getInstance();
        updateCurrentUser();
        checkIfUserIsParent();

        // Top
        setupMessagesButton();
        handler.post(getUnreadMessagesRunnable);

        // Center
        setupSimpleButtonActivityChange(R.id.toMonitor, MonitorActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toMap, FindGroupsActivity.class, false);
        setupSimpleButtonActivityChange(R.id.toCreateGroup, CreateGroupActivity.class, false);
        setupSimpleButtonActivityChange(R.id.dashBoard_viewMyGroupsBtn, ListGroupsActivity.class, true);

        setupProfileButton();
        setUpStoreButton();
        setUpLeaderBoardButton();
        setupLogoutButton();

        // Bottom
        setupTrackerButton();
        setupPanicButton();
        setupStopUploadButton();
        setupSimpleButtonActivityChange(R.id.dashBoard_permissionsBtn, ViewPermissionsActivity.class, false);
        notifyNewPermissionRequest();
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
        notifyNewPermissionRequest();
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
        instance.getCurrentUser().setRewards(updatedUser.getRewards());
        setupProfileSection();
    }

    private void setupProfileSection() {

        pointsHelper = new PointsHelper();
        Rewards rewards = new Rewards(this);

        Integer totalPoints;
        int invalidTier = -1;
        int currentTier;
        Integer avatarId;
        int titleColor;
        String titleName;
        int colorValue;

        TypedArray typedArrayColors = getResources().obtainTypedArray(R.array.colors);
        TypedArray defaultColor = getResources().obtainTypedArray(R.array.color_default);
        String[] typedArrayTitles = getResources().getStringArray(R.array.title_default);
        TypedArray defaultAvatar = getResources().obtainTypedArray(R.array.avatar_default);

        currentTier = pointsHelper.getCurrentTier();

        if (instance.getCurrentUser().getTotalPointsEarned() != null) {
            totalPoints = instance.getCurrentUser().getTotalPointsEarned();
        }  else {
            totalPoints = 0;
        }

        int[] tierMaxPoints = pointsHelper.getTierPoints();

        int currentTierMaxPoints = 0;

        if (currentTier >= 0) {
            if((currentTier + 1) < Rewards.NUM_TIERS) {
                currentTierMaxPoints = tierMaxPoints[currentTier + 1] - tierMaxPoints[currentTier];
            } else {
                currentTierMaxPoints = tierMaxPoints[currentTier];
            }
        } else {
            currentTierMaxPoints = tierMaxPoints[currentTier + 1];
        }

        int pointsNeeded = pointsHelper.getPointsNeeded();

        int pointsProgress = currentTierMaxPoints - pointsNeeded;

        if (pointsProgress < 0) {
            pointsProgress = 0;
        }

        String welcomeMessage = "Welcome, " + instance.getCurrentUser().getName() + "!";
        String pointsMessage = pointsProgress + " / " + currentTierMaxPoints;

        if (totalPoints > tierMaxPoints[Rewards.NUM_TIERS -1]) {
            pointsMessage = "Max tier reached!";
        }

        TextView totalPointsText = findViewById(R.id.dashBoard_totalPointsTxt);
        String totalPointsMessage = "Total points: " + totalPoints;
        totalPointsText.setText(totalPointsMessage);

        if(currentTier != invalidTier){

            // Colors
            titleColor = typedArrayColors.getResourceId(currentTier, invalidTier);
            colorValue = getResources().getColor(titleColor);

            // Titles
            titleName = rewards.getTierTitle(currentTier);


            if(instance.getCurrentUser().getRewards() != null) {
                avatarId = instance.getCurrentUser().getRewards().getSelectedIconId();
            } else {
                avatarId = defaultAvatar.getResourceId(0,0);
            }

        } else {
            titleColor = defaultColor.getResourceId(currentTier+1, 0);
            colorValue = getResources().getColor(titleColor);

            // Default Title
            titleName = typedArrayTitles[currentTier+1];
            avatarId = defaultAvatar.getResourceId(0,0);
        }

        displayDetails(R.id.txtUserName, welcomeMessage);
        displayDetails(R.id.txtUserPoints, pointsMessage);
        setTitleColor(R.id.txtUserTitle, colorValue);
        displayDetails(R.id.txtUserTitle, titleName);
        displayAvater(R.id.imgViewAvatar, avatarId);

        // Update progress bar
        updateProgressBar(currentTier + 1, currentTierMaxPoints - pointsNeeded, currentTierMaxPoints);
    }

    private void updateProgressBar(int currentTier, int currentProgress, int maxProgress) {
        ProgressBar progressBar = findViewById(R.id.dashBoard_progressBar);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(currentProgress);

        TextView currentTierTextView = findViewById(R.id.dashBoard_currentTier);
        String currentTierText = currentTier + "";
        currentTierTextView.setText(currentTierText);
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
            getUnreadMessages();
            Log.d(TAG, "Getting messages from server...");

            // Update permission request status
            notifyNewPermissionRequest();
            Log.d(TAG, "Getting pending permission requests from server...");

            //Update user when fetching messages
            updateCurrentUser();
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
    private void displayDetails(int resourceID, String message) {
        TextView userDetail = (TextView) findViewById(resourceID);
        userDetail.setText(message);
    }

    private void displayAvater(Integer avatarId, Integer avatarResource) {
        Drawable avatarImg = getResources().getDrawable(avatarResource);
        ImageView avatar = (ImageView) findViewById(avatarId);
        avatar.setImageDrawable(avatarImg);
    }

    private void setTitleColor(int resourceId, int titleColor) {
        TextView title = (TextView) findViewById(resourceId);
        title.setTextColor(titleColor);
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

    // Show a "!" next to permissions icon to signify pending request
    private void notifyNewPermissionRequest() {
        Call<List<PermissionRequest>> caller = instance.getProxy().getPendingPermissions(instance.getCurrentUser().getId());
        ProxyBuilder.callProxy(this, caller, returnedList -> onNotifyNewPermissionRequestResponse(returnedList));
    }

    private void onNotifyNewPermissionRequestResponse(List<PermissionRequest> returnedList) {
        Log.d(TAG, "onNotifyNewPermissionRequestResponse: returnedList: " + returnedList);

        TextView exclamationMark = findViewById(R.id.dashBoard_permissionsExclamation);

        if (returnedList.size() == 0) {
            exclamationMark.setVisibility(View.INVISIBLE);
        } else {
            exclamationMark.setVisibility(View.VISIBLE);
        }
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

