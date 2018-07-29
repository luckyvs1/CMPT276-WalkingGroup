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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
        requestDisplayList = requestList;
        populatePermissionRequestList();
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
                    approvedUsersContainer.setVisibility(View.VISIBLE);
                    deniedTag.setVisibility(View.GONE);
                    deniedUserContainer.setVisibility(View.GONE);
                    pendingTag.setVisibility(View.GONE);
                    pendingUsersContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.GONE);
                    break;

                case DENIED:
                    deniedTag.setVisibility(View.VISIBLE);
                    deniedUserContainer.setVisibility(View.VISIBLE);
                    approvedTag.setVisibility(View.GONE);
                    approvedUsersContainer.setVisibility(View.GONE);
                    pendingTag.setVisibility(View.GONE);
                    pendingUsersContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.GONE);
                    break;

                case PENDING:
                    pendingTag.setVisibility(View.VISIBLE);
                    pendingUsersContainer.setVisibility(View.VISIBLE);
                    approvedTag.setVisibility(View.GONE);
                    approvedUsersContainer.setVisibility(View.VISIBLE);
                    deniedTag.setVisibility(View.GONE);
                    deniedUserContainer.setVisibility(View.GONE);

                    actionBtnContainer.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }

            // Display request message
            messageTextView.setText(currentRequest.getMessage());

            // TODO: Display authorizors
//
//            Set<PermissionRequest.Authorizor> authorizors = currentRequest.getAuthorizors();
//
//            String approvedUsers = "";
//            String deniedUser = "";
//            String pendingUsers = "";
//
//            for (PermissionRequest.Authorizor authorizor : authorizors) {
//                switch (authorizor.getStatus()) {
//                    case APPROVED:
//                        User userApproved = authorizor.getWhoApprovedOrDenied();
//
//                        if (!Objects.equals(approvedUsers, "")) {
//                            approvedUsers += ", ";
//                        }
//
//                        String userApprovedText = getUserNameFromMap(userApproved.getId());
//                        approvedUsers += userApprovedText;
//                        break;
//
//                    case DENIED:
//                        User userDenied = authorizor.getWhoApprovedOrDenied();
//                        String userDeniedText = getUserNameFromMap(userDenied.getId()) + ", ";
//
//                    case PENDING:
//
//                    default:
//                        break;
//                }
//            }

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

            return itemView;
        }



        private String getUserNameFromMap(long id) {
            Integer userId = (int) id;
            return userNameMap.get(userId);
        }
    }
}
