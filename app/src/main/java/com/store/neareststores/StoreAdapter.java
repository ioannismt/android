package com.store.neareststores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.libraries.places.api.model.Review;

import java.util.List;

public class StoreAdapter extends BaseAdapter {
    private final Context context;
    private final List<Store> stores;

    public StoreAdapter(Context context, List<Store> stores) {
        this.context = context;
        this.stores = stores;
    }

    @Override
    public int getCount() {
        return stores.size();
    }

    @Override
    public Object getItem(int position) {
        return stores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_store, parent, false);
        }

        Store store = stores.get(position);

        TextView nameTextView = view.findViewById(R.id.name_text_view);
        nameTextView.setText(store.getName());


        return view;
    }
}