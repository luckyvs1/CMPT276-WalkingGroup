package olive.walkinggroup.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.Dash;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class DashBoardActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private Model instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        setupLogoutButton();
        createToMonitorButton();
        createToFindGroupsButton();
        createToCreateGroupButton();

        instance = Model.getInstance();

        try {
            Toast.makeText(DashBoardActivity.this, "Welcome " + instance.getCurrentUser().getName() + " " + instance.getCurrentUser().getId(), Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            Log.d("DashboardActivity", e.getMessage());
        }
        createToViewMyGroupsButton();

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
                Intent intent = new Intent(DashBoardActivity.this, FindGroupsActivity.class);
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

    private void createToViewMyGroupsButton() {
        Button btn = findViewById(R.id.dashboard_viewMyGroupsBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Model model = Model.getInstance();
                User currentUser = model.getCurrentUser();

                Intent intent = new Intent(DashBoardActivity.this, ListGroupsActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });
    }

    private void setupLogoutButton() {
        Button logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nullValue = null;

                // Remove the token from the proxy
                instance.updateProxy(nullValue);

                // Remove the token and email from the shared preferences
                storeToSharedPreferences("Token", nullValue);
                storeToSharedPreferences("UserEmail", nullValue);

                // End the activity
                finish();
            }
        });
    }

    // Remove the login token and user email
    private void storeToSharedPreferences(String keyName, String value) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString(keyName,value);
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
