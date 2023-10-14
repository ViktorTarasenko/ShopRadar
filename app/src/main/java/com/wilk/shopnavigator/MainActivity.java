package com.wilk.shopnavigator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    LocationChangedReceiver locationChangedReceiver;
    private long lastTimestamp = 0;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView distanceTextView;
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;
    Intent inertialLocIntent;
    private double velocityX = 0.0d;
    private double velocityY = 0.0d;
    private double velocityZ = 0.0d;
    private double distanceX = 0.0d;
    private double distanceY = 0.0d;
    private double distanceZ = 0.0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        distanceTextView = findViewById(R.id.distanceTextView);
        inertialLocIntent =
                new Intent(MainActivity.this, InertialLocateService.class);
        ClientPos ClientPos = new ClientPos(NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude());
        inertialLocIntent.putExtra("init_pos", ClientPos);
        locationChangedReceiver = new LocationChangedReceiver();
        IntentFilter dataIntentFilter = new IntentFilter();
        dataIntentFilter.addAction("locate");
        registerReceiver(locationChangedReceiver, dataIntentFilter);
        startService(inertialLocIntent);

        // Initialize the sensor manager and accelerometer sensor
        /*sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (accelerometer != null) {
            velocityX = 0.0d;
            velocityY = 0.0d;
            velocityZ = 0.0d;
            distanceX = 0.0d;
            distanceY = 0.0d;
            distanceZ = 0.0d;
            lastTimestamp = 0;
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }*/
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        /*if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Calculate acceleration magnitude
            long currentTime = System.currentTimeMillis();
            if (lastTimestamp != 0) {
                double accelerationXNew = sensorEvent.values[0];
                double accelerationYNew = sensorEvent.values[1];
                double accelerationZNew = sensorEvent.values[2];
                double timeDifference = currentTime - lastTimestamp;
                // Calculate velocity using the kinematic equation: v = u + at
                // Initial velocity (u) is assumed to be 0
                velocityX += (((accelerationX + accelerationXNew) / 2) * timeDifference) / 1000.0d; // Convert timeDifference to seconds
                velocityY += (((accelerationY + accelerationYNew) / 2) * timeDifference) / 1000.0d; // Convert timeDifference to seconds
                velocityZ += (((accelerationZ + accelerationZNew) / 2) * timeDifference) / 1000.0d; // Convert timeDifference to seconds

                // Calculate distance using the kinematic equation: s = ut + (1/2)at^2
                distanceX += (velocityX * timeDifference / 1000.0d) + (0.5 * (accelerationX + accelerationXNew) / 2 * Math.pow((double) timeDifference / 1000.0d, 2));
                distanceY += (velocityY * timeDifference / 1000.0d) + (0.5 * accelerationY * Math.pow((double) timeDifference / 1000.0d, 2));
                distanceZ += (velocityZ * timeDifference / 1000.0d) + (0.5 * accelerationZ * Math.pow((double) timeDifference / 1000.0d, 2));

                // Update the distanceTextView
                distanceTextView.setText(String.format(Locale.UK, "Distance: is %.2f %.2f %.2f meters", velocityX, velocityY, velocityZ));
                Log.d("acceleration", ""+Math.pow((double) timeDifference / 1000.0d, 2));
            }
            lastTimestamp = currentTime;
            accelerationX = sensorEvent.values[0];
            accelerationY = sensorEvent.values[1];
            accelerationZ = sensorEvent.values[2];
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(inertialLocIntent);
        unregisterReceiver(locationChangedReceiver);
    }

    class LocationChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case "locate":
                    ClientPos ClientPos = (ClientPos) intent.getSerializableExtra("pos_data");
                    NowClientPos.setPosPara(ClientPos);
                    distanceTextView.setText(String.format(Locale.UK, "Coordinates are: .5%f, .2%f", ClientPos.getLatitude(), ClientPos.getLongitude()));
                    break;
                default:
                    break;
            }
        }
    }
}