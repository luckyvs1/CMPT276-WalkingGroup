package olive.walkinggroup.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private Model instance;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = Model.getInstance();
        currentUser = new User();

        checkUserTokenUpdateUser();
        setupLoginBtn();
        setupSignupBtn();

    }

    // By pass login screen if the user already has a token
    private void checkUserTokenUpdateUser() {
        String token = getFromSharedPreferences("Token");
        if(token != null){
            instance.updateProxy(token);
            updateCurrentUser();
        }
    }

    private void setupLoginBtn() {
        Button loginBtn = (Button) findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the user details
                setUserDetails();

                // Register for token received:
                ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

                //User newUser = new User();
                //newUser.setEmail("john@smith.com");
                //newUser.setPassword("johnsmith");

                // Login the user
                Call<Void> caller = instance.getProxy().login(currentUser);
                ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> loginUserResponse(returnedNothing));

            }
        });
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w("User logged in", "   --> NOW HAVE TOKEN: " + token);
        instance.updateProxy(token);

        String userEmail = currentUser.getEmail();

        //Store token and email using shared preferences
        storeToSharedPreferences("Token", token);
        storeToSharedPreferences("UserEmail", userEmail);
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

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    private void loginUserResponse(Void returnedNothing) {

        updateCurrentUser();

        // Clear the fields
        clearInputFields();
    }

    private void updateCurrentUser() {
        String userEmail = getFromSharedPreferences("UserEmail");
        Call<User> caller = instance.getProxy().getUserByEmail(userEmail);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> getUserByEmailResponse(returnedUser));
    }

    private void getUserByEmailResponse(User user){
        instance.setCurrentUser(user);
        goToDashBoardActivity();
    }

    private void clearInputFields() {
        setUserInput(R.id.txtGetPassword);
        setUserInput(R.id.txtGetEmail);
    }

    private void setUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);
        userText.setText("");
    }

    private void goToDashBoardActivity() {
        // Go to the dashboard activity
        Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
        startActivity(intent);
    }

    // Put message up in toast and logcat
    private void notifyUserViaLogAndToast(String message) {
        Log.w("User logged in", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Get the user details from the login activity to login user
    private void setUserDetails() {
        String email = getUserInput(R.id.txtGetEmail);
        String password = getUserInput(R.id.txtGetPassword);

        // Update the details of the user instance
        currentUser.setPassword(password);
        currentUser.setEmail(email);
    }

    private String getUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);
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
            }
        });
    }
}
