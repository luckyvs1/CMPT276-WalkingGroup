package olive.walkinggroup.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;

public class DashBoardActivity extends AppCompatActivity {

    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        createToMonitorButton();
        createToFindGroupsButton();
        createToCreateGroupButton();
        createToViewMyGroupsButton();
        setupLogoutButton();
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

    private void createToFindGroupsButton() {
        Button btn = (Button)findViewById(R.id.toMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, JoinGroupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createToCreateGroupButton() {
        Button btn = (Button)findViewById(R.id.toCreateGroup);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model model = Model.getInstance();
                User currentUser = model.getCurrentUser();

                Intent intent = new Intent(DashBoardActivity.this, CreateGroupActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
    }

    private void createToViewMyGroupsButton() {
        Button btn = findViewById(R.id.dashboard_viewMyGroupsBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, ListGroupsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupLogoutButton() {
        Button logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = null;

                // Remove the token from the proxy
                proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);

                // Remove the token from the shared preferences
                updateStoredTokenToSharedPreferences(token);

                // End the activity
                finish();
            }
        });
    }

    // Remove the login token
    private void updateStoredTokenToSharedPreferences(String token) {
        SharedPreferences userPrefs = getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString("TokenValue",token);
        editor.commit();
    }

    //https://stackoverflow.com/questions/2354336/android-pressing-back-button-should-exit-the-app
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
