package com.wilk.shopnavigator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    LocationChangedReceiver locationChangedReceiver;
    private TextView distanceTextView;
    private Intent inertialLocIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        distanceTextView = findViewById(R.id.distanceTextView);
        inertialLocIntent = new Intent(MainActivity.this, InertialLocateService.class);
        ClientPos ClientPos = new ClientPos(NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude());
        inertialLocIntent.putExtra("init_pos", ClientPos);
        locationChangedReceiver = new LocationChangedReceiver();
        IntentFilter dataIntentFilter = new IntentFilter();
        dataIntentFilter.addAction("locate");
        registerReceiver(locationChangedReceiver, dataIntentFilter);
        startService(inertialLocIntent);
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
            if (Objects.requireNonNull(action).equals("locate")) {
                ClientPos clientPos = (ClientPos) intent.getSerializableExtra("pos_data");
                NowClientPos.setPosPara(clientPos);
                distanceTextView.setText(String.format(Locale.UK, "Coordinates are: %1.5f, %1.5f", clientPos.getLatitude(), clientPos.getLongitude()));
            }
        }
    }
}