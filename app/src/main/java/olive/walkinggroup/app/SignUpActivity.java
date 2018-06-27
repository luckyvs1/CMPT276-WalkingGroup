package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.User;

public class SignUpActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        user = User.getInstance();

        setupConfirmSignupBtn();
        setupCancelBtn();
    }


    private void setupConfirmSignupBtn() {
        Button confirmBtn = (Button) findViewById(R.id.btnSubmitSignup);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check to see if the sign up is valid

                // Set the user details if the sign up is valid
                setUserDetails();

                // Go to dashboard activity if the sign up is valid
                // Android back button must go to the loginActivity not signUpActivity
                Intent intent = new Intent(SignUpActivity.this, DashBoardActivity.class);
                startActivity(intent);
                finish();

                // Else display a toast message
                Toast.makeText(SignUpActivity.this, "Failed signup", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserDetails() {
        String name = getUserInput(R.id.txtSetName);
        String email = getUserInput(R.id.txtSetEmail);
        String password = getUserInput(R.id.txtSetPassword);
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
