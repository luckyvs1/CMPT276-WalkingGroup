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
import olive.walkinggroup.dataobjects.User;

public class StoreActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();
    private int totalPoints;
    TextView [] coverTiers = new TextView [5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        for(int i = 0 ; i < 5 ; i++) {
            String ID = "cover_tier"+(i+1);
            int resID = getResources().getIdentifier(ID,"id",getPackageName());
            coverTiers [i] = findViewById(resID);
        }

        setupCurrentPoints();
        setupCoverVisibility();
        setupTierClickable();
    }

    private void setupCurrentPoints(){
        if(user.getTotalPointsEarned()==null)
            {totalPoints = 0;}
        else
            {totalPoints = user.getTotalPointsEarned();}
        TextView viewPoints = findViewById(R.id.shop_currentPoint);
        viewPoints.append(" "+totalPoints);
    }

    private void setupCoverVisibility() {
        for(int i = 0 ; i < 5 ; i ++){
            //TODO: Set up points needed to unlock tier
            int targetPoints = (i+1)*100;
            if(totalPoints >= targetPoints){
                coverTiers[i].setVisibility(View.GONE);
            }
        }
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
