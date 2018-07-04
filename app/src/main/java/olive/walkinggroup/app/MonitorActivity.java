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

        setupEditMonitorsMeButton();
        setupIMonitorButton();
    }

    private void setupIMonitorButton() {
        Button btn = findViewById(R.id.editIMonitor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                editMonitorUserFragment dialog = new editMonitorUserFragment();
                dialog.show(manager, "EditDialog");
                populateIMonitor();
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
                populateMonitorsMe();
            }
        });
    }

    private void populateMonitorsMe() {
        //Create list
        String [] stringList = user.getMonitoredByUsersDescriptions();

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.monitorsMe);
        list.setAdapter(adapter);
    }

    private void populateIMonitor() {
        //Create list
        String [] stringList = user.getMonitorsUsersDescriptions();

        //Build Adaptor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item, stringList);

        //Configure the list view
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setAdapter(adapter);
    }
}