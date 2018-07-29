package olive.walkinggroup.app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

    List<PermissionRequest> requestDisplayList = new ArrayList<>();

    Model model;
    WGServerProxy proxy;
    User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_permissions);

        model = Model.getInstance();
        proxy = model.getProxy();
        currentUser = model.getCurrentUser();

        getMyPermissionRequests();
    }

    private void getMyPermissionRequests() {
        Call<List<PermissionRequest>> caller = proxy.getPermissions(currentUser.getId());
        ProxyBuilder.callProxy(this, caller, requestList -> onGetMyPermissionRequestsResponse(requestList));
    }

    private void onGetMyPermissionRequestsResponse(List<PermissionRequest> requestList) {
        Collections.reverse(requestList);
        requestDisplayList = requestList;
        buildUserNameMap();
    }

    private void buildUserNameMap() {
        rawUserSet = PermissionHelper.getAllUsers(requestDisplayList);

        for (User user : rawUserSet) {
            Call<User> caller = proxy.getUserById(user.getId());
            ProxyBuilder.callProxy(this, caller, detailedUser -> onBuildUserNameMapResponse(detailedUser));
        }
    }

    private void onBuildUserNameMapResponse(User detailedUser) {
        detailedUserSet.add(detailedUser);
        userNameMap.put(detailedUser.getId().intValue(), detailedUser.getName());

        if (detailedUserSet.size() == rawUserSet.size()) {
            populatePermissionRequestList();
        }
    }

    private void populatePermissionRequestList() {
        ListView requestList = findViewById(R.id.viewPermissions_list);
        PermissionRequestListAdapter adapter = new PermissionRequestListAdapter();
        requestList.setAdapter(adapter);

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

            // Action buttons onClickListeners
            approveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(currentRequest.getId(), WGServerProxy.PermissionStatus.APPROVED);
                    ProxyBuilder.callProxy(ViewPermissionsActivity.this, caller, null);
                }
            });

            denyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<PermissionRequest> caller = proxy.approveOrDenyPermissionRequest(currentRequest.getId(), WGServerProxy.PermissionStatus.DENIED);
                    ProxyBuilder.callProxy(ViewPermissionsActivity.this, caller, null);
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
}
