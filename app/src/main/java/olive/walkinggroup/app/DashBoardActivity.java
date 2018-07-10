package olive.walkinggroup.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.UploadGpsLocation;
import olive.walkinggroup.dataobjects.User;

public class DashBoardActivity extends AppCompatActivity {
    
    private Model instance;
    private User user;
    private UploadGpsLocation uploadGpsLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        instance = Model.getInstance();
        user = instance.getCurrentUser();
        uploadGpsLocation = new UploadGpsLocation(this);


        displayUserName();
        setupLogoutButton();
        setupSimpleButtonActivityChange(R.id.toMonitor, MonitorActivity.class);
        setupSimpleButtonActivityChange(R.id.toMap, FindGroupsActivity.class);
        setupSimpleButtonActivityChange(R.id.toCreateGroup, CreateGroupActivity.class);
        setupSimpleButtonActivityChange(R.id.dashboard_viewMyGroupsBtn, ListGroupsActivity.class);


        // TEST
        uploadGpsLocation.start();

    }



    private void displayUserName() {
        try {
            String message = "Welcome, " + instance.getCurrentUser().getName() + "!";
            TextView userName = (TextView) findViewById(R.id.txtUserName);
            userName.setText(message);
        } catch (NullPointerException e) {
            Log.d("DashboardActivity", e.getMessage());
        }
    }                   

    private void setupSimpleButtonActivityChange(int buttonId, Class activityName) {
        Button btn = (Button) findViewById(buttonId);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashBoardActivity.this, activityName);
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
