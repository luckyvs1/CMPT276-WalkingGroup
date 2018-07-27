package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import olive.walkinggroup.R;

public class CurrentRewardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rewards);
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, CurrentRewardsActivity.class);
    }
}
