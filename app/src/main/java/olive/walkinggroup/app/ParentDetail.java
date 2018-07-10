package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;

public class ParentDetail extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children_detail);

        extractDataFromIntent();
        setView();
        populateView();
    }

    private void setView() {
        name = findViewById(R.id.detailName);
        birthday = findViewById(R.id.detailBirthday);
        address = findViewById(R.id.detailAddress);
        phone = findViewById(R.id.detailPhone);
        cell = findViewById(R.id.detailCell);
        email = findViewById(R.id.detailEmail);
        grade = findViewById(R.id.detailGrade);
        grade.setVisibility(View.GONE);
        teacher = findViewById(R.id.detailTeacher);
        teacher.setVisibility(View.GONE);
        TextView contactTitle = findViewById(R.id.contactTitle);
        contactTitle.setVisibility(View.GONE);
        contactList = findViewById(R.id.monitorsMe);
        contactList.setVisibility(View.GONE);
    }

    private void populateView(){
        name.append(user.getName());
//        birthday.append(user.getBirthMonth()+user.getBirthYear().toString());
//        address.append(user.getAddress());
//        phone.append(user.getHomePhone());
//        cell.append(user.getCellPhone());
        email.append(user.getEmail());
    }

    public static Intent makeIntent(Context context, User user) {
        Intent intent = new Intent (context, ParentDetail.class);
        intent.putExtra("Parent", user);
        return intent;
    }

    private void extractDataFromIntent(){
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("Parent");
    }
}
