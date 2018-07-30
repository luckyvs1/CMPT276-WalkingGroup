package olive.walkinggroup.dataobjects;

import android.content.Intent;
import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that your group can change the format of in (almost) any way you like
 * to encode the rewards that this user has earned.
 *
 * This class gets serialized/deserialized as part of a User object. Server stores it as
 * a JSON string, so it has no direct knowledge of what it contains.
 * (Rewards may not be used during first project iteration or two)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectedRewards implements Serializable{

    private Integer selectedIconId = 0;

    // Needed for JSON deserialization
    public SelectedRewards() {
    }

    public SelectedRewards(Integer selectedIconId) {
        this.selectedIconId = selectedIconId;
    }

    public Integer getSelectedIconId() {
        return selectedIconId;
    }

    public void setSelectedIconId(Integer selectedIconId) {
        this.selectedIconId = selectedIconId;
    }




}