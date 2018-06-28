package olive.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import olive.walkinggroup.R;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;

public class DashBoardActivity extends AppCompatActivity {

    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        setupLogoutButton();
    }

    private void setupLogoutButton() {
        Button logout = (Button) findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = null;

                // Remove the token from the proxy
                proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);
                finish();
            }
        });
    }
}
