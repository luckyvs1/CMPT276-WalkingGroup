package olive.walkinggroup.dataobjects;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import olive.walkinggroup.R;

public  class Rewards {

    private Context context;
    public static final int NUM_TIERS = 10;
    public static final int NUM_ICONS_PER_TIER = 3;
    static List<String> title = new ArrayList<>();
    static List<List<Integer>> icons = new ArrayList<>(NUM_TIERS);

    public String getTierTitle(int pos){
        return title.get(pos);
    }

    public List<Integer>  getTierIcons (int pos){
        return icons.get(pos);
    }

    public Rewards(Context context){
        List<String> stringList = Arrays.asList(new String[]{"A", "B", "A", "B", "C", "D","A", "B", "C", "D"});
        title.addAll(stringList);


        TypedArray drawablesArray = context.getResources().obtainTypedArray(R.array.avatar_imgs);

        for (int i = 0; i < NUM_TIERS; i++) {
            icons.add(new ArrayList<>(NUM_ICONS_PER_TIER));
            for (int j = 0; j < NUM_ICONS_PER_TIER; j++) {
                icons.get(i).add(drawablesArray.getResourceId(i*NUM_ICONS_PER_TIER+j,-1));
            }
        }
        Log.i("MyApp", icons.toString());
        drawablesArray.recycle();


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
        return icons.subList(currentTier + 1 , NUM_TIERS);
    }
}
