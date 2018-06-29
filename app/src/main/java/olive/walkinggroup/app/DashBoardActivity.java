package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import olive.walkinggroup.R;

public class DashBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        createToMonitorButton();
        createToMapButton();
        createToCreateGroupButton();
        createLogoutButton();
    }

    private void createToMonitorButton() {
        Button btn = (Button)findViewById(R.id.toMonitor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, MonitorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createToMapButton() {
        Button btn = (Button)findViewById(R.id.toMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, Map.class);
                startActivity(intent);
            }
        });
    }

    private void createToCreateGroupButton() {
        Button btn = (Button)findViewById(R.id.toCreateGroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createLogoutButton() {
        Button btn = (Button) findViewById(R.id.logoutBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}