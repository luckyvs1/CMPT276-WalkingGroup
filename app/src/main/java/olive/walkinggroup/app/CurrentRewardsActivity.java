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

public class CurrentRewardsActivity extends AppCompatActivity {

    private Rewards rewards;
    private List<List<Integer>> iconIds;
    private ImageView previouslySelectedIcon;
    private int selectedIconId = 0;
    private Model instance;
    private User currentUser;
    private PointsHelper pointsHelper;
    private int currentTier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rewards);

        instance = Model.getInstance();
        currentUser = instance.getCurrentUser();
        pointsHelper = new PointsHelper();
        currentTier = pointsHelper.getCurrentTier();

        if (currentTier > -1) {
            rewards = new Rewards(this);
            iconIds = new ArrayList<>(rewards.getUnlockedIconsUpToTier(currentTier + 1));
            setupListIcons();
        }
        setupOKButton();
    }

    private void setupOKButton() {
        Button btn = findViewById(R.id.btnCurrentRewardsOK);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIconId != 0) {
                    SelectedRewards selectedRewards = new SelectedRewards("Title", selectedIconId);
                    currentUser.setRewards(selectedRewards);
                    updateUserOnServer();
                }
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

        Call<User> caller = instance.getProxy().updateUser(currentUser.getId(), currentUser);
        ProxyBuilder.callProxy(this, caller, returnedUsers -> onUpdateUser(returnedUsers));
    }

    private void onUpdateUser(User returnedUsers) {
    }

    private class IconsAdapter extends ArrayAdapter<List<Integer>> {
        private IconsAdapter() {
            super(CurrentRewardsActivity.this, R.layout.list_currentrewards_icon_item, iconIds);
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
                    ImageView selectedImageView = (ImageView)v;
                    if (previouslySelectedIcon != null) {
                        previouslySelectedIcon.clearColorFilter();
                    }
                    selectedImageView.setColorFilter(Color.CYAN, PorterDuff.Mode.LIGHTEN);
                    previouslySelectedIcon = selectedImageView;
                    selectedIconId = imageResourceId;

                }

            });

        }
    }
}
