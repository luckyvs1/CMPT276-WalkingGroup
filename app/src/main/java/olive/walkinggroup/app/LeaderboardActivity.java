package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class LeaderboardActivity extends AppCompatActivity {

    private Model instance;
    private List<User> leaderboardUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        instance = Model.getInstance();

        getAllUsersFromServer();
    }

    private void getAllUsersFromServer() {
        Call<List<User>> caller = instance.getProxy().getUsers();
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onGetAllUsers(returnedUsers));
    }

    private void onGetAllUsers(List<User> returnedUsers) {
        // TODO: filter top 100
        leaderboardUsers = returnedUsers;



    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, LeaderboardActivity.class);
    }
}
