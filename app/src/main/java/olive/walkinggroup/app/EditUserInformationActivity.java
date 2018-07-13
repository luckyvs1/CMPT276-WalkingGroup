package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_information);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();

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
        Button cancelSignUp = (Button) findViewById(R.id.btnCancelSignUp);
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
                    caller = instance.getProxy().updateUser(user.getId(), user);
                } else {
                    caller = instance.getProxy().updateUser(currentUser.getId(), currentUser);
                }
                ProxyBuilder.callProxy(EditUserInformationActivity.this, caller, updatedUser -> updateUserResponse(updatedUser));
            }
        });
    }

    private void updateUserResponse(User updatedUser) {

        if(editUserEmail != null) {
            user = updatedUser;
        } else {
            instance.setCurrentUser(updatedUser);
            currentUser = updatedUser;
        }
        returnUserObject(updatedUser);
    }

    private void returnUserObject(User updatedUser) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updatedUser",updatedUser);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    // Set the user details from the edit user information activity
    private void setUserDetails() {
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

            if (birthYear != null && editUserEmail == null) {
                intBirthYear = Integer.valueOf(birthYear);
                currentUser.setBirthYear(intBirthYear);

            } else if (birthYear != null && editUserEmail != null) {
                intBirthYear = Integer.valueOf(birthYear);
                user.setBirthYear(intBirthYear);
            }

            if (birthMonth != null && editUserEmail == null) {
                intBirthMonth = Integer.valueOf(birthMonth);
                currentUser.setBirthMonth(intBirthMonth);
            } else if (birthMonth != null && editUserEmail != null) {
                intBirthMonth = Integer.valueOf(birthMonth);
                user.setBirthMonth(intBirthMonth);
            }

            if(editUserEmail == null) {
                // Update the details of the user instance
                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setAddress(address);
                currentUser.setTeacherName(teacherName);
                currentUser.setGrade(grade);
                currentUser.setCellPhone(cellPhone);
                currentUser.setHomePhone(homePhone);
                currentUser.setEmergencyContactInfo(emergencyContactInformation);
            } else {
                user.setName(name);
                user.setEmail(email);
                user.setCellPhone(cellPhone);
                user.setHomePhone(homePhone);
                user.setEmergencyContactInfo(emergencyContactInformation);
                user.setAddress(address);
                user.setGrade(grade);
                user.setTeacherName(teacherName);
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

}
