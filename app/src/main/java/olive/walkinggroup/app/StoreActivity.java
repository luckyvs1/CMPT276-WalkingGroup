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
        ImageView avatar = findViewById(R.id.avatar_11);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StoreActivity.this,"Clicked",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
