package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import olive.walkinggroup.R;

public class UserSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        setupSimpleButtonActivityChange(R.id.btnEditProfile, EditUserInformationActivity.class);
        setupSimpleButtonActivityChange(R.id.btnEditChildProfile, ViewMonitorUsersActivity.class);
    }

    private void setupSimpleButtonActivityChange(int buttonId, Class activityName) {
        Button btn = (Button) findViewById(buttonId);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSettingsActivity.this, activityName);
                startActivity(intent);
            }
        });
    }
}
