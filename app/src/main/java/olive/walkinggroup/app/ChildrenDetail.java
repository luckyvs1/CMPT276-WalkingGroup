package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
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
    ListView contactList;
    List <User> parentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children_detail);

        extractDataFromIntent();
        setView();
        populateView();
        verifyMonitoredUsersList();
        registerClickCallback();
        setupEditButton();
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
        contactList = findViewById(R.id.IMonitor);
    }

    private void populateView(){
        name.append(user.getName());
        email.append(user.getEmail());

        if (user.getBirthYear()!=null && user.getBirthMonth()!=null)
        {birthday.append(""+user.getBirthMonth()+" / "+user.getBirthYear().toString());}
        if(user.getAddress()!=null)
        {address.append(user.getAddress());}
        if(user.getHomePhone()!=null)
        {phone.append(user.getHomePhone());}
        if(user.getCellPhone()!=null)
        {cell.append(user.getCellPhone());}
        if(user.getGrade()!=null)
        {grade.append(user.getGrade());}
        if(user.getTeacherName()!=null)
        {teacher.append(user.getTeacherName());}
    }

    public void verifyMonitoredUsersList(){
        Call<List<User>> caller = instance.getProxy().getMonitoredByUsers(user.getId());
        ProxyBuilder.callProxy(this,caller, listOfUsers -> setMonitoredByUsersList(listOfUsers));
    }

    private void setMonitoredByUsersList(List<User>list){
        user.setMonitoredByUsers(list);
        parentList = user.getMonitoredByUsers();
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
        ListView list = (ListView) findViewById(R.id.monitorsMe);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Toast.makeText(ChildrenDetail.this,parentList.get(position).getName(),Toast.LENGTH_SHORT).show();
                Intent intent = ParentDetail.makeIntent (ChildrenDetail.this, parentList.get(position));
                startActivity(intent);
            }
        });
    }

    private void setupEditButton() {
        Button editBtn = (Button) findViewById(R.id.btnEditInformation);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email;

                if (user != null) {
                    email = user.getEmail();
                } else  {
                    email = instance.getCurrentUser().getEmail();
                }
                Intent intent = EditUserInformationActivity.makeIntent(ChildrenDetail.this, email);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                user = (User) data.getSerializableExtra("updatedUser");

                populateView();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

    public static Intent makeIntent(Context context, User user) {
        Intent intent = new Intent (context, ChildrenDetail.class);
        intent.putExtra("Children", user);
        return intent;
    }

    private void extractDataFromIntent(){
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("Children");
    }
}
