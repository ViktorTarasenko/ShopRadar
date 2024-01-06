package com.wilk.shopnavigator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.wilk.shopnavigator.data.ShopItem;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private int nextLocateServiceNumber = 0;
    private LocationChangedReceiver locationChangedReceiver;
    private TextView distanceTextView;
    private Button markZeroLocationButton;
    private Button addItemButton;
    private Intent inertialLocIntent;
    private Set<ShopItem> items;
    private AddShopItemDialogFragment addShopItemDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = loadItems();
        distanceTextView = findViewById(R.id.coordinatesTextView);
        markZeroLocationButton = findViewById(R.id.markZeroLocationButton);
        addShopItemDialogFragment = new AddShopItemDialogFragment();
        markZeroLocationButton.setOnClickListener(view -> {
            clearItems();
            restartLocationService();
        });
        addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(view -> {
            showAddShopItemDialog();
        });
        startLocationService();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();
    }
    private void startLocationService() {
        NowClientPos.reset();
        distanceTextView.setText(getResources().getString(R.string.defaults_coordinates_text));
        inertialLocIntent = new Intent(MainActivity.this, InertialLocateService.class);
        ClientPos ClientPos = new ClientPos(NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude());
        inertialLocIntent.putExtra("init_pos", ClientPos);
        inertialLocIntent.putExtra("service_number", nextLocateServiceNumber++);
        locationChangedReceiver = new LocationChangedReceiver();
        IntentFilter dataIntentFilter = new IntentFilter();
        dataIntentFilter.addAction("locate");
        registerReceiver(locationChangedReceiver, dataIntentFilter);
        startService(inertialLocIntent);
    }
    private void showAddShopItemDialog() {
        addShopItemDialogFragment.show(getSupportFragmentManager(), "Add new ShopItem");
    }
    private void addShopItem(String itemName) {
        items.add(new ShopItem(itemName, NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude()));
    }
    private void stopLocationService() {
        stopService(inertialLocIntent);
        unregisterReceiver(locationChangedReceiver);
    }
    private void restartLocationService() {
        stopLocationService();
        startLocationService();
    }
    private void clearItems() {
        items = initItems();
    }
    private Set<ShopItem> loadItems() {
        return initItems();
    }
    private Set<ShopItem> initItems() {
        return new HashSet<>();
    }

    private class LocationChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.requireNonNull(action).equals("locate")) {
                ClientPos clientPos = (ClientPos) intent.getSerializableExtra("pos_data");
                int locateServiceNumber = intent.getIntExtra("service_number", 0);
                if (locateServiceNumber == MainActivity.this.nextLocateServiceNumber - 1) {
                    NowClientPos.setPosPara(clientPos);
                    distanceTextView.setText(String.format(Locale.UK, "Coordinates are: %1.5f, %1.5f", clientPos.getLatitude(), clientPos.getLongitude()));
                }
            }
        }
    }
    public static class AddShopItemDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_item_dialog, null);
            TextView shopItemNameTextView = dialogView.findViewById(R.id.shopItemName);
            builder.setView(dialogView).
                    setPositiveButton(R.string.save, (dialog, id) -> {
                        ((MainActivity) requireActivity()).addShopItem(shopItemNameTextView.getText().toString());
                        shopItemNameTextView.setText("");
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> Objects.requireNonNull(AddShopItemDialogFragment.this.getDialog()).cancel());
            return builder.create();
        }
    }
}