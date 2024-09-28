package com.store.neareststores;
import android.Manifest;

import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap googleMap;
    private AlertDialog alertDialog;
    private Location currentLocation;

    private LatLng selectedLocation;
    private final List<String> favoriteStores = new ArrayList<>();
    private final HashMap<String, Marker> markerMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize MarkerUtils
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        Button viewFavoritesButton = findViewById(R.id.view_favorites_button);
        Button removeAllFavoritesButton = findViewById(R.id.remove_all_favorites_button);

        // Set the title of the activity
        setTitle(getResources().getString(R.string.title_activity_maps));

        // Initialize the Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MainActivity", "Map fragment is null");
        }

        // Check if Google Maps is installed
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.google.android.apps.maps", PackageManager.GET_ACTIVITIES);
            // Google Maps is installed
        } catch (PackageManager.NameNotFoundException e) {
            // Google Maps is not installed
            Toast.makeText(this, "Google Maps is not installed. Please install it to use this feature.", Toast.LENGTH_SHORT).show();
            // Open the Google Play Store to install Google Maps
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.google.android.apps.maps"));
            startActivity(intent);
        }

        // Get current location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                        // Move the camera to the current location
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    } else {
                        // Handle the case when the location is null
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        // Request location updates
                        fusedLocationClient.requestLocationUpdates(LocationRequest.create(), locationCallback, null);
                    }
                });

        viewFavoritesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoriteStoresActivity.class);
            startActivity(intent);
            intent.putExtra("selectedLocation", selectedLocation);
        });

        removeAllFavoritesButton.setOnClickListener(v -> {
            removeMarkersAndFavorites();
        });

        // Add a button to select a location
        Button selectLocationButton = findViewById(R.id.select_location_button);
        selectLocationButton.setOnClickListener(v -> {
            showLocationDialog();
        });
    }

    private void showLocationDialog() {
        // Create a dialog with different locations
        AlertDialog.Builder locationDialog = new AlertDialog.Builder(MainActivity.this);
        locationDialog.setTitle("Select a Location");

        // Create a custom layout
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add buttons for different locations
        Button thessalonikiButton = new Button(MainActivity.this);
        thessalonikiButton.setText("Thessaloniki, Greece");
        thessalonikiButton.setOnClickListener(v -> {
            // Set the center of the map to Thessaloniki
            LatLng thessalonikiCenter = new LatLng(40.6401, 22.9444);
            selectedLocation = new LatLng(40.6401, 22.9444);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(thessalonikiCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(thessalonikiButton);

        Button athensButton = new Button(MainActivity.this);
        athensButton.setText("Athens, Greece");
        athensButton.setOnClickListener(v -> {
            // Set the center of the map to Athens
            LatLng athensCenter = new LatLng(37.9838, 23.7275);
            selectedLocation = new LatLng(37.9838, 23.7275);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(athensCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(athensButton);

        Button parisButton = new Button(MainActivity.this);
        parisButton.setText("Paris, France");
        parisButton.setOnClickListener(v -> {
            // Set the center of the map to Paris
            LatLng parisCenter = new LatLng(48.8567, 2.3508);
            selectedLocation = new LatLng(48.8567, 2.3508);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(parisCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(parisButton);

        Button londonButton = new Button(MainActivity.this);
        londonButton.setText("London, UK");
        londonButton.setOnClickListener(v -> {
            // Set the center of the map to London
            LatLng londonCenter = new LatLng(51.5074, -0.1278);
            selectedLocation = new LatLng(51.5074, -0.1278);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(londonCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(londonButton);

        Button newYorkButton = new Button(MainActivity.this);
        newYorkButton.setText("New York, USA");
        newYorkButton.setOnClickListener(v -> {
            // Set the center of the map to New York
            LatLng newYorkCenter = new LatLng(40.7128, -74.0060);
            selectedLocation = new LatLng(40.7128, -74.0060);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newYorkCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(newYorkButton);

        Button tokyoButton = new Button(MainActivity.this);
        tokyoButton.setText("Tokyo, Japan");
        tokyoButton.setOnClickListener(v -> {
            // Set the center of the map to Tokyo
            LatLng tokyoCenter = new LatLng(35.6895, 139.7670);
            selectedLocation = new LatLng(35.6895, 139.7670);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tokyoCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(tokyoButton);

        Button sydneyButton = new Button(MainActivity.this);
        sydneyButton.setText("Sydney, Australia");
        sydneyButton.setOnClickListener(v -> {
            // Set the center of the map to Sydney
            LatLng sydneyCenter = new LatLng(-33.8651, 151.2099);
            selectedLocation = new LatLng(-33.8651, 151.2099);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydneyCenter, 15));
            fetchNearbyStores();
        });
        layout.addView(sydneyButton);

        locationDialog.setView(layout);

        locationDialog.setPositiveButton("Close", (dialog, which) -> {
            // Close the dialog when the "Close" button is clicked
            dialog.dismiss();
        });

        locationDialog.show(); // Don't forget to show the dialog
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                // Move the camera to the current location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                fetchNearbyStores();
            } else {
                // Handle the case when the location is null
                Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void fetchNearbyStores() {
        String apiKey = ""; // Replace with your actual API key
        if (currentLocation != null) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&radius=1000&type=&key=" + apiKey;
            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray storesArray = jsonObject.getJSONArray("results");

                        // Add markers to the map for each store
                        for (int i = 0; i < storesArray.length(); i++) {
                            JSONObject storeObject = storesArray.getJSONObject(i);
                            String storeName = storeObject.getString("name");
                            double latitude = storeObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            double longitude = storeObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                            LatLng storeLocation = new LatLng(latitude, longitude);
                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(storeLocation)
                                    .title(storeName));
                            markerMap.put(storeName, marker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle the error
                }
            });

            queue.add(stringRequest);
        } else {
            // Handle the case when the current location is null
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Set the center of the map to Thessaloniki by default
        LatLng thessalonikiCenter = new LatLng(40.6401, 22.9444);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(thessalonikiCenter, 15));
        selectedLocation = thessalonikiCenter; // Set the selected location to Thessaloniki
        // Move the camera to Thessaloniki
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(thessalonikiCenter));
        fetchNearbyStores(); // Fetch nearby stores for the default location
        // Load markers from database
        loadMarkersFromDatabase();
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                View view = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView titleTextView = view.findViewById(R.id.title);
                titleTextView.setText(marker.getTitle());
                return view;
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Get the position of the marker
                LatLng markerPosition = marker.getPosition();

                // Move the camera to the marker's position
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15));

                // Get the title of the marker
                String storeName = marker.getTitle();

                // Show a dialog with options for the marker
                showMarkerOptions(storeName);

                return true;
            }
        });

        googleMap.setOnMapClickListener(latLng -> {
            // Add a red marker at the clicked location
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Loading...")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Add the marker to the markerMap
            markerMap.put("Loading...", marker);

            // Show a dialog to enter the name of the store
            AlertDialog.Builder nameDialog = new AlertDialog.Builder(MainActivity.this);
            nameDialog.setTitle("Enter Store Name and its current location in the format {Store name}, {City}, {Country}");
            final EditText nameEditText = new EditText(MainActivity.this);
            nameDialog.setView(nameEditText);
            nameDialog.setPositiveButton("OK", (dialog, which) -> {
                String storeName = nameEditText.getText().toString();
                // Update the marker title with the store name
                assert marker != null;
                marker.setTitle(storeName);

                // Update the marker in the markerMap
                markerMap.put(storeName, marker);

                // Show a dialog to confirm adding to favorites
                AlertDialog.Builder favoriteDialog = new AlertDialog.Builder(MainActivity.this);
                favoriteDialog.setMessage("Do you want to add " + storeName + " to your favorite stores?");

                favoriteDialog.setPositiveButton("Yes", (dialog1, which1) -> {
                    // Add the store to favorites
                    Store store = new Store(storeName, latLng.latitude, latLng.longitude);
                    try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                        if (databaseHelper.addStoreToFavorites(store)) {
                            Toast.makeText(MainActivity.this, "Store added to favorites", Toast.LENGTH_SHORT).show();
                            favoriteStores.add(storeName);
                        } else {
                            Toast.makeText(MainActivity.this, "Error adding store to favorites", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Handle any exceptions that might occur
                        e.printStackTrace();
                    }
                });
                favoriteDialog.setNegativeButton("No", (dialog1, which1) -> {
                    // Do nothing
                });
                favoriteDialog.show();
            });
            nameDialog.setNegativeButton("Cancel", (dialog, which) -> {
                // Remove the marker
                assert marker != null;
                marker.remove();
            });
            nameDialog.show();
        });

        // Load favorite stores from database
        try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
            List<Store> favoriteStores = databaseHelper.getFavoriteStores();

            // Add markers to the map for each store and store them in the HashMap
            for (Store store : favoriteStores) {
                LatLng storeLocation = new LatLng(store.getLatitude(), store.getLongitude());
                String locationString = store.getName() + ", " + selectedLocation; // Get the location string
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(storeLocation)
                        .title(locationString));
                markerMap.put(store.getName(), marker);
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur
            e.printStackTrace();
        }
    }

    private void showMarkerOptions(String storeName) {
        // Retrieve the marker from the HashMap
        Marker marker = markerMap.get(storeName);
        if (marker == null) {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a dialog with options for the marker
        AlertDialog.Builder optionsDialog = new AlertDialog.Builder(MainActivity.this);
        optionsDialog.setTitle("Marker Options");

        // Create a custom layout
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Create buttons programmatically
        Button viewDetailsButton = new Button(MainActivity.this);
        viewDetailsButton.setText("View Details");
        viewDetailsButton.setOnClickListener(v -> {
            showStoreDetails(marker);
        });
        layout.addView(viewDetailsButton);

        Button rateStoreButton = new Button(MainActivity.this);
        rateStoreButton.setText("Rate Store");
        rateStoreButton.setOnClickListener(v -> {
            rateStore(marker);
        });
        layout.addView(rateStoreButton);

        Button viewReviewsButton = new Button(MainActivity.this);
        viewReviewsButton.setText("View Reviews");
        viewReviewsButton.setOnClickListener(v -> {
            viewReviews(marker);
        });
        layout.addView(viewReviewsButton);

        Button viewRatingsButton = new Button(MainActivity.this);
        viewRatingsButton.setText("View Ratings");
        viewRatingsButton.setOnClickListener(v -> {
            viewRatings(marker);
        });
        layout.addView(viewRatingsButton);

        Button leaveReviewButton = new Button(MainActivity.this);
        leaveReviewButton.setText("Leave Review");
        leaveReviewButton.setOnClickListener(v -> {
            leaveReview(marker);
        });
        layout.addView(leaveReviewButton);

        Button navigateToStoreButton = new Button(MainActivity.this);
        navigateToStoreButton.setText("Navigate to Store");
        navigateToStoreButton.setOnClickListener(v -> {
            navigateToStore(marker);
        });
        layout.addView(navigateToStoreButton);

        Button removeMarkerButton = new Button(MainActivity.this);
        removeMarkerButton.setText("Remove Marker");
        removeMarkerButton.setOnClickListener(v -> {
            removeMarker(marker);
        });
        layout.addView(removeMarkerButton);

        optionsDialog.setView(layout);

        optionsDialog.setPositiveButton("Close", (dialog, which) -> {
            // Close the dialog when the "Close" button is clicked
            dialog.dismiss();
        });

        AlertDialog optionsDialogAlert = optionsDialog.create();
        optionsDialogAlert.show(); // Don't forget to show the dialog
    }

    private void showStoreDetails(Marker marker) {
        if (marker == null) {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder detailsDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        detailsDialogBuilder.setTitle("Store Details");

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.store_details_dialog, null);
        TextView messageTextView = dialogLayout.findViewById(R.id.message_text_view);

        messageTextView.setText("Name: " + marker.getTitle() + "\nAddress: " + marker.getPosition() + "\nRating: " + getRatingForStore(marker.getTitle()) + "/5\nOpening Hours: 9:00 AM - 5:00 PM\n");

        detailsDialogBuilder.setView(dialogLayout);
        detailsDialogBuilder.setPositiveButton("Ok", (dialog, which) -> {
            dialog.dismiss();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        });

        AlertDialog detailsDialog = detailsDialogBuilder.create();
        detailsDialog.show();
    }
    private void addMarkerToMap(LatLng location, String storeName) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(storeName));
        assert marker != null;
        marker.setTag(storeName); // Set the store name as the tag
        markerMap.put(storeName, marker);
    }
    private void loadMarkersFromDatabase() {
        try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
            List<Store> favoriteStores = databaseHelper.getFavoriteStores();

            // Add markers to the map for each store and store them in the HashMap
            for (Store store : favoriteStores) {
                LatLng storeLocation = new LatLng(store.getLatitude(), store.getLongitude());
                addMarkerToMap(storeLocation, store.getName());
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur
            e.printStackTrace();
        }
    }
    private void rateStore(Marker marker) {
        if (marker != null) {
            // Show a dialog to rate the store
            AlertDialog.Builder rateDialog = new AlertDialog.Builder(MainActivity.this);
            rateDialog.setTitle("Rate this store");
            final EditText ratingEditText = new EditText(MainActivity.this);
            ratingEditText.setHint("Enter rating (1-5)");
            rateDialog.setView(ratingEditText);
            rateDialog.setPositiveButton("OK", (dialog, which) -> {
                String ratingText = ratingEditText.getText().toString();
                if (!ratingText.isEmpty()) {
                    double rating = Double.parseDouble(ratingText);
                    if (rating >= 1 && rating <= 5) {
                        // Add the rating to the database
                        addRating(marker.getTitle(), rating);
                        Toast.makeText(MainActivity.this, "Rating added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid rating. Please enter a rating between 1 and 5.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a rating.", Toast.LENGTH_SHORT).show();
                }
            });
            rateDialog.setNegativeButton("Cancel", (dialog, which) -> {
                // Do nothing
            });
            rateDialog.show();
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRating(String storeName, double rating) {
        try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
            if (databaseHelper.addRating(storeName, rating)) {
                // Rating added successfully
            } else {
                // Error adding rating
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur
            e.printStackTrace();
        }
    }

    private double getRatingForStore(String storeName) {
        try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
            return databaseHelper.getRatingForStore(storeName);
        } catch (Exception e) {
            // Handle any exceptions that might occur
            e.printStackTrace();
            return 0.0;
        }
    }

    private void leaveReview(Marker marker) {
        if (marker != null) {
            // Show a dialog to enter the review
            AlertDialog.Builder reviewDialog = new AlertDialog.Builder(MainActivity.this);
            reviewDialog.setTitle("Leave a Review");
            final EditText reviewEditText = new EditText(MainActivity.this);
            reviewDialog.setView(reviewEditText);
            reviewDialog.setPositiveButton("OK", (dialog2, which2) -> {
                String review = reviewEditText.getText().toString();
                // Store the review in the database
                try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                    if (databaseHelper.addReview(marker.getTitle(), review)) {
                        Toast.makeText(MainActivity.this, "Review added successfully", Toast.LENGTH_SHORT).show();
                        // Get the reviews for the category
                        List<MyReview> reviews = databaseHelper.getReviewsForStore(marker.getTitle());
                        // Create a string to hold all the reviews
                        StringBuilder reviewsStringBuilder = new StringBuilder();
                        for (MyReview reviewItem : reviews) {
                            reviewsStringBuilder.append(marker.getTitle()).append(": ").append(reviewItem.getText()).append("\n");
                        }
                        // Create and show the dialog
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("Reviews");
                        alertDialogBuilder.setMessage(reviewsStringBuilder.toString());
                        alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error adding review", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Handle any exceptions that might occur
                    e.printStackTrace();
                }
            });
            reviewDialog.setNegativeButton("Cancel", (dialog2, which2) -> {
                // Do nothing
            });
            reviewDialog.show();
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewReviews(Marker marker) {
        if (marker != null) {
            // Show a dialog to view the reviews
            try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                List<MyReview> reviews = databaseHelper.getReviewsForStore(marker.getTitle());
                if (reviews.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No reviews for this store", Toast.LENGTH_SHORT).show();
                } else {
                    // Create a string to hold all the reviews
                    StringBuilder reviewsStringBuilder = new StringBuilder();
                    for (MyReview reviewItem : reviews) {
                        reviewsStringBuilder.append(marker.getTitle()).append(": ").append(reviewItem.getText()).append("\n");
                    }
                    // Create and show the dialog
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Reviews");
                    alertDialogBuilder.setMessage(reviewsStringBuilder.toString());
                    alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            } catch (Exception e) {
                // Handle any exceptions that might occur
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMarkersAndFavorites() {
        try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
            databaseHelper.removeAllFavorites();
            markerMap.clear();
            googleMap.clear();
            favoriteStores.clear();
            Toast.makeText(MainActivity.this, "All favorites removed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void viewRatings(Marker marker) {
        if (marker != null) {
            // Show a dialog to view the ratings
            try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                double rating = databaseHelper.getRatingForStore(marker.getTitle());
                if (rating == 0.0) {
                    Toast.makeText(MainActivity.this, "No ratings for this store", Toast.LENGTH_SHORT).show();
                } else {
                    // Create and show the dialog
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle("Ratings");
                    alertDialogBuilder.setMessage(marker.getTitle() + ": " + rating + "/5");
                    alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            } catch (Exception e) {
                // Handle any exceptions that might occur
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToStore(Marker marker) {
        if (marker != null) {
            // Get the latitude and longitude of the selected store
            LatLng storeLocation = marker.getPosition();

            // Create a Uri for the Google Maps app
            Uri navigationUri = Uri.parse("googlemaps://maps?saddr=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&daddr=" + storeLocation.latitude + "," + storeLocation.longitude + "&mode=w");

            // Create an intent to open the Google Maps app
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, navigationUri);

            // Check if the Google Maps app is installed
            PackageManager pm = getPackageManager();
            if (pm.resolveActivity(navigationIntent, 0) != null) {
                // Open the Google Maps app
                startActivity(navigationIntent);
            } else {
                // Google Maps app is not installed, show a toast message
                Toast.makeText(this, "Google Maps app is not installed. Please install it to use this feature.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }
    private void removeMarker(Marker marker) {
        if (marker != null) {
            // Remove the marker from the map
            marker.remove();
            // Remove the store from the database
            String storeName = marker.getTitle();
            try (DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this)) {
                if (databaseHelper.removeStoreFromFavorites(storeName)) {
                    Toast.makeText(MainActivity.this, "Store removed from favorites", Toast.LENGTH_SHORT).show();
                    // Remove from favorites
                    favoriteStores.remove(storeName);
                    // Remove the marker from the markerMap
                    markerMap.remove(storeName);
                } else {
                    Toast.makeText(MainActivity.this, "Error removing store from favorites", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error removing store from favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Marker not found", Toast.LENGTH_SHORT).show();
        }
    }
}