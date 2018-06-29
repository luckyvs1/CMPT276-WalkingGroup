package olive.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.User;

public class MainActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = User.getInstance();

        setupLoginBtn();
        setupSignupBtn();
    }

    private void setupLoginBtn() {
        Button loginBtn = (Button) findViewById(R.id.btnLogin);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if the user email and password are valid

                // Go to the dashboard activity
                Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                startActivity(intent);

                // Failed login attempt

                // Display toast
                Toast.makeText(MainActivity.this, "Failed Login", Toast.LENGTH_SHORT).show();
            }
        });
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
