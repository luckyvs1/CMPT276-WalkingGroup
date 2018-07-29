package olive.walkinggroup.app;

import android.app.ActionBar;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.PointsHelper;
import olive.walkinggroup.dataobjects.User;

public class StoreActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();
    private PointsHelper pointsHelper = new PointsHelper();

    private int numTiers = 10;
    private int totalPoints;
    private int currentPoints;
    private int currentTier;
    private int pointsNeeded;
    private int [] TierPoints;

    TextView [] coverTiers = new TextView [numTiers];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        currentTier = pointsHelper.getCurrentTier();
        pointsNeeded = pointsHelper.getPointsNeeded();
        TierPoints = pointsHelper.getTierPoints();

        for(int i = 0 ; i < numTiers ; i++) {
            String ID = "cover_tier"+(i+1);
            int resID = getResources().getIdentifier(ID,"id",getPackageName());
            coverTiers [i] = findViewById(resID);
            coverTiers[i].setText("Get "+TierPoints[i]+" Total Points to Unlock");
        }

        setupPointsView();
        setupCoverVisibility();
//        setupTierClickable();
    }

    private void setupPointsView(){
        totalPoints = pointsHelper.getTotalPoints();
        currentPoints = pointsHelper.getCurrentPoints();
        TextView viewPoints = findViewById(R.id.shop_currentPoint);
        viewPoints.setText("Current points: "+currentPoints+"\nTotal points: "+totalPoints);
    }

    private void setupCoverVisibility() {
        for(int i = 0 ; i <= currentTier ; i ++){
            coverTiers[i].setVisibility(View.GONE);
        }
        coverTiers[currentTier+1].append("\n"+"You still need "+pointsNeeded+" points !");
    }

    private void setupTierClickable(){
        for (int i = 0 ; i < 5 ; i++) {
            if (coverTiers[i].getVisibility() == View.GONE) {
                setupAvatarClickable(i);
            }
        }
    }

    private void setupAvatarClickable(int i){
        for (int j = 0 ; j < 3 ; j++){
            String ID = "avatar_"+(i+1)+(j+1);
            int resID = getResources().getIdentifier(ID,"id",getPackageName());
            ImageView avatar = findViewById(resID);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Avatar on click action
                    Toast.makeText(StoreActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
