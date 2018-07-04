package olive.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Store information about the walking groups.
 *
 * Information includes a group name and its description
 * and array of latitudes and longitudes that represents its walking route.
 * The array of latitudes and longitudes currently stores only the group's start and end points.
 *
 * Operations include checking if a user is a member of the group, or a leader of the group
 * and returning a user's position in the group's member list.  The Group class also allows
 * setting a start and end point.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase implements Serializable{

    private String groupName;
    private String groupDescription;
    private User leader;
    private double[] routeLatArray;
    private double[] routeLngArray;
    private List<User> memberUsers = new ArrayList<>();

    public Group(String groupName,
                 String groupDescription,
                 User leader,
                 double[] routeLatArray,
                 double[] routeLngArray,
                 List<User> memberUsers) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.leader = leader;
        this.routeLatArray = routeLatArray;
        this.routeLngArray = routeLngArray;
        this.memberUsers = memberUsers;
    }

    public Group() {
        // Dummy constructor
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public double[] getRouteLatArray() {
        return routeLatArray;
    }

    public void setRouteLatArray(double[] routeLngArray) {
        this.routeLatArray = routeLngArray;
    }

    public double[] getRouteLngArray() {
        return routeLngArray;
    }

    public void setRouteLngArray(double[] routeLngArray) {
        this.routeLngArray = routeLngArray;
    }

    public List<User> getMemberUsers() {
        return memberUsers;
    }

    public void setMemberUsers(List<User> memberUsers) {
        this.memberUsers = memberUsers;
    }

    public LatLng getStartPoint() {
        return new LatLng(routeLatArray[0], routeLngArray[0]);
    }

    public LatLng getEndPoint() {
        return new LatLng(routeLatArray[1], routeLngArray[1]);
    }

    public void setStartPoint(LatLng start) {
        routeLatArray[0] = start.latitude;
        routeLngArray[0] = start.longitude;
    }

    public void setEndPoint(LatLng end) {
        routeLatArray[1] = end.latitude;
        routeLngArray[1] = end.longitude;
    }

    public boolean isMember(User user) {
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < memberUsers.size(); i++) {
            idList.add(memberUsers.get(i).getId().intValue());
        }
        return idList.contains(user.getId().intValue());
    }

    public boolean isLeader(User user) {
        return (Objects.equals(leader.getId(), user.getId()));
    }

    public int getMemberListIndex(User user) {
        if (!(isMember(user))) {
            return -1;
        }
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < memberUsers.size(); i++) {
            idList.add(memberUsers.get(i).getId().intValue());
        }
        return idList.indexOf(user.getId().intValue());
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupDescription='" + groupDescription + '\'' +
                " ,routeLatArray=" + Arrays.toString(routeLatArray) +
                " ,routeLngArray=" + Arrays.toString(routeLngArray) +
                " ,leader=" + leader +
                " ,memberUsers=" + memberUsers +
                '}';
    }
}
