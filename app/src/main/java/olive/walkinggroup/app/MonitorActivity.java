package olive.walkinggroup.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class MonitorActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        //New Code Starts
        verifyMonitoredUsersList();
        verifyIMonitorList();
        //New Code Ends

        setupEditMonitorsMeButton();
        setupIMonitorButton();
    }

    //New Code Starts
    public void verifyMonitoredUsersList(){
        Call<List<User>> caller = instance.getProxy().getMonitoredByUsers(user.getId());
        ProxyBuilder.callProxy(this,caller, listOfUsers -> setMonitoredByUsersList(listOfUsers));
    }

    private void setMonitoredByUsersList(List<User>list){
        user.setMonitoredByUsers(list);
        populateMonitorsMe();
    }
    //New Code Ends

    public void populateMonitorsMe() {
        //Create list
        String [] stringList = user.getMonitoredByUsersDescriptions();

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.monitorsMe);
        list.setAdapter(adapter);
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

                Intent intent = EditUserInformationActivity.makeIntent(MonitorActivity.this, email);
                //Toast.makeText(MonitorActivity.this, email, Toast.LENGTH_LONG).show();
                startActivity(intent);
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

    private void setupIMonitorButton() {
        Button btn = findViewById(R.id.editIMonitor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                editMonitorUserFragment dialog = new editMonitorUserFragment();
                dialog.show(manager, "EditDialog");
            }
        });
    }

    private void setupEditMonitorsMeButton(){
        Button btn = findViewById(R.id.editMonitorsMe);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                editMonitoredByUserFragment dialog = new editMonitoredByUserFragment();
                dialog.show(manager, "EditDialog");
            }
        });
    }
}