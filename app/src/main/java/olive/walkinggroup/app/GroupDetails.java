package olive.walkinggroup.app;

        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.TextView;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;

        import org.w3c.dom.Text;

        import olive.walkinggroup.R;
        import olive.walkinggroup.dataobjects.Group;

public class GroupDetails extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        // To be implemented after merge #4

        initializeMap();
        initializeText();
    }

    private void initializeText() {
        TextView groupName = findViewById(R.id.groupDetail_groupName);
        TextView groupLeader = findViewById(R.id.groupDetail_groupLeader);
        TextView groupComment = findViewById(R.id.groupDetail_groupComment);

        // To be implemented after merge #4
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.groupDetail_map);
        mapFragment.getMapAsync(GroupDetails.this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // To be implemented after merge #4

    }
}
