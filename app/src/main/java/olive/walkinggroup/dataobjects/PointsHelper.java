package olive.walkinggroup.dataobjects;

/**
 * Helper class to calculate points and tiers based on current points.
 * Contains methods that are used in Points related activities.
 */

public class PointsHelper {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    int max_Tier = Rewards.NUM_TIERS;
    int currentTier;
    int currentPoints;
    int totalPoints;
    int [] TierPoints = new int[max_Tier];

    int n1 = 0;
    int n2 = 4000;

    public PointsHelper(){
        if (user.getTotalPointsEarned()==null) {
            totalPoints = 0;
        } else {
            totalPoints = user.getTotalPointsEarned();
        }

        currentPoints = totalPoints;
        setTierPoints();
        currentTier = getCurrentTier();
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int [] getTierPoints(){
        return TierPoints;
    }

    public int getCurrentTier() {
        currentTier = -1;
        for(int i = 0 ; i < max_Tier ; i++){
            if(totalPoints >= TierPoints[i]){
                currentTier = i;
            }
        }
        return currentTier;
    }

    public int getPointsNeeded(){
        int points = 0;
        if(currentTier < max_Tier-1){
        points = TierPoints[currentTier+1] - currentPoints;}
        return points;
    }

    private void setTierPoints() {
        int firstNum = n1;
        int secondNum = n2;

        for(int i = 0 ; i < max_Tier ; i++){
            int thirdNum = firstNum + secondNum;
            TierPoints[i] = thirdNum;
            firstNum = secondNum;
            secondNum = thirdNum;
        }
    }
}
