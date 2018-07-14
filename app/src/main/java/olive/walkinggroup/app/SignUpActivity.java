package olive.walkinggroup.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {

    private User currentUser;
    private Model instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        hideLoadingCircle();
        currentUser = new User();
        instance = Model.getInstance();

        // Setup activity buttons
        setupConfirmSignupBtn();
        setupCancelBtn();
    }

    private void setupConfirmSignupBtn() {
        Button confirmBtn = (Button) findViewById(R.id.btnSubmitUser);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the user details
                setUserDetails();

                // Make server call
                Call<User> caller = instance.getProxy().createUser(currentUser);
                ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> signupResponse(returnedUser));

            }
        });
    }

    // Set the user details from the signup activity
    private void setUserDetails() {

        try {
            String name = getUserInput(R.id.txtSetName);
            String email = getUserInput(R.id.txtSetEmail);
            String password = getUserInput(R.id.txtSetPassword);

            // Update the details of the user instance
            currentUser.setName(name);
            currentUser.setPassword(password);
            currentUser.setEmail(email);

        } catch (NullPointerException e ) {
            Toast.makeText(SignUpActivity.this, R.string.SignupErrorMessage, Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(SignUpActivity.this, R.string.SignupErrorMessage, Toast.LENGTH_LONG).show();
        }

    }

    private String getUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);

        // https://stackoverflow.com/questions/11535011/edittext-field-is-required-before-moving-on-to-another-activity#11535058
        if(TextUtils.isEmpty(userText.getText()) && ((userInputResourceID == R.id.txtSetPassword) || (userInputResourceID == R.id.txtSetEmail) || (userInputResourceID == R.id.txtSetName))) {
            userText.setError(getString(R.string.invalidInput));
        }
        return userText.getText().toString();
    }

    // Response from the call back function for signed up user
    private void signupResponse(User user) {
        instance.setCurrentUser(user);
        loginUserGetToken();
    }

    private void loginUserGetToken() {
        showLoadingCircle();
        // Register for token received:
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

        // Make call
        Call<Void> caller = instance.getProxy().login(currentUser);
        ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedNothing -> loginUserResponse(returnedNothing));
    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    // Response for call back from the login user
    private void loginUserResponse(Void returnedNothing) {
        // Navigate user to the next activity
        goToDashBoardActivity();
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        instance.updateProxy(token);
        Toast.makeText(SignUpActivity.this, "Logged in as:  " + currentUser.getName(), Toast.LENGTH_LONG).show();

        String userEmail = instance.getCurrentUser().getEmail();

        //Store token and email using shared preferences
        storeToSharedPreferences("Token", token);
        storeToSharedPreferences("UserEmail", userEmail);
    }

    // Store the resource to shared preferences
    private void storeToSharedPreferences(String keyName, String value) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString(keyName,value);
        editor.commit();
    }

    private void goToDashBoardActivity() {
        // Android back button must go to the loginActivity not signUpActivity
        Intent intent = new Intent(SignUpActivity.this, DashBoardActivity.class);
        startActivity(intent);
        finish();
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

    private void showLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.signup_loading);
        Button cancelButton =  (Button) findViewById(R.id.btnCancelSignUp);
        Button signupButton =  (Button) findViewById(R.id.btnSubmitUser);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            signupButton.setVisibility(View.INVISIBLE);
        }
    }

    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.signup_loading);
        Button cancelButton =  (Button) findViewById(R.id.btnCancelSignUp);
        Button signupButton =  (Button) findViewById(R.id.btnSubmitUser);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            signupButton.setVisibility(View.VISIBLE);
        }
    }

}


