package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import olive.walkinggroup.R;

public class ViewPermissionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_permissions);

        populatePermissionRequestList();
    }

    private void populatePermissionRequestList() {
        ListView requestList = findViewById(R.id.viewPermissions_list);

    }
}
