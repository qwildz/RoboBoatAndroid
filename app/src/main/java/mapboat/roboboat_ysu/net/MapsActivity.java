package mapboat.roboboat_ysu.net;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import jaron.simpleserialization.SerializationData;
import jaron.simpleserialization.SerializationDataEventListener;
import jaron.simpleserialization.SerializationSerialConnection;
import mapboat.roboboat_ysu.net.Utils.LogHelper;
import mapboat.roboboat_ysu.net.roboboat.R;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    final Context context = this;

    TextView tvBoatLocation, tvBoatStatus, tvBoatCommand;

    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabLocateBoat;
    private FloatingActionButton fabStartBoat;
    private FloatingActionButton fabStopBoat;
    private FloatingActionButton fabManual;
    private FloatingActionButton fabSpeed;
    private FloatingActionButton fabTune;

    private GoogleMap mMap;
    private Marker boatMarker, targetMarker;

    public static BluetoothSPP bt;

    public static SerializationSerialConnection simpleSerialization = new SerializationSerialConnection();
    public static CommandData commandData = new CommandData();
    public static BoatData boatData = new BoatData();

    public static int countLastTryResendCommand = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvBoatLocation = (TextView) findViewById(R.id.t_boat_location);
        tvBoatStatus = (TextView) findViewById(R.id.t_boat_status);
        tvBoatCommand = (TextView) findViewById(R.id.t_boat_command);

        fabMenu = (FloatingActionMenu) findViewById(R.id.menu);
        fabLocateBoat = (FloatingActionButton) findViewById(R.id.fab_locate);
        fabStartBoat = (FloatingActionButton) findViewById(R.id.fab_start);
        fabStopBoat = (FloatingActionButton) findViewById(R.id.fab_stop);
        fabManual = (FloatingActionButton) findViewById(R.id.fab_manual);
        fabSpeed = (FloatingActionButton) findViewById(R.id.fab_speed);
        fabTune = (FloatingActionButton) findViewById(R.id.fab_tune);

        fabStartBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabMenu.close(true);
                fabStopBoat.show(true);

                CommandData.idc++;
                CommandData.run = true;

                sendCommand();
            }
        });

        fabStopBoat.hide(false);
        fabStopBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabStopBoat.hide(true);

                CommandData.idc++;
                CommandData.run = false;

                sendCommand();
            }
        });

        fabManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);

                CommandData.idc++;
                CommandData.aut = false;

                sendCommand();

                Intent iManual = new Intent(getApplicationContext(), ManualModeActivity.class);
                startActivity(iManual);
            }
        });

        fabSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);

                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.speed_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                alertDialogBuilder.setView(promptsView);

                final EditText etMaxSpeed = (EditText) promptsView.findViewById(R.id.max_speed);

                etMaxSpeed.setText(String.valueOf(CommandData.max));

                // set dialog message
                alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CommandData.max = Integer.parseInt(etMaxSpeed.getText().toString());
                                CommandData.idc++;
                                sendCommand();
                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        fabTune.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);

                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.pid_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                alertDialogBuilder.setView(promptsView);

                final EditText etKP = (EditText) promptsView.findViewById(R.id.kP);
                final EditText etKI = (EditText) promptsView.findViewById(R.id.kI);
                final EditText etKD = (EditText) promptsView.findViewById(R.id.kD);

                etKP.setText(String.valueOf(CommandData.kP));
                etKI.setText(String.valueOf(CommandData.kI));
                etKD.setText(String.valueOf(CommandData.kD));

                // set dialog message
                alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                CommandData.kP = Integer.parseInt(etKP.getText().toString());
                                CommandData.kI = Integer.parseInt(etKI.getText().toString());
                                CommandData.kD = Integer.parseInt(etKD.getText().toString());
                                CommandData.idc++;
                                sendCommand();
                            }
                        }).setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setupBluetooth();
        setupDataParser();

        if (bt.getServiceState() != BluetoothState.STATE_CONNECTED && bt.isBluetoothEnabled()) {
            connectBluetooth();
        }

        boatData.setListener(new SerializationDataEventListener() {
            @Override
            public void dataUpdate(SerializationData data) {
                if (CommandData.aut) {
                    if (BoatData.last_id_command != CommandData.idc) {
                        LogHelper.simpleLog(TAG, "Command not received");
                        LogHelper.simpleLog(TAG, "Try resend command");
                        countLastTryResendCommand--;
                        if (countLastTryResendCommand == 0) {
                            countLastTryResendCommand = 2;
                            sendCommand();
                        }
                    }
                }

                updateBoatStatus();
            }
        });
        simpleSerialization.addDeserializableData(boatData);
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
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onResume() {
        super.onResume();

        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            CommandData.idc++;
            CommandData.aut = true;

            sendCommand();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

                if (bt.getServiceState() != BluetoothState.STATE_CONNECTED) {
                    connectBluetooth();
                }
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectBluetooth() {
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

        markerOptions = new MarkerOptions()
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
                    CommandData.tlat = ((float) marker.getPosition().latitude);
                    CommandData.tlng = ((float) marker.getPosition().longitude);
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

                CommandData.tlat = ((float) latLng.latitude);
                CommandData.tlng = ((float) latLng.longitude);

                Toast.makeText(getBaseContext(), "Target move to: " + latLng.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        fabLocateBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(boatMarker.getPosition())
                        .zoom(18)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                fabMenu.close(true);
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

//                for (int i = 0; i < data.length; i++) {
//                    LogHelper.simpleLog(TAG, "" + ((int) data[i]));
//                }
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

        if (CommandData.aut) {
            if (BoatData.last_id_command != CommandData.idc) {
                LogHelper.simpleLog(TAG, "Command not received");
                LogHelper.simpleLog(TAG, "Try resend command");
                countLastTryResendCommand--;
                if (countLastTryResendCommand == 0) {
                    countLastTryResendCommand = 2;
                    sendCommand();
                }
            }
        }

        updateBoatStatus();
    }

    private void processData(byte[] data) {
        LogHelper.simpleLog(TAG, data.toString());
        simpleSerialization.read(data);
    }

    private void updateBoatStatus() {
        boatMarker.setRotation(((float) BoatData.bearing));
        boatMarker.setPosition(new LatLng(BoatData.lat, BoatData.lng));
        tvBoatLocation.setText("Lat: " + boatMarker.getPosition().latitude + " Lng: " + boatMarker.getPosition().longitude + " Deg: " + boatMarker.getRotation());
        tvBoatStatus.setText("L: " + BoatData.left_motor + " R: " + BoatData.right_motor + " GPSData: " + BoatData.gps + " Run: " + BoatData.run + " Completed : " + BoatData.completed);
        tvBoatCommand.setText("Run: " + CommandData.run + " TLat: " + CommandData.tlat + " TLng: " + CommandData.tlng + " P: " + CommandData.kP + " I: " + CommandData.kI + " D: " + CommandData.kD);
    }

    public static void sendCommand() {
        //String json = CommandData.parseBoatData();
        //LogHelper.simpleLog(TAG, "Length : " + json.length() + " Message : " + json);
        //bt.send(json, false);

        byte[] data = simpleSerialization.write(commandData);
        LogHelper.simpleLog(TAG, "Length : " + data.length + " Message : " + data);
        bt.send(data, false);
    }
}
