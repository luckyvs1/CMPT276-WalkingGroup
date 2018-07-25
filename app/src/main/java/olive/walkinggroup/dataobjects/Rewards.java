package olive.walkinggroup.dataobjects;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public  class Rewards {

    private Context context;
    public static final int NUM_TIERS = 10;
    static List<String> title = new ArrayList<>();
    static List<List<Integer>> icons = new ArrayList<>();

    public String getTierTitle(int pos){
        return title.get(pos);
    }

    public List<Integer>  getTierIcons (int pos){
        return icons.get(pos);
    }

    public Rewards(Context context){
        List<String> stringList = Arrays.asList(new String[]{"A", "B", "A", "B", "C", "D","A", "B", "C", "D"});
        title.addAll(stringList);

        List<Integer> intList = Arrays.asList(new Integer[] {1,2,3});
        icons.add(intList);
        icons.add(intList);
        icons.add(intList);
        icons.add(intList);
        icons.add(intList);
        icons.add(intList);
    }

    public List<String> getUnlockedTitlesUpToTier(int currentTier) {
        return title.subList(0, currentTier);
    }

    public List<List<Integer>> getUnlockedIconsUpToTier(int currentTier) {
        return icons.subList(0, currentTier);
    }

    public List<String> getLockedTitlesFromTier(int currentTier) {
        return title.subList(currentTier + 1, NUM_TIERS);
    }

    public List<List<Integer>> getLockedIconsFromTier(int currentTier) {
        return icons.subList(currentTier + 1, NUM_TIERS);
    }
}
