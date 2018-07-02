package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Group;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class CreateGroupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DEST_LOCATION = 0;
    private static final int REQUEST_CODE_MEETING_PLACE = 1;

    private Model instance;
    private User user;

    private String groupName = null;
    private LatLng startPoint = null;
    private LatLng endPoint = null;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        instance = Model.getInstance();
        user = instance.getCurrentUser();

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

                    double[] latArray = new double[]{startPoint.latitude, endPoint.latitude};
                    double[] lngArray = new double[]{startPoint.longitude, endPoint.longitude};

                    List<User> members = new ArrayList<>();
                    members.add(user);

                    Group group = new Group(groupName, " ", user, latArray, lngArray, members);
                    pushGroupObjectToServer(group);
                    // TODO: push updated User object (add group to leadsGroups) and Group object to server.
                    finish();
                }
            }
        });
    }

    private void pushGroupObjectToServer(Group group) {
        WGServerProxy proxy = instance.getProxy();

        Call<Group> caller = proxy.createGroup(group);
        ProxyBuilder.callProxy(CreateGroupActivity.this, caller, returnedGroup -> response(returnedGroup));
    }

    private void response(Group returnedGroup) {
        notifyUserViaLogAndToast("Server replied with group name: " + returnedGroup.getGroupName());

    }

    private void notifyUserViaLogAndToast(String message) {
        Toast.makeText(CreateGroupActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.i("App", message);
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
