package codecamp.cz.ultramegafabolousapp;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import codecamp.cz.ultramegafabolousapp.gcm.RegistrationIntentService;

/**
 * Main Activity
 *
 * @author Michal Kučera [michal.kucera@ackee.cz]
 * @since {27/04/16}
 **/
public class SampleActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = SampleActivity.class.getName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //  Pozice mitonu
    final LatLng miton = new LatLng(50.078184, 14.438299);

    private GoogleMap map;
    private Circle circle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);

            if (savedInstanceState == null) {
                addMapFragmentToLayout();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        // Pridani markeru na mapu
        map.addMarker(
                // Vytvoreni markeru
                // Dokumentace https://developers.google.com/android/reference/com/google/android/gms/maps/model/MarkerOptions.html
                new MarkerOptions()
                        .position(miton)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_here))
                        .anchor(0.5f, 0.5f)
//                        .infoWindowAnchor(-2, 2)
                        .title("Tady jsme")
        );

        /**
         * U camera update factory jsou 2 metody: newLatlng(pozice) a newLatLngZoom(pozice, zoomLevel)
         * Zoom levely:
         *      1: World
         *      5: Landmass/continent
         *      10: City
         *      15: Streets
         *      20: Buildings
         * Dale jsou taky metody na ruzne zoomy, scroll, presouvani na nejake cast uzemi (definovano levym hornim rohem a pravym dolnim)
         * Vice na https://developers.google.com/android/reference/com/google/android/gms/maps/CameraUpdateFactory#public-method-summary
         */
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(miton, 18));
        // Na zobrazeni vasi lokace je potreba permission android.permission.ACCESS_FINE_LOCATION nebo android.permission.ACCESS_COARSE_LOCATION
        // Moznost customizace vasi lokace pres map.setLocationSource()
        map.setMyLocationEnabled(true);

        /**
         * Pokud jsme Ui nenastavili pri vytvareni, mame moznost nastavit pres map.getUiSettings()
         */
        map.getUiSettings().setMyLocationButtonEnabled(true);

        // Tlacitko pro zmeny typu mapy
        setupChangeMapTypeButton();
        // Tlacitko pro animaci na pozici mitonu
        setupMitonLocationButton();

        /**
         * Po kliku na marker se zobrazi info window
         * Prepsat tento adapter pokud chceme custom view
         */
        map.setInfoWindowAdapter(new CustomInfoAdapter());
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                ObjectAnimator animator = ObjectAnimator.ofInt(circle, "fillColor", 0x3f000000, Color.BLACK);
                animator.setEvaluator(new ArgbEvaluator());
                animator.start();
            }
        });

        map.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                ObjectAnimator animator = ObjectAnimator.ofInt(circle, "fillColor", Color.BLACK, 0x3f000000);
                animator.setEvaluator(new ArgbEvaluator());
                animator.start();
            }
        });

        map.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                startActivity(new Intent(SampleActivity.this, PositionTrackingActivity.class));
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                LatLng southwest = new LatLng(latLng.latitude - 0.025, latLng.longitude - 0.0125);
                LatLng northEast = new LatLng(latLng.latitude + 0.025, latLng.longitude + 0.0125);

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                LatLngBounds latLngBounds = new LatLngBounds(southwest, northEast);
                builder.setLatLngBounds(latLngBounds);
                try {
                    startActivityForResult(builder.build(SampleActivity.this), SampleActivity.PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        drawCircle();
    }

    private void drawCircle() {
        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(miton)
                .strokeWidth(5) // In pixels, default is 10
                .strokeColor(Color.RED)
                .fillColor(0x3f000000) // Format ARGB
                .radius(30); // In meters

        // Get back the mutable Circle
        circle = map.addCircle(circleOptions);
    }

    private void setupChangeMapTypeButton() {
        Button changeButton = (Button) findViewById(R.id.btn_change_type);
        if (changeButton != null) {
            changeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * Nastaveni typu mapy, ruzne hodonoty jsou:
                     * MAP_TYPE_NONE: Zadna mapa (0)
                     * MAP_TYPE_NORMAL: Basic map with roads. (1)
                     * MAP_TYPE_SATELLITE: Satellite view with roads. (2)
                     * MAP_TYPE_TERRAIN: Terrain view without roads. (3)
                     * MAP_TYPE_HYBRID: Satellite maps with a transparent layer of major streets. (4)
                     */
                    map.setMapType((map.getMapType() + 1) % 5);
                }
            });
        }
    }

    private void setupMitonLocationButton() {
        Button ourLocation = (Button) findViewById(R.id.btn_our_location);
        if (ourLocation != null) {
            ourLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(miton, 16);
                    /**
                     *  Zde muzeme pouzit animateCamera() pro animaci na lokaci a
                     *  nebo moveCamera pro okamzity presun
                     */
                    map.animateCamera(update);
                }
            });
        }
    }

    private void addMapFragmentToLayout() {
        GoogleMapOptions options = new GoogleMapOptions();
        options.compassEnabled(false);
        options.rotateGesturesEnabled(false);
        options.zoomControlsEnabled(true);
        options.ambientEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {

        /**
         * Metoda pro uplne cele vlastni info view
         */
        @Override
        public View getInfoWindow(Marker marker) {
            // Vytvorime view
            View view = LayoutInflater.from(SampleActivity.this).inflate(R.layout.view_custom_info_window, null);
            // Zobrazime nazev
            TextView tv = (TextView) view.findViewById(R.id.txt_name);
            tv.setText(marker.getTitle());

            return view;
//            return null;
        }

        /**
         * Metoda pro naplni pouze obsahu jiz defaultniho info window
         * Vola se pokud getInfoWindow() vraci null
         */
        @Override
        public View getInfoContents(Marker marker) {
//            // Vytvorime view
//            View view = LayoutInflater.from(SampleActivity.this).inflate(R.layout.view_custom_info_window, null);
//            // Zobrazime nazev
//            TextView tv = (TextView) view.findViewById(R.id.txt_name);
//            tv.setText(marker.getTitle());
//
//            return view;
            return null;
        }
    }
}
