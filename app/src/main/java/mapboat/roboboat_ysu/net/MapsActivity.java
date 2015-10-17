package mapboat.roboboat_ysu.net;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import mapboat.roboboat_ysu.net.Utils.LogHelper;
import mapboat.roboboat_ysu.net.roboboat.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    TextView tvBoatStatus;
    private FloatingActionButton fabLocateBoat;

    private GoogleMap mMap;
    private Marker boatMarker, targetMarker;

    private BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvBoatStatus = (TextView) findViewById(R.id.t_boat_status);
        fabLocateBoat = (FloatingActionButton) findViewById(R.id.b_locate_boat);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupBluetooth();
        setupDataParser();

//        fabLocateBoat.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
//                    bt.disconnect();
//                } else {
        if (bt.getServiceState() != BluetoothState.STATE_CONNECTED) {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            intent.putExtra("bluetooth_devices", "Bluetooth devices");
            intent.putExtra("no_devices_found", "No device");
            intent.putExtra("scanning", "Scanning");
            intent.putExtra("scan_for_devices", "Search");
            intent.putExtra("select_device", "Select");
            intent.putExtra("layout_list", R.layout.device_layout_list);
            intent.putExtra("layout_text", R.layout.device_layout_text);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
//                }
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        DataParser.clearData();
        bt.stopService();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng position = new LatLng(35.429368, 129.147201);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title("Boat Position")
                //.snippet("Latitude:" + 0 + " \nLongitude:" + 0)
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_black_48dp))
                .rotation(45);

        boatMarker = mMap.addMarker(markerOptions);

        tvBoatStatus.setText("Lat: " + boatMarker.getPosition().latitude + " Lng: " + boatMarker.getPosition().longitude + " Deg: " + boatMarker.getRotation());

        markerOptions =  new MarkerOptions()
                .position(position)
                .title("Target Position")
                .draggable(true)
                .visible(false); // Test

        targetMarker = mMap.addMarker(markerOptions);

        // Creating CameraUpdate object for position
        CameraUpdate updatePosition = CameraUpdateFactory.newLatLngZoom(position, 10);
        mMap.animateCamera(updatePosition);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker.getId().equals(targetMarker.getId())) {
                    marker.setSnippet("Lat: " + marker.getPosition().latitude + " Lng: " + marker.getPosition().longitude);
                    Toast.makeText(getBaseContext(), "Target move to: " + marker.getPosition().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                if (marker.getId().equals(targetMarker.getId())) {
//                    marker.setSnippet("Lat: " + marker.getPosition().latitude + " Lng: " + marker.getPosition().longitude);
//                }

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                targetMarker.setPosition(latLng);
                targetMarker.setSnippet("Lat: " + latLng.latitude + " Lng: " + latLng.longitude);

                if (!targetMarker.isVisible())
                    targetMarker.setVisible(true);

                Toast.makeText(getBaseContext(), "Target move to: " + latLng.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        fabLocateBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(boatMarker.getPosition())
                        .zoom(10)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    private void setupBluetooth() {
        bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            public void onServiceStateChanged(int state) {
                if (state == BluetoothState.STATE_CONNECTED) {
                    LogHelper.simpleLog(TAG, "State : Connected");
                    Toast.makeText(getApplicationContext()
                            , "State : Connected"
                            , Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothState.STATE_CONNECTING) {
                    LogHelper.simpleLog(TAG, "State : Connecting");
                    Toast.makeText(getApplicationContext()
                            , "State : Connecting"
                            , Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothState.STATE_LISTEN) {
                    LogHelper.simpleLog(TAG, "State : Listen");
                    Toast.makeText(getApplicationContext()
                            , "State : Listen"
                            , Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothState.STATE_NONE) {
                    LogHelper.simpleLog(TAG, "State : None");
                    Toast.makeText(getApplicationContext()
                            , "State : None"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                LogHelper.simpleLog(TAG, "Device Connected!!");
                Toast.makeText(getApplicationContext()
                        , "Device Connected!!"
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                LogHelper.simpleLog(TAG, "Device Disconnected!!");
                Toast.makeText(getApplicationContext()
                        , "Device Disconnected!!"
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                LogHelper.simpleLog(TAG, "Unable to Connected!!");
                Toast.makeText(getApplicationContext()
                        , "Unable to Connected!!"
                        , Toast.LENGTH_SHORT).show();
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                //DataParser.appendData(data);

                processData(message);

                LogHelper.simpleLog(TAG, "Length : " + data.length + " Message : " + message);
            }
        });

        bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
            public void onNewConnection(String name, String address) {
                LogHelper.simpleLog(TAG, "New Connection - " + name + " - " + address);
                Toast.makeText(getApplicationContext()
                        , "New Connection - " + name + " - " + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onAutoConnectionStarted() {
                LogHelper.simpleLog(TAG, "Auto menu_connection started");
                Toast.makeText(getApplicationContext()
                        , "Auto menu_connection started"
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDataParser() {
        DataParser.setOnReadableDataAvailableListener(new DataParser.OnReadableDataAvailableListener() {
            @Override
            public void onNewData(String data) {
                LogHelper.simpleLog(TAG, "Message : " + data);
            }
        });
    }

    private void processData(String data) {
        data = data.substring(0, data.length() - 1);
        LogHelper.simpleLog(TAG, data);
        BoatData.parseBoatData(data);
        
        updateBoatStatus();
    }
    
    private void updateBoatStatus() {
        boatMarker.setRotation(((float) BoatData.bearing));
        boatMarker.setPosition(new LatLng(BoatData.lat, BoatData.lng));

    }
}
