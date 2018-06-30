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
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {

    private User user;
    private long userId = 0;
    private String userEmail;
    private String userPassword;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), null);

        // Get the current user instance
        user = User.getInstance();

        // Setup activity buttons
        setupConfirmSignupBtn();
        setupCancelBtn();
    }


    private void setupConfirmSignupBtn() {
        Button confirmBtn = (Button) findViewById(R.id.btnSubmitSignup);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the user details
                setUserDetails();

                // Make server call
                 Call<User> caller = proxy.createUser(user);
                 ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> response(returnedUser));

            }
        });
    }

    private void launchDashboardActivity() {
        // Go to dashboard activity if the sign up is valid
        // Android back button must go to the loginActivity not signUpActivity
        Intent intent = new Intent(SignUpActivity.this, DashBoardActivity.class);
        startActivity(intent);
        finish();
    }


    // Response from the call back function
    private void response(User user) {
        notifyUserViaLogAndToast("Server replied with user: " + user.toString());
        userId = user.getId();
        userEmail = user.getEmail();

        loginUserGetToken();
    }

    private void loginUserGetToken() {
        // Register for token received:
        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));

        // Make call
        Call<Void> caller = proxy.login(user);
        ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedNothing -> response(returnedNothing));
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w("User logged in", "   --> NOW HAVE TOKEN: " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);

        //Store token using shared preferences
        storeTokenToSharedPreferences(token);
    }

    // Store the login token
    private void storeTokenToSharedPreferences(String token) {
        SharedPreferences userPrefs = getSharedPreferences("token", MODE_PRIVATE);
        SharedPreferences.Editor editor = userPrefs.edit();
        editor.putString("TokenValue",token);
        editor.commit();
    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");

        // Navigate user to the next activity
        launchDashboardActivity();
    }

    // Toast and log cat message on successful user creation
    private void notifyUserViaLogAndToast(String s) {
        Toast.makeText(SignUpActivity.this, s, Toast.LENGTH_SHORT).show();
        Log.i("User Created", s);
    }

    // Set the user details from the signup activity
    private void setUserDetails() {
        String name = getUserInput(R.id.txtSetName);
        String email = getUserInput(R.id.txtSetEmail);
        String password = getUserInput(R.id.txtSetPassword);

        // Update the details of the user instance
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);
    }

    private String getUserInput(int userInputResourceID) {
        EditText userText = (EditText) findViewById(userInputResourceID);
        return userText.getText().toString();
    }

    private void setupCancelBtn() {
        Button cancelSignUp = (Button) findViewById(R.id.btnCancelSignUp);
        cancelSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
}   }
