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

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.PermissionRequest;

public class ViewPermissionsActivity extends AppCompatActivity {

    HashMap<Integer, String> userNameMap = new HashMap<>();

    List<PermissionRequest> requestDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_permissions);

        populatePermissionRequestList();
    }

    private void populatePermissionRequestList() {
        ListView requestList = findViewById(R.id.viewPermissions_list);

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

            // TODO: may need to make text clearer
            actionTextView.setText(currentRequest.getAction());

            // Display correct status tag
            switch (currentRequest.getStatus()) {
                case APPROVED:
                    approvedTag.setVisibility(View.VISIBLE);
                    break;
                case DENIED:
                    deniedTag.setVisibility(View.VISIBLE);
                    break;
                case PENDING:
                    pendingTag.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            // Display request message
            messageTextView.setText(currentRequest.getMessage());

            // Display authorizors


            return itemView;
        }
    }
}
