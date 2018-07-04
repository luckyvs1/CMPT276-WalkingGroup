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
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase implements Serializable{

    private String groupName;
    private String groupDescription;
    private User leader;
    private double[] latArray;
    private double[] lngArray;
    private List<User> members;

    public Group(String groupName,
                 String groupDescription,
                 User leader,
                 double[] latArray,
                 double[] lngArray,
                 List<User> members) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.leader = leader;
        this.latArray = latArray;
        this.lngArray = lngArray;
        this.members = members;
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

    public double[] getLatArray() {
        return latArray;
    }

    public void setLatArray(double[] latArray) {
        this.latArray = latArray;
    }

    public double[] getLngArray() {
        return lngArray;
    }

    public void setLngArray(double[] lngArray) {
        this.lngArray = lngArray;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public LatLng getStartPoint() {
        return new LatLng(latArray[0], lngArray[0]);
    }

    public LatLng getEndPoint() {
        return new LatLng(latArray[1], lngArray[1]);
    }

    public void setStartPoint(LatLng start) {
        latArray[0] = start.latitude;
        lngArray[0] = start.longitude;
    }

    public void setEndPoint(LatLng end) {
        latArray[1] = end.latitude;
        lngArray[1] = end.longitude;
    }

    public boolean isMember(User user) {
        List<Integer> idList = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            idList.add(members.get(i).getId().intValue());
        }
        return idList.contains(user.getId().intValue());
    }

    public boolean isLeader(User user) {
        return (Objects.equals(leader.getId(), user.getId()));
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupDescription='" + groupDescription + '\'' +
                '}';
    }
}
