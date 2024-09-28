package com.store.neareststores;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.Marker;
import com.store.neareststores.DatabaseHelper;
import com.store.neareststores.StoreAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStoresActivity extends AppCompatActivity {

    private ListView favoriteListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_stores);

        initializeViews();
        loadFavoriteStores();
    }

    private void initializeViews() {
        favoriteListView = findViewById(R.id.list_view);
        Button backToMainActivityButton = findViewById(R.id.back_button);

        backToMainActivityButton.setOnClickListener(v -> {
            finish(); // Close the activity and return to the previous one
        });
    }

    private void loadFavoriteStores() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        try {
            List<Store> favoriteStores = databaseHelper.getFavoriteStores();

            if (favoriteStores.isEmpty()) {
                Toast.makeText(this, "No favorite stores found", Toast.LENGTH_SHORT).show();
            } else {
                StoreAdapter adapter = new StoreAdapter(FavoriteStoresActivity.this, favoriteStores);
                favoriteListView.setAdapter(adapter);

                favoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Store selectedStore = favoriteStores.get(position);
                        redirectToMarkerLocation(selectedStore);
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading favorite stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToMarkerLocation(Store store) {
        // Redirect to the marker location
        Intent intent = new Intent(FavoriteStoresActivity.this, MainActivity.class);
        intent.putExtra("latitude", store.getLatitude());
        intent.putExtra("longitude", store.getLongitude());
        intent.putExtra("storeName", store.getName());
        startActivity(intent);
    }
}



