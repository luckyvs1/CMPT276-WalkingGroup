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
import olive.walkinggroup.dataobjects.UserListHelper;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class UserDetailsActivity extends AppCompatActivity {
    private Model instance = Model.getInstance();
    User user;
    private Boolean editPermission = false;
    private Boolean sameindividual = false;

    List <User> parentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        extractDataFromIntent();
        checkToDisplayEdit();
        populateView();
        verifyMonitoredUsersList();
        registerClickCallback();
        setupEditButton();
        setupLeaderboardButton();
    }

    private void setupLeaderboardButton() {
        Button btn = findViewById(R.id.btnLeaderboard);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = LeaderboardActivity.makeLaunchIntent(UserDetailsActivity.this);
                startActivity(intent);
            }
        });
    }

    private void checkToDisplayEdit() {
        if(user != null){
            editPermission = UserListHelper.isOnMonitorsUserList(instance.getCurrentUser(), user);
            sameindividual =  UserListHelper.sameUser(instance.getCurrentUser(), user);

            Button editBtn = (Button) findViewById(R.id.btnEditInformation);

            if(editPermission || sameindividual){
                editBtn.setVisibility(View.VISIBLE);
            } else {
                editBtn.setVisibility(View.GONE);
            }
        }
    }

    private void populateView(){

        String birthday = "";

        if(user.getBirthYear()!=null && user.getBirthMonth()!=null){
            birthday = ""+user.getBirthMonth().toString()+" / "+user.getBirthYear().toString();
        }

        setTextValueHelper(R.id.txtDisplayName, user.getName());
        setTextValueHelper(R.id.txtDisplayEmail, user.getEmail());
        setTextValueHelper(R.id.txtDisplayBirthday, birthday);
        setTextValueHelper(R.id.txtDisplayPhone, user.getHomePhone());
        setTextValueHelper(R.id.txtDisplayCell, user.getCellPhone());
        setTextValueHelper(R.id.txtDisplayGrade, user.getGrade());
        setTextValueHelper(R.id.txtDisplayTeacherName, user.getTeacherName());
        setTextValueHelper(R.id.txtDisplayEmergencyContact, user.getEmergencyContactInfo());
        setTextValueHelper(R.id.txtDisplayAddress, user.getAddress());

    }

    private void setTextValueHelper(int userInputResourceID, String value) {
        TextView txtValue = (TextView) findViewById(userInputResourceID);
        txtValue.setText(value);
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
                Toast.makeText(UserDetailsActivity.this,parentList.get(position).getName(),Toast.LENGTH_SHORT).show();
                Intent intent = UserDetailsActivity.makeIntent (UserDetailsActivity.this, parentList.get(position));
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

                    //Todo: Implement a more permanent solution after permission settings have been determined
                    editPermission = UserListHelper.isOnMonitorsUserList(instance.getCurrentUser(), user);
                    sameindividual =  UserListHelper.sameUser(instance.getCurrentUser(), user);

                    if(editPermission || sameindividual){
                        Toast.makeText(UserDetailsActivity.this, R.string.PermissionAvailableToEdit, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(UserDetailsActivity.this, R.string.PermissionUnavailableToEdit, Toast.LENGTH_LONG).show();
                    }

                } else  {
                    editPermission = true;
                    sameindividual = true;
                    email = instance.getCurrentUser().getEmail();
                }


                if(editPermission || sameindividual) {
                    Intent intent = EditUserInformationActivity.makeIntent(UserDetailsActivity.this, email);
                    startActivityForResult(intent, 1);
                }
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
                // No result code currently unused
            }
        }
    }//onActivityResult

    public static Intent makeIntent(Context context, User user) {
        Intent intent = new Intent (context, UserDetailsActivity.class);
        intent.putExtra("Children", user);
        return intent;
    }

    private void extractDataFromIntent(){
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("Children");
    }
}
