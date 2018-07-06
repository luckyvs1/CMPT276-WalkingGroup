package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

    private static final int REQUEST_CODE_MEETING_PLACE = 0;
    private static final int REQUEST_CODE_DEST_LOCATION = 1;

    private static final int TYPE_MEETING_PLACE = 2;
    private static final int TYPE_DEST_LOCATION = 3;

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
        String txt = getString(R.string.create_group_latCaption) + + location.latitude + " " +
                getString(R.string.create_group_lngCaption) + location.longitude;
        textView.setTextColor(Color.GRAY);
        textView.setText(txt);
    }

    private void setupOKBtn() {
        Button btn = findViewById(R.id.btnCreateGroupOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroupNameInput();

                if (validateInput()) {

                    // TODO: testUser to be replaced with currentUser object.
                    User testUser = new User();
                    //testUser.setId((long) 365);

                    testUser.setId(user.getId());

                    Group group = new Group(groupName,
                            groupName,
                            testUser,
                            new double[]{startPoint.latitude, endPoint.latitude},
                            new double[]{startPoint.longitude, endPoint.longitude}
                            , null);

                    Log.i("MyApp", "" + group.toString());
                    pushGroupObjectToServer(group);
                    finish();
                }
            }
        });
    }

    private boolean validateInput() {
        Boolean nameEntered = checkNameInput();
        Boolean destPlaceEntered = checkLocationInput(endPoint, TYPE_DEST_LOCATION);
        Boolean meetingPlaceEntered =checkLocationInput(startPoint, TYPE_MEETING_PLACE);

        return nameEntered && destPlaceEntered && meetingPlaceEntered;
    }

    private void pushGroupObjectToServer(Group group) {
        WGServerProxy proxy = instance.getProxy();

        Call<Group> caller = proxy.createGroup(group);
        ProxyBuilder.callProxy(CreateGroupActivity.this, caller, returnedGroup -> response(returnedGroup));
    }

    private void response(Group returnedGroup) {
        notifyUserViaLogAndToast(getString(R.string.create_group_serverReply) + returnedGroup.toString());

    }

    private void notifyUserViaLogAndToast(String message) {
        Toast.makeText(CreateGroupActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.i("App", message);
    }

    private boolean checkLocationInput(LatLng latLng, int locationType) {
        boolean locationExists = latLng != null;

        if (!locationExists) {
            if (locationType == TYPE_MEETING_PLACE) {
                setTextViewMessage(getString(R.string.create_group_chooseMeetingPlace), Color.RED, R.id.textViewSelectedMeetingPlace);
            } else if (locationType == TYPE_DEST_LOCATION) {
                setTextViewMessage(getString(R.string.create_group_destPlace), Color.RED, R.id.textViewSelectedDestLocation);
            }
        }
        return locationExists;
    }

    private void setTextViewMessage(String msg, int color, int textViewId) {
        TextView textView = findViewById(textViewId);
        textView.setTextColor(color);
        textView.setText(msg);
    }

    private void getGroupNameInput() {
        EditText editText = findViewById(R.id.editTextGroupName);
        groupName = editText.getText().toString();
    }

    private boolean checkNameInput() {
        Boolean hasNameInput = groupName.length() != 0;
        if (!hasNameInput) {
            displayEditTextError(getString(R.string.create_group_enterName), R.id.editTextGroupName);

        }
        return hasNameInput;
    }

    private void displayEditTextError(String msg, int editTextId) {
        EditText editText = findViewById(editTextId);
        editText.setError(msg);
    }


}
