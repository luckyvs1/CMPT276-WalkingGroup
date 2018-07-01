package olive.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Store information about the walking groups.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase{

    private String groupName;
    private String groupLeaderName;
    private String groupDescription;
    private LatLng startPoint;
    private LatLng endPoint;

    public Group(String groupName,
                 String groupDescription,
                 String groupLeaderName,
                 LatLng startPoint,
                 LatLng endPoint) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupLeaderName = groupLeaderName;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupLeaderName() {
        return groupLeaderName;
    }

    public void setGroupLeaderName(String groupLeaderName) {
        this.groupLeaderName = groupLeaderName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public LatLng getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public LatLng getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }
}
