package com.mygdx.game;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.mygdx.game.data.ShopItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    private String loadedMap;
    private int nextLocateServiceNumber = 0;
    private LocationChangedReceiver locationChangedReceiver;
    private TextView distanceTextView;
    private Intent inertialLocIntent;
    private final ObservableList<ShopItem> items = new ObservableArrayList<>();
    private AddShopItemDialogFragment addShopItemDialogFragment;
    private SaveMapDialogFragment saveMapDialogFragment;
    private final ShopRadarRenderer shopRadarRenderer = new ShopRadarRenderer(NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadItems();
        setLoadedMap(null);
        distanceTextView = findViewById(R.id.coordinatesTextView);
        Button markZeroLocationButton = findViewById(R.id.markZeroLocationButton);
        addShopItemDialogFragment = new AddShopItemDialogFragment();
        saveMapDialogFragment = new SaveMapDialogFragment();
        markZeroLocationButton.setOnClickListener(view -> {
            restartLocationService();
        });
        Button addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(view -> showAddShopItemDialog());
        Button saveMapButton = findViewById(R.id.saveMapButton);
        saveMapButton.setOnClickListener(v -> saveMap());
        Button closeMapButton = findViewById(R.id.closeMapButton);
        closeMapButton.setOnClickListener(v -> closeMap());
        Button loadMapButton = findViewById(R.id.loadMapButton);
        loadMapButton.setOnClickListener(v -> loadMap());

        RadarFragment fragment = new RadarFragment(shopRadarRenderer);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.radarLayout, fragment);
        trans.commit();
        items.addOnListChangedCallback(new ShopItemsOnListChangedCallback());
        startLocationService();
    }

    private void loadMap() {
        Log.e("file is", getFilesDir().getAbsolutePath());
        final List<String> mapNames = Arrays.stream(Objects.requireNonNull(getFilesDir().listFiles())).map(File::getName).collect(Collectors.toList());
        if (mapNames.isEmpty()) {
            Toast.makeText(this, "no saved maps!", Toast.LENGTH_LONG).show();
        } else {
            new LoadMapDialogFragment(mapNames).show(getSupportFragmentManager(), "Load map");
        }
    }

    private void loadMapFromFile(String mapName) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(new File(getFilesDir(), mapName))));
        List<ShopItem> items = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            String name = null;
            Double latitude = null;
            Double longitude = null;
            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();
                if (fieldName.equals("name")) {
                    name = reader.nextString();
                } else if (fieldName.equals("latitude")) {
                    latitude = reader.nextDouble();
                } else if (fieldName.equals("longitude")) {
                    longitude = reader.nextDouble();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            if ((name != null) && (latitude != null) && (longitude != null)) {
                items.add(new ShopItem(name, latitude, longitude));
            }

        }
        reader.endArray();
        this.items.addAll(items);
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
        registerReceiver(locationChangedReceiver, dataIntentFilter, RECEIVER_EXPORTED);
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
        Gdx.app.postRunnable(() -> shopRadarRenderer.passClientPosition(NowClientPos.getNowLatitude(), NowClientPos.getNowLongitude()));
        startLocationService();
    }

    private void loadItems() {
       //TODO! nothing for now
    }

    private void saveMap() {
        if (loadedMap != null) {
            saveMapToFile(loadedMap);
        } else {
            saveMapDialogFragment.show(getSupportFragmentManager(), "Save map");
        }
    }

    private void closeMap() {
        clearItems();
        setLoadedMap(null);
    }

    private void setLoadedMap(String loadedMap) {
        this.loadedMap = loadedMap;
        if (loadedMap == null) {
            setTitle("New map");
        } else {
            setTitle(String.format("Loaded map: %s", loadedMap));
        }
    }

    private void clearItems() {
        items.clear();
    }

    private void saveMapToFile(String mapName) {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(getFilesDir(), mapName)), StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (ShopItem shopItem : items) {
                writer.beginObject();
                writer.name("name").value(shopItem.getName());
                writer.name("latitude").value(shopItem.getLatitude());
                writer.name("longitude").value(shopItem.getLongitude());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch (IOException e) {
            Toast.makeText(this, "error saving file !", Toast.LENGTH_LONG).show();
            Log.e("", "", e);
        }
    }

    @Override
    public void exit() {

    }

    private class ShopItemsOnListChangedCallback extends ObservableList.OnListChangedCallback<ObservableList<ShopItem>> {

        @Override
        public void onChanged(ObservableList<ShopItem> sender) {
            passItemsToRenderer(sender);
        }

        @Override
        public void onItemRangeChanged(ObservableList<ShopItem> sender, int positionStart, int itemCount) {
            passItemsToRenderer(sender);
        }

        @Override
        public void onItemRangeInserted(ObservableList<ShopItem> sender, int positionStart, int itemCount) {
            passItemsToRenderer(sender);
        }

        @Override
        public void onItemRangeMoved(ObservableList<ShopItem> sender, int fromPosition, int toPosition, int itemCount) {
            passItemsToRenderer(sender);
        }

        @Override
        public void onItemRangeRemoved(ObservableList<ShopItem> sender, int positionStart, int itemCount) {
            passItemsToRenderer(sender);
        }
        private void passItemsToRenderer(final ObservableList<ShopItem> items) {
            List<ShopItemDrawable> shopItemsDrawables = items.
                    stream().
                    map(shopItem -> new ShopItemDrawable(
                            shopItem.getName(),
                            shopItem.getLatitude(),
                            shopItem.getLongitude()))
                    .collect(Collectors.toList());
            Gdx.app.postRunnable(() -> shopRadarRenderer.passShopItemDrawables(shopItemsDrawables));
        }
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
                    Gdx.app.postRunnable(() -> shopRadarRenderer.passClientPosition(clientPos.getLatitude(), clientPos.getLongitude()));

                    distanceTextView.setText(String.format(Locale.UK, "Coordinates are: %1.5f, %1.5f", clientPos.getLatitude(), clientPos.getLongitude()));
                }
            }
        }
    }

    public static class RadarFragment extends AndroidFragmentApplication {
        private final ShopRadarRenderer shopRadarRenderer;

        public RadarFragment(ShopRadarRenderer shopRadarRenderer) {
            this.shopRadarRenderer = shopRadarRenderer;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
            return initializeForView(shopRadarRenderer, config);
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

    public static class SaveMapDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.save_map_dialog, null);
            TextView mapNameTextView = dialogView.findViewById(R.id.mapName);
            builder.setView(dialogView).
                    setPositiveButton(R.string.save, (dialog, id) -> {
                        String mapName = mapNameTextView.getText().toString();
                        ((MainActivity) requireActivity()).saveMapToFile(mapName);
                        ((MainActivity) requireActivity()).setLoadedMap(mapName);
                    })
                    .setNegativeButton(R.string.cancel, (dialog, id) -> Objects.requireNonNull(SaveMapDialogFragment.this.getDialog()).cancel());
            return builder.create();
        }
    }

    public static class LoadMapDialogFragment extends DialogFragment {
        private final List<String> mapNames;

        public LoadMapDialogFragment(List<String> mapNames) {
            this.mapNames = mapNames;
        }

        public LoadMapDialogFragment() {
            mapNames = new ArrayList<>();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.load_map_dialog, null);
            ListView mapNamesListView = dialogView.findViewById(R.id.mapNames);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, mapNames);
            mapNamesListView.setAdapter(adapter);
            mapNamesListView.setOnItemClickListener((parent, view, position, id) -> {
                String mapName = adapter.getItem(position);
                try {
                    ((MainActivity) requireActivity()).loadMapFromFile(mapName);
                    ((MainActivity) requireActivity()).setLoadedMap(mapName);
                    Objects.requireNonNull(LoadMapDialogFragment.this.getDialog()).cancel();
                } catch (IOException e) {
                    Log.e("", "", e);
                    Toast.makeText(getActivity(), "error loading map!", Toast.LENGTH_LONG).show();
                }
            });
            builder.setView(dialogView)
                    .setNegativeButton(R.string.cancel, (dialog, id) -> Objects.requireNonNull(LoadMapDialogFragment.this.getDialog()).cancel());
            return builder.create();
        }
    }

}