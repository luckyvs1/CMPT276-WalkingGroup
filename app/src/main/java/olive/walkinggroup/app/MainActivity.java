package olive.walkinggroup.app;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private User user;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build the server proxy
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), null);

        user = User.getInstance();

        setupLoginBtn();
        setupSignupBtn();

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

                  // Make call
                Call<Void> caller = proxy.login(user);
                ProxyBuilder.callProxy(MainActivity.this, caller, returnedNothing -> response(returnedNothing));

            }
        });
    }

    // Handle the token by generating a new Proxy which is encoded with it.
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w("User logged in", "   --> NOW HAVE TOKEN: " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);
    }

    // Login actually completes by calling this; nothing to do as it was all done
    // when we got the token.
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");

        // Navigate user to the next activity
        goToDashBoardActivity();

        // Clear the fields
        clearInputFields();
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
        Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
        startActivity(intent);
    }

    // Put message up in toast and logcat
    private void notifyUserViaLogAndToast(String message) {
        Log.w("User logged in", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Get the user details from the login activity
    private void setUserDetails() {
        String email = getUserInput(R.id.txtGetEmail);
        String password = getUserInput(R.id.txtGetPassword);

        // Update the details of the user instance
        user.setPassword(password);
        user.setEmail(email);
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
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
