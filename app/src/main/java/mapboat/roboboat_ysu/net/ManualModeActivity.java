package mapboat.roboboat_ysu.net;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.zerokol.views.JoystickView;

import mapboat.roboboat_ysu.net.Utils.LogHelper;
import mapboat.roboboat_ysu.net.roboboat.R;

public class ManualModeActivity extends AppCompatActivity {

    private static final String TAG = ManualModeActivity.class.getSimpleName();

    public enum STEER_MODE {
        PWM_MODE(0), OFF_STEER(1), DEC_INC(2);

        private final int value;
        private STEER_MODE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    private static STEER_MODE steerMode = STEER_MODE.PWM_MODE;
    int temp_mode = steerMode.getValue();

    private TextView throttleStatus, steerStatus;
    private JoystickView throttleStick, steerStick;

    private double throttle, steer, leftMotor, rightMotor;

    private String sdirection, sgas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_mode);

        throttleStatus = (TextView) findViewById(R.id.throttle_status);
        steerStatus = (TextView) findViewById(R.id.steer_status);

        throttleStick = (JoystickView) findViewById(R.id.throttle_stick);
        steerStick = (JoystickView) findViewById(R.id.steer_stick);

        throttleStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {

                switch (direction) {
                    // Maju
                    case JoystickView.FRONT:
                    case JoystickView.FRONT_RIGHT:
                    case JoystickView.LEFT_FRONT:
                        sgas = "Front";
                        throttle = power;
                        break;

                    //Mundur?????
                    case JoystickView.RIGHT_BOTTOM:
                    case JoystickView.BOTTOM:
                    case JoystickView.BOTTOM_LEFT:
                        sgas = "Bottom";
                        throttle = 0;
                        break;

                    default:
                        sgas = "Idle";
                        throttle = 0;
                }

                calculatePower();

                sendCommand();
            }
        }, 50L);

        steerStick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {

            @Override
            public void onValueChanged(int angle, int power, int direction) {

                switch (direction) {
                    // Kiri
                    case JoystickView.BOTTOM_LEFT:
                    case JoystickView.LEFT:
                    case JoystickView.LEFT_FRONT:
                        sdirection = "Right";
                        steer = power;
                        break;

                    // Kanan
                    case JoystickView.FRONT_RIGHT:
                    case JoystickView.RIGHT:
                    case JoystickView.RIGHT_BOTTOM:
                        sdirection = "Left";
                        steer = power * -1;
                        break;

                    default:
                        sdirection = "Straight";
                        steer = 0;
                }

                calculatePower();

                sendCommand();
            }
        }, 50L);
    }

    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.manualmode_menu, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.steer_mode:
                temp_mode = steerMode.getValue();

                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                CharSequence items[] = new CharSequence[] {"PWM Mode", "Off Steer", "Decrease & Increase Speed"};
                adb.setSingleChoiceItems(items, temp_mode, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int n) {
                        temp_mode = n;
                    }

                });

                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        steerMode = STEER_MODE.values()[temp_mode];
                    }
                });
                adb.setNegativeButton("Cancel", null);
                adb.setTitle("Steer Mode");
                adb.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void calculatePower() {
        double totalPower = CommandData.max * (throttle/100);

        LogHelper.simpleLog(TAG, "totalPower: " + totalPower);

        if (Math.abs(steer) > 0) {
            double power = 0.8 * totalPower;
            double th = ((Math.abs(steer) / 100) * power);
            if (steer < 0) {
                if(steerMode == STEER_MODE.OFF_STEER) {
                    if(steer < -20) {
                        leftMotor = 0;
                    }
                } else {
                    leftMotor = power - th;
                }

                if (leftMotor < 0) {
                    leftMotor = 0;
                }

                if(steerMode == STEER_MODE.DEC_INC) {
                    rightMotor = power + th;
                } else {
                    rightMotor = power;
                }
            } else if (steer > 0) {
                if(steerMode == STEER_MODE.OFF_STEER) {
                    if(steer > 20) {
                        rightMotor = 0;
                    }
                } else {
                    rightMotor = power - th;
                }

                if (rightMotor < 0) {
                    rightMotor = 0;
                }

                if(steerMode == STEER_MODE.DEC_INC) {
                    leftMotor = power + th;
                } else {
                    leftMotor = power;
                }
            }
        } else {
            leftMotor = totalPower;
            rightMotor = totalPower;
        }
    }

    void sendCommand() {
        LogHelper.simpleLog(TAG, "L: " + leftMotor + " R: " + rightMotor);

        CommandData.idc++;
        CommandData.aut = false;
        CommandData.lmt = Math.round((float) leftMotor);
        CommandData.rmt = Math.round((float) rightMotor);

        throttleStatus.setText("Power:" + throttle + " Dir: " + sgas  +  " L:" + CommandData.lmt);
        steerStatus.setText("Power:" + steer + " Dir: " + sdirection  +  " R:" + CommandData.rmt);

        MapsActivity.sendCommand();
    }
}
