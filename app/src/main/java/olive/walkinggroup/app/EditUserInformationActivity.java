package olive.walkinggroup.app;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_information);

        instance = Model.getInstance();

        getUserDetails();


        // Setup activity buttons
        setupConfirmEditUserBtn();
        setupCancelBtn();


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

                // Make server call
                Call<User> caller = instance.getProxy().updateUser(instance.getCurrentUser().getId(), instance.getCurrentUser());
                ProxyBuilder.callProxy(EditUserInformationActivity.this, caller, updatedUser -> updateUserResponse(updatedUser));
            }
        });
    }

    private void updateUserResponse(User updatedUser) {
        instance.setCurrentUser(updatedUser);

        Toast.makeText(EditUserInformationActivity.this, R.string.UpdateUserInformatonSuccessfully, Toast.LENGTH_LONG).show();
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

            if (birthYear != null) {
                intBirthYear = Integer.valueOf(birthYear);
            } else {
                intBirthYear = 0;
            }

            if (birthMonth != null) {
                intBirthMonth = Integer.valueOf(birthMonth);
            } else {
                intBirthMonth = 0;
            }

            // Update the details of the user instance
            instance.getCurrentUser().setName(name);
            instance.getCurrentUser().setEmail(email);
            instance.getCurrentUser().setAddress(address);
            instance.getCurrentUser().setTeacherName(teacherName);
            instance.getCurrentUser().setGrade(grade);
            instance.getCurrentUser().setCellPhone(cellPhone);
            instance.getCurrentUser().setHomePhone(homePhone);
            instance.getCurrentUser().setEmergencyContactInfo(emergencyContactInformation);
            instance.getCurrentUser().setBirthYear(intBirthYear);
            instance.getCurrentUser().setBirthMonth(intBirthMonth);

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
        String name = instance.getCurrentUser().getName();
        String email = instance.getCurrentUser().getEmail();
        String birthYear = String.valueOf(instance.getCurrentUser().getBirthYear());
        String birthMonth = String.valueOf(instance.getCurrentUser().getBirthMonth());
        String address = instance.getCurrentUser().getAddress();
        String teacherName = instance.getCurrentUser().getTeacherName();
        String grade = instance.getCurrentUser().getGrade();
        String cellPhone = instance.getCurrentUser().getCellPhone();
        String homePhone = instance.getCurrentUser().getHomePhone();
        String emergencyContactInformation = instance.getCurrentUser().getEmergencyContactInfo();

        setUserInput(R.id.txtSetName, name);
        setUserInput(R.id.txtSetEmail, email);
        setUserInput(R.id.txtSetBirthyear, birthYear);
        setUserInput(R.id.txtSetBirthmonth, birthMonth);
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


}
