package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class StoreActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        setCurrentPoints();
    }

    private void setCurrentPoints(){
        int currentPoints = user.getCurrentPoints();
        TextView viewPoints = findViewById(R.id.shop_currentPoint);
        viewPoints.append(" "+currentPoints);
    }
}
