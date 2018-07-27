package olive.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.Rewards;

public class CurrentRewardsActivity extends AppCompatActivity {

    private Rewards rewards;
    private List<List<Integer>> iconIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_rewards);



        rewards = new Rewards(this);
        iconIds = new ArrayList<>(rewards.getUnlockedIconsUpToTier(5));
        setupListIcons();
    }

    private void setupListIcons() {

        IconsAdapter iconsAdapter = new IconsAdapter();
        ListView listView = findViewById(R.id.listViewCurrentRewardsIcons);
        listView.setAdapter(iconsAdapter);
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, CurrentRewardsActivity.class);
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

        }
    }
}
