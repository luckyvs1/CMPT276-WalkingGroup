package olive.walkinggroup.dataobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import olive.walkinggroup.proxy.WGServerProxy;

public class PermissionHelper {

    public static List<PermissionRequest> getAllRequestsWithStatus(List<PermissionRequest> requestList, WGServerProxy.PermissionStatus status) {
        List<PermissionRequest> returnList = new ArrayList<>();

        for (PermissionRequest request : requestList) {
            if (request.getStatus().equals(status)) {
                returnList.add(request);
            }
        }

        return returnList;
    }

    public static String makeDisplayActionString(String action) {
        switch (action) {
            case "A_START_MONITOR_B":
                return "Request to monitor user";

            case "A_STOP_MONITORING_B":
                return "Request to stop monitoring";

            case "A_JOIN_GROUP":
                return "Request to join group";

            case "A_LEAVE_GROUP":
                return "Request to leave group";

            case "A_LEAD_GROUP":
                return "Request to lead group";

            default:
                return "Permission request";
        }
    }

    public static Set<User> getAllUsers(List<PermissionRequest> requestList) {
        Set<User> userSet = new HashSet<>();

        for (PermissionRequest request : requestList) {
            Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();

            for (PermissionRequest.Authorizor authorizor : authorizors) {
                userSet.addAll(authorizor.getUsers());
            }
        }

        return userSet;
    }

    public static String makeApproveUserList(PermissionRequest request, HashMap<Integer, String> nameMap, User currentUser) {
        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();
        Set<User> approvedUsersSet = new HashSet<>();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.APPROVED)) {
                approvedUsersSet.add(authorizorGroup.getWhoApprovedOrDenied());
            }
        }

        StringBuilder nameListString = new StringBuilder();
        approvedUsersSet = UserListHelper.sortUserSetById(approvedUsersSet);

        for (User user : approvedUsersSet) {
            if (!nameListString.toString().equals("")) {
               nameListString.append(", ");
            }

            String name = nameMap.get(user.getId().intValue());
            nameListString.append(name);

            if (user.getId().equals(currentUser.getId())) {
                nameListString.append(" (You)");
            }
        }

        return nameListString.toString();
    }

    public static String getDeniedUserName(PermissionRequest request, HashMap<Integer, String> nameMap, User currentUser) {
        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.DENIED)) {
                String returnString = nameMap.get(authorizorGroup.getWhoApprovedOrDenied().getId().intValue());

                if (authorizorGroup.getWhoApprovedOrDenied().getId().equals(currentUser.getId())) {
                    returnString += " (You)";
                }

                return returnString;
            }
        }

        return "";
    }

    public static String makePendingUserList(PermissionRequest request, HashMap<Integer, String> nameMap, User currentUser) {
        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();
        Set<User> pendingUsersSet = new HashSet<>();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.PENDING)) {
                pendingUsersSet.addAll(authorizorGroup.getUsers());
            }
        }

        StringBuilder nameListString = new StringBuilder();
        pendingUsersSet = UserListHelper.sortUserSetById(pendingUsersSet);

        for (User user : pendingUsersSet) {
            if (!nameListString.toString().equals("")) {
                nameListString.append(", ");
            }

            String name = nameMap.get(user.getId().intValue());
            nameListString.append(name);

            if (user.getId().equals(currentUser.getId())) {
                nameListString.append(" (You)");
            }
        }

        return nameListString.toString();
    }

    // Check if some other user has made decision on behalf of user
    public static boolean hasActionDoneOnBehalf(PermissionRequest request, User user, HashMap<Integer, String> nameMap) {
        return (getActionDoneOnBehalfString(request, user, nameMap)) != null;
    }

    /**
     * Return a string containing information about if an action has been done on behalf of a user.
     * If a user belongs to one authorizor group only and the authorizor group has approved/denied,
     * and if the user is not the one authorizing it, this function returns a string as:
     * "<user_name> has approved/denied", where user_name is the user who authorized the action.
     *
     * If the user belongs to multiple authorizor groups, the function will only return such string if
     * no authorizor group has status "PENDING" (i.e. the user can no longer make any decision).
     *
     * Otherwise, the function returns null.
     */


    public static String getActionDoneOnBehalfString(PermissionRequest request, User user, HashMap<Integer, String> nameMap) {
        long userId = user.getId();

        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();
        Set<PermissionRequest.Authorizor> myAuthorizorGroups = new HashSet<>();

        User actionDoneOnBehalfBy = new User();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            Set<User> userList = authorizorGroup.getUsers();
            boolean userIsInList = false;

            for (User eachUser : userList) {
                if (eachUser.getId().equals(userId)) {
                    userIsInList = true;
                }
            }

            if (userIsInList) {
                myAuthorizorGroups.add(authorizorGroup);
            }
        }

        String actionString = "";

        for (PermissionRequest.Authorizor authorizorGroup : myAuthorizorGroups) {
            // If authorizor group is still pending
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.PENDING)) {
                return null;
            } else {
                actionDoneOnBehalfBy = authorizorGroup.getWhoApprovedOrDenied();

                if (actionDoneOnBehalfBy.getId().equals(userId)) {
                    return null;
                }

                if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.APPROVED)) {
                    actionString = " has approved";
                } else {
                    actionString = " has denied";
                }
            }
        }

        if (actionDoneOnBehalfBy.getId() != null) {
            String onBehalfByUserName = nameMap.get(actionDoneOnBehalfBy.getId().intValue());
            return (onBehalfByUserName + actionString);
        }

        return null;
    }

    public static boolean userHasMadeDecision(PermissionRequest request, User user) {
        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();

        for (PermissionRequest.Authorizor authorizor : authorizors) {
            Set<User> userSet = authorizor.getUsers();

            for (User eachUser : userSet) {
                if (eachUser.getId().equals(user.getId())) {
                    return (!(authorizor.getStatus().equals(WGServerProxy.PermissionStatus.PENDING)));
                }
            }
        }

        return false;
    }
}
