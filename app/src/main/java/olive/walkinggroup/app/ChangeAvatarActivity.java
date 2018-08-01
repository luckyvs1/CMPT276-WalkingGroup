package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Model;
import olive.walkinggroup.dataobjects.PointsHelper;
import olive.walkinggroup.dataobjects.Rewards;
import olive.walkinggroup.dataobjects.SelectedRewards;
import olive.walkinggroup.dataobjects.User;
import olive.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

public class ChangeAvatarActivity extends AppCompatActivity {

    private Rewards rewards;
    private List<List<Integer>> iconIds;
    private List<String> unlockedTitles;
    private ImageView previouslySelectedIcon;
    private int selectedIconId = 0;
    private Model instance;
    private User currentUser;
    private User dummyUser = new User();
    private PointsHelper pointsHelper;
    private int currentTier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatar);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();
        pointsHelper = new PointsHelper();
        currentTier = pointsHelper.getCurrentTier();

        TextView textView = findViewById(R.id.textViewNoRewards);
        textView.setVisibility(View.GONE);

        if (currentTier > -1) {
            rewards = new Rewards(this);
            iconIds = new ArrayList<>(rewards.getUnlockedIconsUpToTier(currentTier + 1));
            unlockedTitles = new ArrayList<>(rewards.getUnlockedTitlesUpToTier(currentTier + 1));
            setupListIcons();
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.current_rewards_no_rewards);
        }
        setupOKButton();
    }

    private void setupOKButton() {
        Button btn = findViewById(R.id.btnCurrentRewardsOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIconId != 0) {
                    SelectedRewards selectedRewards = new SelectedRewards(selectedIconId);
                    dummyUser.setName(currentUser.getName());
                    dummyUser.setEmail(currentUser.getEmail());
                    dummyUser.setCurrentPoints(currentUser.getCurrentPoints());
                    dummyUser.setTotalPointsEarned(currentUser.getTotalPointsEarned());
                    dummyUser.setBirthYear(currentUser.getBirthYear());
                    dummyUser.setBirthMonth(currentUser.getBirthMonth());
                    dummyUser.setAddress(currentUser.getAddress());
                    dummyUser.setCellPhone(currentUser.getCellPhone());
                    dummyUser.setHomePhone(currentUser.getHomePhone());
                    dummyUser.setTeacherName(currentUser.getTeacherName());
                    dummyUser.setGrade(currentUser.getGrade());
                    dummyUser.setEmergencyContactInfo(currentUser.getEmergencyContactInfo());
                    dummyUser.setRewards(selectedRewards);
                    updateUserOnServer();
                }
                finish();
            }
        });
    }

    private void setupListIcons() {

        CurrentRewardsAdapter currentRewardsAdapter = new CurrentRewardsAdapter();
        ListView listView = findViewById(R.id.listViewCurrentRewardsIcons);
        listView.setAdapter(currentRewardsAdapter);
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, ChangeAvatarActivity.class);
    }

    private void updateUserOnServer() {

        Call<User> caller = instance.getProxy().updateUser(currentUser.getId(), dummyUser);
        ProxyBuilder.callProxy(this, caller, returnedUser -> onUpdateUser(returnedUser));
    }

    private void onUpdateUser(User returnedUser) {
        instance.setCurrentUser(returnedUser);
    }

    private class CurrentRewardsAdapter extends ArrayAdapter<List<Integer>> {
        private CurrentRewardsAdapter() {
            super(ChangeAvatarActivity.this, R.layout.list_currentrewards_item, iconIds);
        }

        // Disable ListView recycling
        // https://stackoverflow.com/questions/6921462/listview-reusing-views-when-i-dont-want-it-to
        @Override
        public int getViewTypeCount() {
            return getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = ChangeAvatarActivity.this.getLayoutInflater().inflate(R.layout.list_currentrewards_item, parent, false);
            }


            ImageView colOneId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col1);
            ImageView colTwoId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col2);
            ImageView colThreeId = itemView.findViewById(R.id.imageViewCurrentRewardItem_Col3);

            setupTitleTextView(itemView, position);
            setupTierTextView(itemView, position);
            setupImageViewColumn(colOneId, position, 0);
            setupImageViewColumn(colTwoId, position, 1);
            setupImageViewColumn(colThreeId, position, 2);
            return itemView;
        }

        private void setupTierTextView(View itemView, int position) {
            TextView textView = itemView.findViewById(R.id.textViewTierTitle);
            String text = getString(R.string.title_currentrewards_tier) + " " +  (position + 1);
            textView.setText(text);
        }

        private void setupTitleTextView(View itemView, int position) {
            TextView textView = itemView.findViewById(R.id.textViewCurrentRewardTitle);
            textView.setText(unlockedTitles.get(position));
        }

        private void setupImageViewColumn(ImageView imageView, int position, int col) {



            int imageResourceId = iconIds.get(position).get(col);
            imageView.setImageResource(imageResourceId);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView selectedImageView = (ImageView)v;
                    if (previouslySelectedIcon != null) {
                        previouslySelectedIcon.clearColorFilter();
                    }
                    if (previouslySelectedIcon == selectedImageView && selectedIconId != 0) {
                        selectedImageView.clearColorFilter();
                        selectedIconId = 0;
                    } else {
                        selectedImageView.setColorFilter(Color.CYAN, PorterDuff.Mode.LIGHTEN);
                        previouslySelectedIcon = selectedImageView;
                        selectedIconId = imageResourceId;
                    }
                    Log.i("MyApp", selectedIconId + "");

                }

            });

        }
    }
}

