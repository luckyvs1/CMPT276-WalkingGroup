package olive.walkinggroup.dataobjects;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import olive.walkinggroup.R;

/**
 * Rewards class to setup and share titles and icons per tier.
 * Contains methods that are used to interact with the rewards for a user and the tiers.
 */

public  class Rewards {

    private Context context;


    public static final int NUM_TIERS = 10;
    public static final int NUM_ICONS_PER_TIER = 3;
    private List<String> titles;
    private List<List<Integer>> icons = new ArrayList<>(NUM_TIERS);

    public String getTierTitle(int pos){
        return titles.get(pos);
    }

    public List<Integer>  getTierIcons (int pos){
        return icons.get(pos);
    }

    public Rewards(Context context){

        String[] titlesArray = context.getResources().getStringArray(R.array.titleNames);
        titles = new ArrayList<>(Arrays.asList(titlesArray));

        TypedArray drawablesArray = context.getResources().obtainTypedArray(R.array.avatar_imgs);

        for (int i = 0; i < NUM_TIERS; i++) {
            icons.add(new ArrayList<>(NUM_ICONS_PER_TIER));
            for (int j = 0; j < NUM_ICONS_PER_TIER; j++) {
                icons.get(i).add(drawablesArray.getResourceId(i*NUM_ICONS_PER_TIER+j,-1));
            }
        }
        drawablesArray.recycle();


    }

    public List<String> getUnlockedTitlesUpToTier(int currentTier) {
        return titles.subList(0, currentTier);
    }

    public List<List<Integer>> getUnlockedIconsUpToTier(int currentTier) {
        return icons.subList(0, currentTier);
    }

    public List<String> getLockedTitlesFromTier(int currentTier) {
        return titles.subList(currentTier + 1, NUM_TIERS);
    }

    public List<List<Integer>> getLockedIconsFromTier(int currentTier) {
        return icons.subList(currentTier + 1 , NUM_TIERS);
    }
}
