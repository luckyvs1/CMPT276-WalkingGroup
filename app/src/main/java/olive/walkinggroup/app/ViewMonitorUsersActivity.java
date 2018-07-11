package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class ViewMonitorUsersActivity extends AppCompatActivity {

    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_monitor_users);

        // Monitor code from Rory's Activity
        verifyIMonitorList();
        
        setupViewProfileDetails();
        
        setupEditProfileDetails();
    }

    private void setupEditProfileDetails() {
    }

    private void setupViewProfileDetails() {
    }

    public void verifyIMonitorList(){
        Call<List<User>> caller_2 = instance.getProxy().getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(this,caller_2, listOfUsers -> setIMonitorList(listOfUsers));
    }

    private void setIMonitorList(List<User>list){
        user.setMonitorsUsers(list);
        populateIMonitor();
        registerClickCallBack();
    }

    private void registerClickCallBack() {
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tvClicked = (TextView) view;
                String description = tvClicked.getText().toString();
                String email = description.split("Email: ")[1];

                Intent intent = EditUserInformationActivity.makeIntent(ViewMonitorUsersActivity.this, email);
                startActivity(intent);

               // Toast.makeText(ViewMonitorUsersActivity.this, email, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void populateIMonitor() {
        //Create list
        String [] stringList = user.getMonitorsUsersDescriptions();

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setAdapter(adapter);
    }
}
