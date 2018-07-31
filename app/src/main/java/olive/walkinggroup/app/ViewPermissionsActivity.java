package olive.walkinggroup.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.PermissionHelper;
import olive.walkinggroup.dataobjects.PermissionRequest;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ViewPermissionsActivity extends AppCompatActivity {

    private static final String TAG = "ViewPermissionsActivity";
    Set<User> rawUserSet = new HashSet<>();
    Set<User> detailedUserSet = new HashSet<>();
    HashMap<Integer, String> userNameMap = new HashMap<>();

    List<PermissionRequest> requestFullList = new ArrayList<>();
    List<PermissionRequest> requestDisplayList = new ArrayList<>();

    Model model;
    WGServerProxy proxy;
    User currentUser;

    ToggleButton pendingBtn;
    ToggleButton approvedBtn;
    ToggleButton deniedBtn;
    ToggleButton allBtn;
    List<ToggleButton> toggleButtons = new ArrayList<>();
    String selectedStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_permissions);

        model = Model.getInstance();
        proxy = model.getProxy();
        currentUser = model.getCurrentUser();

        setupFilterToggleBtns();
        setupRefreshBtn();
        getMyPermissionRequests();
    }

    private void setupFilterToggleBtns() {
        pendingBtn = findViewById(R.id.viewPermissions_pendingBtn);
        approvedBtn = findViewById(R.id.viewPermissions_approvedBtn);
        deniedBtn = findViewById(R.id.viewPermissions_deniedBtn);
        allBtn = findViewById(R.id.viewPermissions_allBtn);

        toggleButtons.add(pendingBtn);
        toggleButtons.add(approvedBtn);
        toggleButtons.add(deniedBtn);
        toggleButtons.add(allBtn);

        // Default selected status: pending
        pendingBtn.setChecked(true);
        selectedStatus = pendingBtn.getTextOn().toString();

        for (ToggleButton btn : toggleButtons) {
            setToggleBtnOnClickListener(btn);
        }
    }

    private void setToggleBtnOnClickListener(ToggleButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStatus = button.getTextOn().toString();
                Log.d(TAG, "Currently displaying requests with status: " + selectedStatus);
                populatePermissionRequestList();

                for (ToggleButton btn : toggleButtons) {
                    if (!button.equals(btn)) {
                        btn.setChecked(false);
                    } else {
                        btn.setChecked(true);
                    }
                }
            }
        });
    }

    private void setupRefreshBtn() {
        RelativeLayout refreshBtn = findViewById(R.id.viewPermissions_refreshBtn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyPermissionRequests();
            }
        });
    }

    private void getMyPermissionRequests() {
        showLoadingCircle();
        editProgressBarText("Updating permission requests...");
        updateProgressBarProgress(5);

        Call<List<PermissionRequest>> caller = proxy.getPermissions(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, requestList -> onGetMyPermissionRequestsResponse(requestList));
    }

    private void onGetMyPermissionRequestsResponse(List<PermissionRequest> requestList) {
        if (requestList.size() == 0) {
            hideLoadingCircle();
            showEmpty();
            return;
        }

        updateProgressBarProgress(50);
        Collections.reverse(requestList);
        requestFullList = requestList;
        buildUserNameMap();
    }

    private void buildUserNameMap() {
        editProgressBarText("Updating user name list...");
        rawUserSet = PermissionHelper.getAllUsers(requestFullList);

        for (User user : rawUserSet) {
            Call<User> caller = proxy.getUserById(user.getId());
            ProxyBuilder.callProxy(this, caller, detailedUser -> onBuildUserNameMapResponse(detailedUser));
        }
    }

    private void onBuildUserNameMapResponse(User detailedUser) {
        detailedUserSet.add(detailedUser);
        userNameMap.put(detailedUser.getId().intValue(), detailedUser.getName());

        int progress = ((detailedUserSet.size()) * 50) / (rawUserSet.size());
        Log.d(TAG, "progress" + progress);
        updateProgressBarProgress(50 + progress);

        if (detailedUserSet.size() == rawUserSet.size()) {
            populatePermissionRequestList();
        }
    }

    private void populatePermissionRequestList() {
        switch (selectedStatus) {
            case "Pending":
                requestDisplayList = PermissionHelper.getAllRequestsWithStatus(requestFullList, WGServerProxy.PermissionStatus.PENDING);
                break;
            case "Approved":
                requestDisplayList = PermissionHelper.getAllRequestsWithStatus(requestFullList, WGServerProxy.PermissionStatus.APPROVED);
                break;
            case "Denied":
                requestDisplayList = PermissionHelper.getAllRequestsWithStatus(requestFullList, WGServerProxy.PermissionStatus.DENIED);
                break;
            default:
                requestDisplayList = requestFullList;
                break;
        }

        if (requestDisplayList.size() == 0) {
            showEmpty();
        } else {
            hideEmpty();
        }

        ListView requestList = findViewById(R.id.viewPermissions_list);
        PermissionRequestListAdapter adapter = new PermissionRequestListAdapter();
        requestList.setAdapter(adapter);

        updateProgressBarProgress(100);
        hideLoadingCircle();
    }

    public class PermissionRequestListAdapter extends ArrayAdapter<PermissionRequest> {
        public PermissionRequestListAdapter() {
            super(ViewPermissionsActivity.this, R.layout.list_permission_item, requestDisplayList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;

            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_permission_item, parent, false);
            }

            PermissionRequest currentRequest = requestDisplayList.get(position);

            // Views from top to bottom
            TextView actionTextView = itemView.findViewById(R.id.permissionItem_actionTxt);

            TextView approvedTag = itemView.findViewById(R.id.permissionItem_approvedTag);
            TextView deniedTag = itemView.findViewById(R.id.permissionItem_deniedTag);
            TextView pendingTag = itemView.findViewById(R.id.permissionItem_pendingTag);

            TextView messageTextView = itemView.findViewById(R.id.permissionItem_message);

            LinearLayout approvedUsersContainer = itemView.findViewById(R.id.permissionItem_usersApprovedContainer);
            TextView approvedUsersTextView = itemView.findViewById(R.id.permissionItem_usersApproved);
            LinearLayout deniedUserContainer = itemView.findViewById(R.id.permissionItem_userDeniedContainer);
            TextView deniedUserTextView = itemView.findViewById(R.id.permissionItem_userDenied);
            LinearLayout pendingUsersContainer = itemView.findViewById(R.id.permissionItem_userPendingContainer);
            TextView pendingUsersTextView = itemView.findViewById(R.id.permissionItem_usersPending);

            LinearLayout actionBtnContainer = itemView.findViewById(R.id.permissionItem_actionButtonContainer);
            Button approveBtn = itemView.findViewById(R.id.permissionItem_approveBtn);
            Button denyBtn = itemView.findViewById(R.id.permissionItem_denyBtn);

            LinearLayout onBehalfContainer = itemView.findViewById(R.id.permissionItem_onBehalfContainer);
            TextView onBehalfTextView = itemView.findViewById(R.id.permissionItem_onBehalfText);

            // Make descriptive action text
            actionTextView.setText(PermissionHelper.makeDisplayActionString(currentRequest.getAction()));

            // Hide and Display UI
            switch (currentRequest.getStatus()) {
                case APPROVED:
                    approvedTag.setVisibility(View.VISIBLE);
                    deniedTag.setVisibility(View.GONE);
                    pendingTag.setVisibility(View.GONE);

                    approvedUsersContainer.setVisibility(View.VISIBLE);
                    deniedUserContainer.setVisibility(View.GONE);
                    pendingUsersContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.GONE);
                    onBehalfContainer.setVisibility(View.VISIBLE);
                    break;

                case DENIED:
                    deniedTag.setVisibility(View.VISIBLE);
                    approvedTag.setVisibility(View.GONE);
                    pendingTag.setVisibility(View.GONE);

                    deniedUserContainer.setVisibility(View.VISIBLE);
                    approvedUsersContainer.setVisibility(View.GONE);
                    pendingUsersContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.GONE);
                    onBehalfContainer.setVisibility(View.GONE);
                    break;

                case PENDING:
                    pendingTag.setVisibility(View.VISIBLE);
                    approvedTag.setVisibility(View.GONE);
                    deniedTag.setVisibility(View.GONE);

                    pendingUsersContainer.setVisibility(View.VISIBLE);
                    approvedUsersContainer.setVisibility(View.VISIBLE);
                    deniedUserContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.VISIBLE);
                    onBehalfContainer.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }

            // Display request message
            messageTextView.setText(currentRequest.getMessage());

            // Display authorizors, grouped by status
            approvedUsersTextView.setText(PermissionHelper.makeApproveUserList(currentRequest, userNameMap, currentUser));
            deniedUserTextView.setText(PermissionHelper.getDeniedUserName(currentRequest, userNameMap, currentUser));
            pendingUsersTextView.setText(PermissionHelper.makePendingUserList(currentRequest, userNameMap, currentUser));

            // Hide action buttons if currentUser has approved/denied a pending request already
            if (PermissionHelper.userHasMadeDecision(currentRequest, currentUser)) {
                actionBtnContainer.setVisibility(View.GONE);
            }

            // Action buttons onClickListeners
            approveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(currentRequest.getId(), WGServerProxy.PermissionStatus.APPROVED);
                    ProxyBuilder.callProxy(ViewPermissionsActivity.this, caller, returnedRequest -> getMyPermissionRequests());
                }
            });

            denyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(currentRequest.getId(), WGServerProxy.PermissionStatus.DENIED);
                    ProxyBuilder.callProxy(ViewPermissionsActivity.this, caller, returnedRequest -> getMyPermissionRequests());
                }
            });

            // Display "on behalf" text
            // Show when an action is done on behalf of currentUser in an authorizor group.
            // Do not show when currentUser can still authorize in a different authorizor group.
            if (!(PermissionHelper.hasActionDoneOnBehalf(currentRequest, currentUser, userNameMap))) {
                onBehalfContainer.setVisibility(View.GONE);
            } else {
                String onBehalfText = getResources().getString(R.string.permissionItem_onBehalfTxt);
                String actionDoneOnBehalfString = PermissionHelper.getActionDoneOnBehalfString(currentRequest, currentUser, userNameMap);
                onBehalfText = actionDoneOnBehalfString + onBehalfText;
                onBehalfTextView.setText(onBehalfText);

                actionBtnContainer.setVisibility(View.GONE);
            }

            return itemView;
        }
    }

    private void showLoadingCircle() {
        for (Button btn : toggleButtons) {
            btn.setEnabled(false);
        }

        RelativeLayout loadingCircle = findViewById(R.id.viewPermissions_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingCircle() {
        for (Button btn : toggleButtons) {
            btn.setEnabled(true);
        }

        RelativeLayout loadingCircle = findViewById(R.id.viewPermissions_loading);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
        }
    }

    private void editProgressBarText(String text) {
        TextView progressBarTextView = findViewById(R.id.viewPermissions_progressBarTextView);
        progressBarTextView.setText(text);
    }

    private void updateProgressBarProgress(int currentProgress) {
        ProgressBar progressBar = findViewById(R.id.viewPermissions_progressBar);
        progressBar.setProgress(currentProgress);
    }

    private void showEmpty() {
        LinearLayout empty = findViewById(R.id.viewPermissions_empty);

        if (empty != null) {
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmpty() {
        LinearLayout empty = findViewById(R.id.viewPermissions_empty);

        if (empty != null) {
            empty.setVisibility(View.GONE);
        }
    }
}
