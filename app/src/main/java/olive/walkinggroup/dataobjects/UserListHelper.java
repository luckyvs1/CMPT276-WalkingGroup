package olive.walkinggroup.dataobjects;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import olive.walkinggroup.R;
import olive.walkinggroup.app.GroupDetailsActivity;

/**
 * UserListHelper class allows generating an ArrayAdapter to be used in a ListView of users
 * using an activity object, a user list and the current logged-in user.
 */


public class UserListHelper {
    private Activity activity;
    private List<User> userList;
    private User currentUser;
    private MemberListAdapter adapter;
    private TrackerListAdapter trackerListAdapter;
    private LeaderboardListAdapter leaderboardListAdapter;
    private static List<User> monitorList;

    public UserListHelper(Activity activity, List<User> userList, User currentUser, List<User> monitorList) {
        this.activity = activity;
        this.userList = userList;
        this.currentUser = currentUser;
        UserListHelper.monitorList = monitorList;
        adapter = new MemberListAdapter();
        trackerListAdapter = new TrackerListAdapter();

    }

    public UserListHelper(Activity activity, List<User> userList) {
        this.activity = activity;
        this.userList = userList;
        leaderboardListAdapter = new LeaderboardListAdapter();
    }

    public MemberListAdapter getAdapter() {
        return adapter;
    }

    private class MemberListAdapter extends ArrayAdapter<User> {
        public MemberListAdapter() {
            super(activity, R.layout.list_user_item, userList);
        }

        private void setupMemberNameView(View itemView, User user) {
            TextView nameView = itemView.findViewById(R.id.trackUser_name);
            String nameText = user.getName();
            nameView.setText(nameText);
        }

        private void setupMemberEmailView(View itemView, User user) {
            TextView emailView = itemView.findViewById(R.id.listMembers_email);
            String emailText = user.getEmail();
            emailView.setText(emailText);
        }

        private void displayTag(View itemView, User user) {
            RelativeLayout youTag = itemView.findViewById(R.id.listUsers_youTag);
            youTag.setVisibility(View.GONE);

            RelativeLayout monitorTag = itemView.findViewById(R.id.listUsers_monitorTag);
            monitorTag.setVisibility(View.GONE);

            if ((Objects.equals(currentUser.getId(), user.getId()))) {
                youTag.setVisibility(View.VISIBLE);
            }

            if ((isOnMonitorsUserList(currentUser, user))) {
                monitorTag.setVisibility(View.VISIBLE);
            }

            //Todo: add tag for users who monitors me, and leader of groups
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

    public TrackerListAdapter getTrackerListAdapter() {
        return trackerListAdapter;
    }

    private class TrackerListAdapter extends ArrayAdapter<User> {
        public TrackerListAdapter() {
            super(activity, R.layout.list_tracker_user_item, userList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = activity.getLayoutInflater().inflate(R.layout.list_tracker_user_item, parent, false);
            }
            User user = userList.get(position);

            setupTrackUserNameView(itemView, user);
            setupTrackUserLastUpdatedView(itemView, user);
            setupTrackUserTimestampView(itemView,user);
            displayTag(itemView, user);


            return itemView;
        }

        private void setupTrackUserTimestampView(View itemView, User user) {
            String timestamp = user.getLastGpsLocation().getTimestamp();
            TextView textView = itemView.findViewById(R.id.trackUser_timestamp);
            textView.setText("");
            if (timestamp != null) {
                String text = activity.getString(R.string.track_user_timestamp) + " " + timestamp;
                textView.setText(text);
            }

        }
        private void displayTag(View itemView, User user) {
            RelativeLayout leaderTag = itemView.findViewById(R.id.trackUserItem_leaderTag);
            leaderTag.setVisibility(View.GONE);

            RelativeLayout monitorTag = itemView.findViewById(R.id.trackUserItem_monitorTag);
            monitorTag.setVisibility(View.GONE);

            for (int i = 0; i < monitorList.size(); i++) {
                if (isGroupLeaderForCurrentUser(monitorList.get(i),user)) {
                    leaderTag.setVisibility(View.VISIBLE);
                }
            }

            if (isOnMonitorsUserList(currentUser, user)) {
                monitorTag.setVisibility(View.VISIBLE);
            }

        }

        private void setupTrackUserLastUpdatedView(View itemView, User user) {
            TextView textView = itemView.findViewById(R.id.trackUser_lastUpdated);

            GetLastUpdated getLastUpdated = new GetLastUpdated(activity);
            String lastUpdated = getLastUpdated.getLastUpdatedString(user.getLastGpsLocation().getTimestamp());

            textView.setText(lastUpdated);
        }

        private void setupTrackUserNameView(View itemView, User user) {
            TextView textView = itemView.findViewById(R.id.trackUser_name);
            textView.setText(user.getName());
        }

    }

    public LeaderboardListAdapter getLeaderboardListAdapter() {
        return leaderboardListAdapter;
    }


    private class LeaderboardListAdapter extends ArrayAdapter<User> {
        public LeaderboardListAdapter() {
            super(activity, R.layout.list_leaderboard_item, userList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = activity.getLayoutInflater().inflate(R.layout.list_leaderboard_item, parent, false);
            }
            User user = userList.get(position);
            setupLeaderboardUserNameView(itemView, user);
            setupLeaderboardUserRankView(itemView, position);
            setupLeaderboardUserPointsView(itemView,user);
            return itemView;
        }

        private void setupLeaderboardUserPointsView(View itemView, User user) {
            TextView textView = itemView.findViewById(R.id.leaderboardUser_points);
            String points = user.getTotalPointsEarned() + "";
            textView.setText(points);
        }

        private void setupLeaderboardUserRankView(View itemView, int position) {
            TextView textView = itemView.findViewById(R.id.leaderboardUser_rank);
            String rank = (position + 1) + "";
            textView.setText(rank);
        }

        private void setupLeaderboardUserNameView(View itemView, User user) {
            TextView textView = itemView.findViewById(R.id.leaderboardUser_name);
            textView.setText(user.getName());
        }
    }



    // Return true if user is on List<User> monitorsUsers of currentUser
    public static boolean isOnMonitorsUserList(User currentUser, User user) {
        List<Integer> idList = new ArrayList<>();
        List<User> monitorList = currentUser.getMonitorsUsers();

        if (monitorList == null) {
            return false;
        }

        for (int i = 0; i < monitorList.size(); i++) {
            Integer id = monitorList.get(i).getId().intValue();
            idList.add(id);
        }

        return user.getId() != null && (idList.contains(user.getId().intValue()));
    }


    //Todo: Verify if the function is correct or utilize new function to check for group leader
    // Return true if user is a leader of group which current user belongs to
    public static boolean isGroupLeaderForCurrentUser(User currentUser, User user) {
        Boolean isCurrentUsersGroupLeader = false;
        List<Group> memberGroupList = currentUser.getMemberOfGroups();
        List<Group> groupLeaderList = user.getLeadsGroups();

        for (int i = 0; i < memberGroupList.size(); i++) {
            for(int j = 0; j < groupLeaderList.size(); j++) {
                if(memberGroupList.get(i).getId().intValue() == groupLeaderList.get(j).getId().intValue()) {
                    isCurrentUsersGroupLeader = true;
                }
            }
        }
        return isCurrentUsersGroupLeader;
    }

    // Return true if current user and the user are the same individual
    public static boolean sameUser(User currentUser, User user) {
        Boolean isSameUser = false;
        if(currentUser.getId().intValue() == user.getId().intValue()){
            isSameUser = true;
        }
        return isSameUser;
    }

    public static List<User> sortUsers(List<User> listToSort) {
        Collections.sort(listToSort, new UserComparator());
        return listToSort;
    }

    public static class UserComparator implements Comparator<User> {

        @Override
        public int compare(User o1, User o2) {
            String name1 = o1.getName();
            String name2 = o2.getName();

            if (name1 != null && name2 != null) {
                int compareName = name1.compareToIgnoreCase(name2);

                if (compareName != 0) {
                    return compareName;
                } else {
                    String email1 = o1.getEmail();
                    String email2 = o2.getEmail();

                    if (email1 != null && email2 != null) {
                        return email1.compareTo(email2);
                    }
                    return 0;
                }
            }
            return 0;
        }
    }



}
