package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class ChildrenDetail extends AppCompatActivity {
    private Model instance = Model.getInstance();
    User user;

    TextView name;
    TextView birthday;
    TextView address;
    TextView phone;
    TextView cell;
    TextView email;
    TextView grade;
    TextView teacher;
    ListView emergencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children_detail);

        setView();
        populateView();
        verifyMonitoredUsersList();

    }

    private void setView() {
        name = findViewById(R.id.detailName);
        birthday = findViewById(R.id.detailBirthday);
        address = findViewById(R.id.detailAddress);
        phone = findViewById(R.id.detailPhone);
        cell = findViewById(R.id.detailCell);
        email = findViewById(R.id.detailEmail);
        grade = findViewById(R.id.detailGrade);
        teacher = findViewById(R.id.detailTeacher);
        emergencyList = findViewById(R.id.IMonitor);
    }

    private void populateView(){
        name.append(user.getName());
        birthday.append(user.getBirthMonth()+user.getBirthYear().toString());
        address.append(user.getAddress());
        phone.append(user.getHomePhone());
        cell.append(user.getCellPhone());
        email.append(user.getEmail());
        grade.append(user.getGrade());
        teacher.append(user.getTeacherName());
    }

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

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.IMonitor);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView textView = (TextView) viewClicked;
                //Intent intent = calculateActivity.makeIntent (MainActivity.this, currentPotList.getPot(position));
                //startActivityForResult(intent);
            }
        });
    }

}
