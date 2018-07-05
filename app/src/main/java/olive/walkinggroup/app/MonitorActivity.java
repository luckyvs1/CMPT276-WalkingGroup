package olive.walkinggroup.app;

import android.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class MonitorActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = this.getDummy();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        //New Code Starts
        verifyMonitoredUsersList();

        Call<List<User>> caller_2 = instance.getProxy().getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(this,caller_2, listOfUsers -> setIMonitorList(listOfUsers));
        //New Code Ends

        setupEditMonitorsMeButton();
        setupIMonitorButton();
    }

    public static User getDummy() {
        User dummy = new User();
        dummy.setName("Bob");
        dummy.setId((long) 87);
        dummy.setEmail("bob@bobby.com");

        return dummy;
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

    private void setIMonitorList(List<User>list){
        user.setMonitorsUsers(list);
        populateIMonitor();
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