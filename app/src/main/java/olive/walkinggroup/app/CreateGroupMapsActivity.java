package olive.walkinggroup.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import olive.walkinggroup.R;

public class CreateGroupMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LATITUDE_KEY = "olive.walkinggroup.app.CreateGroupMapsActivity - latitude";
    private static final String LONGITUDE_KEY = "olive.walkinggroup.app.CreateGroupMapsActivity - longitude";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("App","Lat: " + latLng.latitude + "Long: " + latLng.longitude);
                Intent intent = new Intent();
                intent.putExtra(LATITUDE_KEY, latLng.latitude);
                intent.putExtra(LONGITUDE_KEY, latLng.longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, CreateGroupMapsActivity.class);
    }

    public static LatLng getLocationFromIntent(Intent intent) {
        double latitude = intent.getDoubleExtra(LATITUDE_KEY, 0);
        double longitude = intent.getDoubleExtra(LONGITUDE_KEY, 0);
        return new LatLng(latitude, longitude);
    }
}
