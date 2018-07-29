package olive.walkinggroup.dataobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import olive.walkinggroup.proxy.WGServerProxy;

public class PermissionHelper {
    public List<PermissionRequest> getAllRequestsWithStatus(List<PermissionRequest> requestList, WGServerProxy.PermissionStatus status) {
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
        Set<Integer> approvedUsersIdSet = new HashSet<>();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.APPROVED)) {
                approvedUsersIdSet.add(authorizorGroup.getWhoApprovedOrDenied().getId().intValue());
            }
        }

        StringBuilder nameListString = new StringBuilder();

        for (Integer id : approvedUsersIdSet) {
            if (!nameListString.toString().equals("")) {
               nameListString.append(", ");
            }

            String name = nameMap.get(id);
            nameListString.append(name);

            if (id.equals(currentUser.getId().intValue())) {
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
        Set<User> approvedUsersSet = new HashSet<>();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.PENDING)) {
                approvedUsersSet.addAll(authorizorGroup.getUsers());
            }
        }

        StringBuilder nameListString = new StringBuilder();

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

    public static boolean hasActionDoneOnBehalf(PermissionRequest request, User user, HashMap<Integer, String> nameMap) {
        return (getActionDoneOnBehalfString(request, user, nameMap)) != null;
    }

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
}
