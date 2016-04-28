package codecamp.cz.ultramegafabolousapp;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity for tracking position
 *
 * @author Michal Kučera [michal.kucera@ackee.cz]
 * @since {28/04/16}
 **/
public class PositionTrackingActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = PositionTrackingActivity.class.getName();
    private boolean mapReady = false;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private boolean locationUpdatesStarted;

    private Marker currentPositionMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initGoogleApiClient();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (googleApiClient.isConnected() && !locationUpdatesStarted) {
            startTrackingPosition();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTrackingPosition();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    private void initGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;
    }

    private void startTrackingPosition() {
        locationUpdatesStarted = true;

        LocationRequest locationRequest = new LocationRequest();
        /**
         * Nastaveni jak casto chceme aktualizace polohy...
         * Tyto aktualizace muzou byt i casteji, ale ne mene nez nastavime v metode setFastestInterval
         */
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        /**
         * Nastaveni presnosti
         * Moznosti:
         *      PRIORITY_BALANCED_POWER_ACCURACY
         *      PRIORITY_HIGH_ACCURACY
         *      PRIORITY_LOW_POWER
         *      PRIORITY_NO_POWER
         */
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void stopTrackingPosition() {
        locationUpdatesStarted = false;

        LocationServices
                .FusedLocationApi
                .removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location lastLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(googleApiClient);

        locationChanged(lastLocation.getLatitude(), lastLocation.getLongitude());

        if (!locationUpdatesStarted) {
            startTrackingPosition();
        }
    }

    private void locationChanged(double latitude, double longitude) {
        Log.d(TAG, "locationChanged() called with: " + "latitude = [" + latitude + "], longitude = [" + longitude + "]");
        if (mapReady) {
            LatLng latLng = new LatLng(latitude, longitude);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

            if (currentPositionMarker != null) {
                currentPositionMarker.remove();
            }

            currentPositionMarker = map.addMarker(
                    new MarkerOptions()
                            .position(latLng)
                            .title("My position")
            );
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        locationChanged(location.getLatitude(), location.getLongitude());
    }
}
