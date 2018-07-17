package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class EditUserInformationActivity extends AppCompatActivity {

    private Model instance;
    private User user;
    private User currentUser;
    private String editUserEmail;
    private User dummyUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_information);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();
        dummyUser = new User();

        extractDataFromIntent();

        if(editUserEmail != null){
            //Get the edit user details
            updateEditUser(editUserEmail);
        } else {
            getUserDetails();
        }

        // Setup activity buttons
        setupConfirmEditUserBtn();
        setupCancelBtn();


    }

    private void updateEditUser(String userEmail) {
        Call<User> caller = instance.getProxy().getUserByEmail(userEmail);
        ProxyBuilder.callProxy(EditUserInformationActivity.this, caller, returnedUser -> getUserByEmailResponse(returnedUser));
    }

    private void getUserByEmailResponse(User userFromEmail){
        user = userFromEmail;
        Toast.makeText(EditUserInformationActivity.this, user.toString(), Toast.LENGTH_LONG).show();
        getUserDetails();
    }

    private void setupCancelBtn() {
        Button cancelSignUp = (Button) findViewById(R.id.btnCancelEditUser);
        cancelSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setupConfirmEditUserBtn() {
        Button editUser = (Button) findViewById(R.id.btnSubmitEditUser);
        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserDetails();

                Call<User> caller;

                // Make server call
                if(editUserEmail != null){
                    caller = instance.getProxy().updateUser(user.getId(), dummyUser);
                } else {
                    caller = instance.getProxy().updateUser(currentUser.getId(), dummyUser);
                }
                ProxyBuilder.callProxy(EditUserInformationActivity.this, caller, updatedUser -> updateUserResponse(updatedUser));
            }
        });
    }

    private void updateUserResponse(User updatedUser) {

        if(editUserEmail != null) {
            user = updatedUser;
            returnUserObject(updatedUser);
        } else {
//            instance.setCurrentUser(updatedUser);
//            currentUser = updatedUser;
            checkUserChangedEmail(updatedUser);
        }
    }

    private void checkUserChangedEmail(User updatedUser) {
        if(currentUser.getEmail() != updatedUser.getEmail()){

            String userPassword = getFromSharedPreferences("userPassword");
            storeToSharedPreferences("userEmail", updatedUser.getEmail());
            //Login the user to update the token and set the updated user as the current user
            instance.setCurrentUser(updatedUser);
            currentUser = instance.getCurrentUser();
            currentUser.setPassword(userPassword);
            loginUserGetToken();

        }
    }

    // Get the resource from shared preferences
    private String getFromSharedPreferences(String keyName) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        String extractedResource = userPrefs.getString(keyName, null);
        return extractedResource;
    }

    // Store the resource to shared preferences
    private void storeToSharedPreferences(String keyName, String value) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString(keyName,value);
        editor.commit();
    }

    private void returnUserObject(User updatedUser) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updatedUser",updatedUser);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    // Set the user details from the edit user information activity
    private void setUserDetails() {
        //Todo: Currently update user does not work if public List<Group> getLeadsGroups() is not JsonIgnore
        //Todo: If public List<Group> getLeadsGroups() is JsonIgnore then rendering group list does not work
        try {
            String name = getUserInput(R.id.txtSetName);
            String email = getUserInput(R.id.txtSetEmail);
            String birthYear = getUserInput(R.id.txtSetBirthyear);
            String birthMonth = getUserInput(R.id.txtSetBirthmonth);
            String address = getUserInput(R.id.txtSetAddress);
            String teacherName = getUserInput(R.id.txtSetTeachername);
            String grade = getUserInput(R.id.txtSetGrade);
            String cellPhone = getUserInput(R.id.txtSetCellphone);
            String homePhone = getUserInput(R.id.txtSetHomephone);
            String emergencyContactInformation = getUserInput(R.id.txtSetEmergencyContact);
            Integer intBirthYear;
            Integer intBirthMonth;

            dummyUser.setName(name);
            dummyUser.setEmail(email);
            dummyUser.setAddress(address);
            dummyUser.setTeacherName(teacherName);
            dummyUser.setGrade(grade);
            dummyUser.setHomePhone(homePhone);
            dummyUser.setCellPhone(cellPhone);
            dummyUser.setEmergencyContactInfo(emergencyContactInformation);

            if(birthYear != null){
                intBirthYear = Integer.valueOf(birthYear);
                dummyUser.setBirthYear(intBirthYear);
            }

            if(birthMonth != null){
                intBirthMonth = Integer.valueOf(birthMonth);
                dummyUser.setBirthMonth(intBirthMonth);
            }

        } catch (NullPointerException e ) {
            Toast.makeText(EditUserInformationActivity.this, R.string.SignupErrorMessage, Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(EditUserInformationActivity.this, R.string.SignupErrorMessage, Toast.LENGTH_LONG).show();
        }

    }

    private String getUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);

        // https://stackoverflow.com/questions/11535011/edittext-field-is-required-before-moving-on-to-another-activity#11535058
        if(TextUtils.isEmpty(userText.getText()) && ((userInputResourceID == R.id.txtSetEmail) || (userInputResourceID == R.id.txtSetName))) {
            userText.setError(getString(R.string.invalidInput));
        } else if (TextUtils.isEmpty(userText.getText())) {
            return null;
        }
        return userText.getText().toString();
    }

    // Set the user details from the edit user information activity
    private void getUserDetails() {
        // Update the details of the user instance
        String name;
        String email;
        String birthYear;
        String birthMonth;
        String address;
        String teacherName;
        String grade;
        String cellPhone;
        String homePhone;
        String emergencyContactInformation;

        if(editUserEmail == null) {
            name = currentUser.getName();
            email = currentUser.getEmail();
            birthYear = String.valueOf(currentUser.getBirthYear());
            birthMonth = String.valueOf(currentUser.getBirthMonth());
            address = currentUser.getAddress();
            teacherName = currentUser.getTeacherName();
            grade = currentUser.getGrade();
            cellPhone = currentUser.getCellPhone();
            homePhone = currentUser.getHomePhone();
            emergencyContactInformation = currentUser.getEmergencyContactInfo();
        } else {
            name = user.getName();
            email = user.getEmail();
            birthYear = String.valueOf(user.getBirthYear());
            birthMonth = String.valueOf(user.getBirthMonth());
            address = user.getAddress();
            teacherName = user.getTeacherName();
            grade = user.getGrade();
            cellPhone = user.getCellPhone();
            homePhone = user.getHomePhone();
            emergencyContactInformation = user.getEmergencyContactInfo();
        }

        setUserInput(R.id.txtSetName, name);
        setUserInput(R.id.txtSetEmail, email);

        if(birthYear != "null") {
            setUserInput(R.id.txtSetBirthyear, birthYear);
        }

        if(birthMonth != "null") {
            setUserInput(R.id.txtSetBirthmonth, birthMonth);
        }

        setUserInput(R.id.txtSetAddress, address);
        setUserInput(R.id.txtSetTeachername, teacherName);
        setUserInput(R.id.txtSetGrade, grade);
        setUserInput(R.id.txtSetCellphone, cellPhone);
        setUserInput(R.id.txtSetHomephone, homePhone);
        setUserInput(R.id.txtSetEmergencyContact, emergencyContactInformation);
    }

    private void setUserInput(int userInputResourceID, String value) {
        EditText userText = (EditText) findViewById(userInputResourceID);
        userText.setText(value);
    }

    public static Intent makeIntent(Context context, String email) {
        Intent intent = new Intent (context, EditUserInformationActivity.class);
        intent.putExtra("Email", email);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        editUserEmail = intent.getStringExtra("Email");
    }

    // Login Functionality
    // -------------------------------------------------------------------------------------------
    private void loginUserGetToken() {

        Toast.makeText(EditUserInformationActivity.this, "called login user", Toast.LENGTH_LONG).show();
        // Register for token received:
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

        // Make call
        Call<Void> caller = instance.getProxy().login(currentUser);
        ProxyBuilder.callProxy(EditUserInformationActivity.this, caller, returnedNothing -> loginUserResponse(returnedNothing));

    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    // Response for call back from the login user
    private void loginUserResponse(Void returnedNothing) {
        // Navigate user to the next activity

        Toast.makeText(EditUserInformationActivity.this, "Login user response", Toast.LENGTH_LONG).show();
        returnUserObject(currentUser);

    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!

        instance.updateProxy(token);


        Toast.makeText(EditUserInformationActivity.this, "Received token", Toast.LENGTH_LONG).show();

        //Store token and email using shared preferences
        storeToSharedPreferences("Token", token);
    }

}
