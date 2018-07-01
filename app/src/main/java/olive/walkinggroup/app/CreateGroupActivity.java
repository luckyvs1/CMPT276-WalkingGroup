package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DEST_LOCATION = 0;
    private static final int REQUEST_CODE_MEETING_PLACE = 1;

    private String groupName = null;
    private LatLng startPoint = null;
    private LatLng endPoint = null;

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
                    displayLatLngOnTextView(destLocation, R.id.textViewSelectedDestLocation);
                    endPoint = destLocation;


                }
                break;
            case REQUEST_CODE_MEETING_PLACE:
                if (resultCode == Activity.RESULT_OK) {
                    LatLng meetingPlace = CreateGroupMapsActivity.getLocationFromIntent(data);
                    displayLatLngOnTextView(meetingPlace, R.id.textViewSelectedMeetingPlace);
                    startPoint = meetingPlace;
                }
                break;

        }
    }

    private void displayLatLngOnTextView(LatLng location, int textViewId) {
        TextView textView = findViewById(textViewId);
        String txt = "Lat: " + location.latitude + " Long: " + location.longitude;
        textView.setText(txt);
    }

    private void setupOKBtn() {
        Button btn = findViewById(R.id.btnCreateGroupOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroupNameInput();
                if (    checkNameInput() &&
                        checkLocationInput(startPoint) &&
                        checkLocationInput(endPoint)
                        ) {
                    // Group group = new Group(groupName, " ", " ", startPoint, endPoint);
                    // TODO: push updated User object (add group to leadsGroups) and Group object to server.
                    finish();
                }
            }
        });
    }

    private boolean checkLocationInput(LatLng latLng) {
        return latLng != null;
    }

    private void getGroupNameInput() {
        EditText editText = findViewById(R.id.editTextGroupName);
        groupName = editText.getText().toString();
    }

    private boolean checkNameInput() {
        return groupName.length() != 0;
    }


}
