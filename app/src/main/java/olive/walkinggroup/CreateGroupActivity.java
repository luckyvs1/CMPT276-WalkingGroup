package olive.walkinggroup;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DEST_LOCATION = 0;
    private static final int REQUEST_CODE_MEETING_PLACE = 1;

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
                startActivityForResult(intent, REQUEST_CODE_MEETING_PLACE);
            }
        });
    }

    private void setupDestLocationBtn() {
        Button btn = findViewById(R.id.btnDestinationLocation);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CreateGroupMapsActivity.makeLaunchIntent(CreateGroupActivity.this);
                startActivityForResult(intent, REQUEST_CODE_DEST_LOCATION);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_DEST_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    LatLng destLocation = CreateGroupMapsActivity.getLocationFromIntent(data);
                    TextView textView = findViewById(R.id.textViewSelectedDestLocation);
                    String txt = "Lat: " + destLocation.latitude + " Long: " + destLocation.longitude;
                    textView.setText(txt);

                }
                break;
            case REQUEST_CODE_MEETING_PLACE:
                if (resultCode == Activity.RESULT_OK) {
                    LatLng meetingPlace = CreateGroupMapsActivity.getLocationFromIntent(data);
                    TextView textView = findViewById(R.id.textViewSelectedMeetingPlace);
                    String txt = "Lat: " + meetingPlace.latitude + " Long: " + meetingPlace.longitude;
                    textView.setText(txt);
                }
                break;

        }
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
