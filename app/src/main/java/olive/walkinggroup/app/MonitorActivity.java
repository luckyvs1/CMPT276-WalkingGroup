package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class MonitorActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    private User user = instance.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        populateMonitorsMe();
        populateIMonitor();

        createAddMonitorsMeButton();
        createRemoveMonitorsMeButton();
        createAddIMonitorButton();
        createRemoveIMonitorButton();

    }

    private void createRemoveIMonitorButton() {
        Button logout = (Button) findViewById(R.id.removeIMonitor);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userEmail = (EditText) findViewById(R.id.txtIMonitor);

            }
        });
    }

    private void createAddIMonitorButton() {
        Button logout = (Button) findViewById(R.id.addIMonitor);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userEmail = (EditText) findViewById(R.id.txtIMonitor);

            }
        });
    }

    private void createRemoveMonitorsMeButton() {
        Button logout = (Button) findViewById(R.id.removeMonitorsMe);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userEmail = (EditText) findViewById(R.id.txtMonitorsMe);

            }
        });
    }

    private void createAddMonitorsMeButton() {
        Button logout = (Button) findViewById(R.id.addMonitorsMe);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userEmail = (EditText) findViewById(R.id.txtMonitorsMe);

            }
        });
    }

    private void populateMonitorsMe() {
        //Create list
        List<User> userList = user.getMonitoredByUsers();
        String [] stringList = userList.toArray(new String[userList.size()]);

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.monitorsMe);
        list.setAdapter(adapter);

    }

    private void populateIMonitor() {
        //Create list
        List<User> userList = user.getMonitorsUsers();
        String [] stringList = userList.toArray(new String[userList.size()]);

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setAdapter(adapter);

    }


}
