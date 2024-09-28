package com.store.neareststores;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class StoreListActivity extends AppCompatActivity {

    private ListView storeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);

        initializeViews();
        loadStoreList();
    }

    private void initializeViews() {
        storeListView = findViewById(R.id.store_list_view);
    }

    private void loadStoreList() {
        ArrayList<Store> stores = getIntent().getParcelableArrayListExtra("stores");
        assert stores != null;

        StoreAdapter adapter = new StoreAdapter(this, stores);
        storeListView.setAdapter(adapter);

        storeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Store selectedStore = stores.get(position);
                redirectToMarkerLocation(selectedStore);
            }
        });
    }

    private void redirectToMarkerLocation(Store store) {
        // Redirect to the marker location
        Intent intent = new Intent(StoreListActivity.this, MainActivity.class);
        intent.putExtra("latitude", store.getLatitude());
        intent.putExtra("longitude", store.getLongitude());
        intent.putExtra("storeName", store.getName());
        startActivity(intent);
    }
}