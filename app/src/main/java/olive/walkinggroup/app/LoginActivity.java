package olive.walkinggroup.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    // Instantiating variables
    private Model instance;
    private User user;
    private Boolean tokenAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideLoadingCircle();
        instance = Model.getInstance();
        user = new User();
        tokenAvailable = isTokenAvailable();

        if(tokenAvailable){
            Toast.makeText(LoginActivity.this, "Logging in, please wait!", Toast.LENGTH_LONG).show();
            String userEmail = getFromSharedPreferences("UserEmail");
            String userPassword = getFromSharedPreferences("UserPassword");
            user.setEmail(userEmail);
            user.setPassword(userPassword);
            updateCurrentUser(userEmail);
        }

        setupLoginBtn();
        setupSignupBtn();

    }

    // By pass login screen if the user already has a token
    private Boolean isTokenAvailable() {
        Boolean validToken = false;
        String token = getFromSharedPreferences("Token");
        if(token != null){
            showLoadingCircle();
            instance.updateProxy(token);
            validToken = true;
        }

        return validToken;
    }

    private void updateCurrentUser(String userEmail) {
        Call<User> caller = instance.getProxy().getUserByEmail(userEmail);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> getUserByEmailResponse(returnedUser));
    }

    private void getUserByEmailResponse(User userFromEmail) {

        instance.setCurrentUser(userFromEmail);

        updateCurrentUserByID(Long.valueOf(instance.getCurrentUser().getId()));

    }

    private void updateCurrentUserByID(Long aLong) {
        Call<User> caller = instance.getProxy().getUserById(aLong);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> getUserByID(returnedUser));
    }

    private void getUserByID(User returnedUser) {

        instance.setCurrentUser(returnedUser);
        // If the current user is not null
        if (instance.getCurrentUser().getId() != null) {
            Toast.makeText(LoginActivity.this, "Logged in as:  " + instance.getCurrentUser().getName(), Toast.LENGTH_LONG).show();
            hideLoadingCircle();
            goToDashBoardActivity();
        } else {
            hideLoadingCircle();
            Toast.makeText(LoginActivity.this, R.string.loginError, Toast.LENGTH_LONG).show();
        }

    }

    private void setupLoginBtn() {
        Button loginBtn = (Button) findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the user details
                setUserDetails();
                // Make call
                loginUserGetToken();
            }
        });
    }

    // Get the resource from shared preferences
    private String getFromSharedPreferences(String keyName) {
        SharedPreferences userPrefs = getSharedPreferences("userValues", MODE_PRIVATE);
        String extractedResource = userPrefs.getString(keyName, null);
        return extractedResource;
    }

    private void clearUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);
        userText.setText("");
    }

    // Put message up in toast and logcat
    private void notifyUserViaLogAndToast(String message) {
        Log.w("User logged in", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Get the user details from the login activity
    private void setUserDetails() {
        String userEmail = getUserInput(R.id.txtGetEmail);
        String userPassword = getUserInput(R.id.txtGetPassword);

        // Update the details of the user instance
        user.setPassword(userPassword);
        user.setEmail(userEmail);

        instance.setCurrentUser(user);

        storeToSharedPreferences("UserEmail", userEmail);
        storeToSharedPreferences("UserPassword", userPassword);
    }

    private String getUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);
        // https://stackoverflow.com/questions/11535011/edittext-field-is-required-before-moving-on-to-another-activity#11535058
        if(TextUtils.isEmpty(userText.getText())) {
            userText.setError(getString(R.string.invalidInput));
        }
        return userText.getText().toString();
    }

    private void setupSignupBtn() {
        Button signUpBtn = (Button) findViewById(R.id.btnSignUp);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Go to the sign up activity
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);

                //Clear user input
                clearUserInput(R.id.txtGetEmail);
                clearUserInput(R.id.txtGetPassword);

                hideLoadingCircle();
            }
        });
    }

    // Login Functionality
    // -------------------------------------------------------------------------------------------
    private void loginUserGetToken() {
        // Register for token received:
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

        // Make call
        Call<Void> caller = instance.getProxy().login(user);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> loginUserResponse(returnedNothing));
    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    // Response for call back from the login user
    private void loginUserResponse(Void returnedNothing) {
        // Navigate user to the next activity
        updateCurrentUser(instance.getCurrentUser().getEmail());
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!

        instance.updateProxy(token);

        //Store token and email using shared preferences
        storeToSharedPreferences("Token", token);
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
        Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
        startActivity(intent);

        //Clear user input
        clearUserInput(R.id.txtGetEmail);
        clearUserInput(R.id.txtGetPassword);
        hideLoadingCircle();
    }

    private void showLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.login_loading);
        Button loginButton =  (Button) findViewById(R.id.btnLogin);
        Button signupButton =  (Button) findViewById(R.id.btnSignUp);


        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            signupButton.setVisibility(View.INVISIBLE);
        }
    }

    private void hideLoadingCircle() {
        RelativeLayout loadingCircle = findViewById(R.id.login_loading);
        Button loginButton =  (Button) findViewById(R.id.btnLogin);
        Button signupButton =  (Button) findViewById(R.id.btnSignUp);

        if (loadingCircle != null) {
            loadingCircle.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            signupButton.setVisibility(View.VISIBLE);
        }
    }
}
