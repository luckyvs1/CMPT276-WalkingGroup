package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import olive.walkinggroup.R;

public class MonitorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        populateMonitorsMe();
        populateIMonitor();
    }

    private void populateMonitorsMe() {
        //Create list
        String[] userList = {"Rory \nckw27@sfu.ca","Lucky \nlvs1@sfu.ca","Josh \njha225@sfu.ca","Jeffery \njgyeung@sfu.ca; "};

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, userList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.monitorsMe);
        list.setAdapter(adapter);

    }

    private void populateIMonitor() {
        //Create list
        String[] userList = {"Rory","Lucky","Josh","Jeffery"};

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, userList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setAdapter(adapter);

    }


}
