package olive.walkinggroup.app;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class StoreActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();
    private int totalPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        setupCurrentPoints();
        setupScrollView();
    }

    private void setupCurrentPoints(){
        if(user.getTotalPointsEarned()==null)
            {totalPoints = 0;}
        else
            {totalPoints = user.getTotalPointsEarned();}
        TextView viewPoints = findViewById(R.id.shop_currentPoint);
        viewPoints.append(" "+totalPoints);
    }

    private void setupScrollView(){
        for(int i = 0 ; i < 5 ; i++){

        }
    }
}
