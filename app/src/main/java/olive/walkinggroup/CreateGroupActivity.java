package olive.walkinggroup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreateGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        setupDestLocationBtn();
        setupMeetingPlaceBtn();

        setupOKBtn();
        setupCancelBtn();
    }

    private void setupMeetingPlaceBtn() {
        Button btn = findViewById(R.id.btnMeetingPlace);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CreateGroupMapsActivity.makeLaunchIntent(CreateGroupActivity.this);
                startActivity(intent);
            }
        });
    }

    private void setupDestLocationBtn() {
        Button btn = findViewById(R.id.btnDestinationLocation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CreateGroupMapsActivity.makeLaunchIntent(CreateGroupActivity.this);
                startActivity(intent);
            }
        });
    }


    private void setupCancelBtn() {
        Button btn = findViewById(R.id.btnCreateGroupCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setupOKBtn() {
        Button btn = findViewById(R.id.btnCreateGroupOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
