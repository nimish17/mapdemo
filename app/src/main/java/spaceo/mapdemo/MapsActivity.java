package spaceo.mapdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap map;
    private GPSTracker tracker;
    private static final int REQ_LOCATION_PERMISSION = 101;
    private static final int REQ_OPEN_SETTING = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        tracker = new GPSTracker(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setAllGesturesEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(false);
                map.getUiSettings().setZoomGesturesEnabled(true);
                map.setBuildingsEnabled(true);
                map.setIndoorEnabled(true);
            }
        });

        findViewById(R.id.btnMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }


    private void getLocation() {
        if (!tracker.canGetLocation) {
            if (Build.VERSION.SDK_INT >= 23) {
                String[] PermissionsLocation = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(PermissionsLocation, REQ_LOCATION_PERMISSION);
                } else
                    showLocationAlert();
            } else
                showLocationAlert();
        } else setMyLocation();
    }

    private void setMyLocation() {
        if (tracker.getLatitude() != 0 && tracker.getLongitude() != 0) {
            LatLng myLocation = new LatLng(tracker.getLatitude(), tracker.getLongitude());
            map.clear();
            map.addMarker(new MarkerOptions().position(myLocation).title("You are at Here!!"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f));
        }
    }

    private AlertDialog locationAlertDialog;
    private void showLocationAlert() {
        if (null == locationAlertDialog)
            locationAlertDialog = new AlertDialog.Builder(this, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? android.R.style.Theme_Material_Light_Dialog_Alert : -1)
                    .setCancelable(false)
                    .setMessage("This demo application would like to access your location")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQ_OPEN_SETTING);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        locationAlertDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OPEN_SETTING) {
            if (null != tracker)
                tracker.getLocation();
            checkLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (null != tracker)
                    tracker.getLocation();
                checkLocation();
            }
        }
    }

    private void checkLocation() {
        if (tracker.canGetLocation)
            setMyLocation();
        else
            showLocationAlert();
    }

}
