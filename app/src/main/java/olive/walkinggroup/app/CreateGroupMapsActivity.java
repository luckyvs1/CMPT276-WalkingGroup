package olive.walkinggroup.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import olive.walkinggroup.R;
import olive.walkinggroup.dataobjects.CurrentLocationHelper;

public class CreateGroupMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LATITUDE_KEY = "olive.walkinggroup.app.CreateGroupMapsActivity - latitude";
    private static final String LONGITUDE_KEY = "olive.walkinggroup.app.CreateGroupMapsActivity - longitude";

    private GoogleMap mMap;
    private CurrentLocationHelper currentLocationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_maps);
        initializeMap();

        setupMyLocationButton();
        currentLocationHelper = new CurrentLocationHelper(this);
        currentLocationHelper.getLocationPermission();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setupMyLocationButton() {
        RelativeLayout myLocationButton = findViewById(R.id.createGroup_myLocationBtn);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationHelper.setAnimate(true);
                currentLocationHelper.centerCameraAtCurrentLocationIfFound(mMap);
            }
        });
    }

    // Refresh activity after user grants permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        currentLocationHelper.setLocationPermissionGranted(false);

        switch (requestCode) {
            case CurrentLocationHelper.LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    currentLocationHelper.setLocationPermissionGranted(true);
                    recreate();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (currentLocationHelper.getLocationPermissionGranted()) {
            currentLocationHelper.setAnimate(false);
            currentLocationHelper.centerCameraAtCurrentLocationIfFound(mMap);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

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
