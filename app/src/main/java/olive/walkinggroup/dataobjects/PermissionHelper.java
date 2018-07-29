package olive.walkinggroup.dataobjects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import olive.walkinggroup.proxy.WGServerProxy;

public class PermissionHelper {

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

    public static Boolean hasActionDoneOnBehalf(PermissionRequest request, User user) {
        long userId = user.getId();

        Set<PermissionRequest.Authorizor> authorizors = request.getAuthorizors();
        Set<PermissionRequest.Authorizor> myAuthorizorGroups = new HashSet<>();

        for (PermissionRequest.Authorizor authorizorGroup : authorizors) {
            Set<User> userList = authorizorGroup.getUsers();
            Boolean userIsInList = false;

            for (User eachUser : userList) {
                if (eachUser.getId().equals(userId)) {
                    userIsInList = true;
                }
            }

            if (userIsInList) {
                myAuthorizorGroups.add(authorizorGroup);
            }
        }

        for (PermissionRequest.Authorizor authorizorGroup : myAuthorizorGroups) {
            // If authorizor group has approved or denied
            if (authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.APPROVED)
                || authorizorGroup.getStatus().equals(WGServerProxy.PermissionStatus.DENIED))
            {
                // If user is

            }
        }
        return true;
    }

}
