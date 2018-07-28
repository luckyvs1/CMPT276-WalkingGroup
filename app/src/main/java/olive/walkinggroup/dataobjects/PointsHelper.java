package olive.walkinggroup.dataobjects;

public class PointsHelper {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    int currentTier;
    int currentPoints;
    int totalPoints;
    int [] TierPoints = new int[10];

    public PointsHelper(){
        if(user.getTotalPointsEarned()==null)
        {totalPoints = 0;}
        else
        {totalPoints = user.getTotalPointsEarned();}

        if(user.getCurrentPoints()==null)
        {currentPoints = 0;}
        else
        {currentPoints = user.getCurrentPoints();}

        setTierPoints();
        currentTier = getCurrentTier();
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    private void setTierPoints() {
        int n1 = 0;
        int n2 = 4000;

        for(int i = 0 ; i < 10 ; i++){
            int n3 = n1 + n2;
            TierPoints[i] = n3;
            n1 = n2;
            n2 = n3;
        }
    }

    public int [] getTierPoints(){
        return TierPoints;
    }

    public int getCurrentTier() {
        currentTier = -1;
        for(int i = 0 ; i < 10 ; i++){
            if(totalPoints >= TierPoints[i]){
                currentTier = i;
            }
        }
        return currentTier;
    }

    public int getPointsNeeded(){
        int points = 0;
        points = TierPoints[currentTier+1] - currentPoints;
        return points;
    }
}
