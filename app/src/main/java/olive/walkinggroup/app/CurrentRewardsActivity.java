package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.Rewards;
import olive.walkinggroup.dataobjects.SelectedRewards;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class CurrentRewardsActivity extends AppCompatActivity {

    private Rewards rewards;
    private List<List<Integer>> iconIds;
    private ImageView previouslySelectedIcon;
    private int selectedIconId;
    private Model instance;
    private User currentUser;
    private User dummyUser = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rewards);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();

        rewards = new Rewards(this);
        iconIds = new ArrayList<>(rewards.getUnlockedIconsUpToTier(5));
        setupListIcons();
        setupOKButton();
    }

    private void setupOKButton() {
        Button btn = findViewById(R.id.btnCurrentRewardsOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedRewards selectedRewards = new SelectedRewards("Title", selectedIconId);
                currentUser.setRewards(selectedRewards);

                // Update rewards to dummy user
                dummyUser.setEmail(currentUser.getEmail());
                dummyUser.setName(currentUser.getName());
                dummyUser.setRewards(selectedRewards);
                dummyUser.setId(currentUser.getId());
                updateUserOnServer();
                finish();
            }
        });
    }

    private void setupListIcons() {

        IconsAdapter iconsAdapter = new IconsAdapter();
        ListView listView = findViewById(R.id.listViewCurrentRewardsIcons);
        listView.setAdapter(iconsAdapter);
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, CurrentRewardsActivity.class);
    }

    private void updateUserOnServer() {

        //Call<User> caller = instance.getProxy().updateUser(currentUser.getId(), currentUser);
        Call<User> caller = instance.getProxy().updateUser(currentUser.getId(), dummyUser);
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onUpdateUser(returnedUsers));
    }

    private void onUpdateUser(User returnedUsers) {
        instance.setCurrentUser(returnedUsers);
    }

    private class IconsAdapter extends ArrayAdapter<List<Integer>> {
        private IconsAdapter() {
            super(CurrentRewardsActivity.this, R.layout.list_currentrewards_icon_item, iconIds);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = CurrentRewardsActivity.this.getLayoutInflater().inflate(R.layout.list_currentrewards_icon_item, parent, false);
            }

            ImageView colOneId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col1);
            ImageView colTwoId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col2);
            ImageView colThreeId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col3);

            setupImageViewColumn(colOneId, position, 0);
            setupImageViewColumn(colTwoId, position, 1);
            setupImageViewColumn(colThreeId, position, 2);
            return itemView;
        }

        private void setupImageViewColumn(ImageView imageView, int position, int col) {
            int imageResourceId = iconIds.get(position).get(col);
            imageView.setImageResource(imageResourceId);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (previouslySelectedIcon != null) {
                        previouslySelectedIcon.clearColorFilter();
                    }
                    imageView.setColorFilter(Color.CYAN, PorterDuff.Mode.LIGHTEN);
                    previouslySelectedIcon = imageView;
                    selectedIconId = imageResourceId;

                }

            });

        }
    }
}
