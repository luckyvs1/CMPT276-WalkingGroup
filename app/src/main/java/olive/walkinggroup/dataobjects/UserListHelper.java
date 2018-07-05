package olive.walkinggroup.dataobjects;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.app.GroupDetailsActivity;

public class UserListHelper {
    private Activity activity;
    private List<User> userList;
    private User currentUser;
    private MemberListAdapter adapter;

    public UserListHelper(Activity activity, List<User> userList, User currentUser) {
        this.activity = activity;
        this.userList = userList;
        this.currentUser = currentUser;
        adapter = new MemberListAdapter();
    }

    public MemberListAdapter getAdapter() {
        return adapter;
    }

    private class MemberListAdapter extends ArrayAdapter<User> {
        public MemberListAdapter() {
            super(activity, R.layout.list_user_item, userList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = activity.getLayoutInflater().inflate(R.layout.list_user_item, parent, false);
            }
            User user = userList.get(position);

            setupMemberNameView(itemView, user);
            setupMemberEmailView(itemView, user);
            displayTag(itemView, user);

            return itemView;
        }
    }

    private void setupMemberNameView(View itemView, User user) {
        TextView nameView = itemView.findViewById(R.id.listMembers_name);
        String nameText = user.getName();
        nameView.setText(nameText);
    }

    private void setupMemberEmailView(View itemView, User user) {
        TextView emailView = itemView.findViewById(R.id.listMembers_email);
        String emailText = user.getEmail();
        emailView.setText(emailText);
    }

    private void displayTag(View itemView, User user) {
        // Hide the youTag if currentUser is not user (compared by Id)
        if (!(Objects.equals(currentUser.getId(), user.getId()))) {
            RelativeLayout youTag = itemView.findViewById(R.id.listUsers_youTag);
            youTag.setVisibility(View.GONE);
        }
        // Hide the monitorTag if user is not on monitor list of currentUser
        if (!(isOnMonitorsUserList(currentUser, user))) {
            RelativeLayout monitorTag = itemView.findViewById(R.id.listUsers_monitorTag);
            monitorTag.setVisibility(View.GONE);
        }
    }

    // Return true if user is on List<User> monitorsUsers of currentUser
    private boolean isOnMonitorsUserList(User currentUser, User user) {
        List<User> monitorList = currentUser.getMonitorsUsers();
        List<Integer> idList = new ArrayList<>();

        for (int i = 0; i < monitorList.size(); i++) {
            Integer id = monitorList.get(i).getId().intValue();
            idList.add(id);
        }

        if (user.getId() == null) {
            return false;
        }
        return (idList.contains(user.getId().intValue()));
    }
}
