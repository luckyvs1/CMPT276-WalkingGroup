package olive.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;

/**
 * Store information about the walking groups.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase{

    private LatLng endPos;
    private String name;
    private String leader;
    private String comment;

    private static Group instance;
    private Group() {
        // Prevents instantiation outside
    }

    public static Group getInstance() {
        if (instance == null) {
            instance = new Group();
        }
        return instance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static void setInstance(Group instance) {
        Group.instance = instance;
    }

    public String getName() {
        return name;
    }

    public String getLeader() {
        return leader;
    }

    public void setEndPos(LatLng endPos) {
        this.endPos = endPos;
    }

    public LatLng getEndPos() {
        return endPos;
    }
}
