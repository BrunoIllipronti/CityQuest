package com.illipronti.cityquest;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestsMap extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, View.OnClickListener {

    final String streetsStyle   = Style.MAPBOX_STREETS;
    final String lightStyle     = Style.LIGHT;
    final String satelliteStyle = Style.SATELLITE_STREETS;
    private String style = streetsStyle;
    private int indexCameraFly = -1;
    private int myLocation = 0;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private SharedPreferences.Editor editor;

    private final int STANDARD_REQUEST_CODE = 0;
    private final int USER_STATUS_LOGOUT = 9;
    private ArrayList<QuestLocation> questLocationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        questLocationList = new ArrayList<QuestLocation>();

        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Be sure to call this setContentView
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_quests_map);

        // Set the Scan button.
        Button btnScan = findViewById(R.id.mapBtnScan);
        btnScan.setOnClickListener(this);

        // Set the Map button.
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(this);

        handleInProgressMapQuests();
    }

    //=========== Firebase In Progress User Quest selection ============
    public void handleInProgressMapQuests(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load User In Progress Quests
        List<Long> user_inprogress_quests = new ArrayList( this.getSharedPreferences("user_session", 0).getStringSet("inprogress_quests", null) );

        // If User has no In Progress Quests... just initialize it so Firebase can look for something
        if(user_inprogress_quests.isEmpty()){
            user_inprogress_quests.add(Long.valueOf(-1));
        }

        // Firestore SELECT * FROM quests WHERE questid IN user_inprogress_quests
        db.collection("quests")
                .whereIn("questid", user_inprogress_quests)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            questLocationList = new ArrayList<QuestLocation>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                questLocationList.add(new QuestLocation(QuestsMap.this, ((GeoPoint)document.get("location")).getLatitude(), ((GeoPoint)document.get("location")).getLongitude(), document.get("quest").toString()));
                            }

                            mapRender();
                        }
                    }
                });
    }

    //======================== Action Bar Actions - BEGIN ==================================
    /**
     * action bar menu inflation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.map_bar, menu);
        return true;
    }

    /**
     * onOptionItemSelected class to validate Render style / Marker locations / "My Location"
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.layer:
                if(style.equals(streetsStyle)){
                    style = lightStyle;
                }
                else if (style.equals(lightStyle)){
                    style = satelliteStyle;
                }
                else {
                    style = streetsStyle;
                }
                mapRender();
                return true;

            case R.id.location:
                myLocation = 1;
                Toast.makeText(this, "Moving to my location!", Toast.LENGTH_LONG).show();
                mapRender();
                return true;

            case R.id.appexit:
                editor.putInt("isLogged",USER_STATUS_LOGOUT);
                editor.apply();

                Intent intent = new Intent(QuestsMap.this, UserLogin.class);
                startActivity(intent);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized / Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapBtnScan:
                QrReader qr = new QrReader(this);
                qr.readCode();
                break;

            case R.id.btnReturn:
                returnToMainSection();
                finish();
                break;
        }
    }
    //======================== Action Bar Actions - END ==================================


    //======================== Map Rendeting - BEGIN ==================================
    private void mapRender(){
        mapView = (MapView) findViewById(R.id.mapView);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                QuestsMap.this.mapboxMap = mapboxMap;

                // Iterate through list of User In progress quests and add map markers
                for (QuestLocation loc : questLocationList) {

                    Log.d("BRUNO", "HEY! LOOP " + loc.title);

                    mapboxMap.addMarker(new MarkerOptions()
                            .position(loc.location)
                            .title(loc.title)
                            //.snippet(loc.description)
                            .icon(loc.icon)
                    );
                }

                // Move Camera effect based on Marker option
                if (indexCameraFly != -1) {
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(questLocationList.get(indexCameraFly).location, 12));
                }

                // Set map style
                mapboxMap.setStyle(new Style.Builder().fromUri(style),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                // Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.
                                if (myLocation == 1) {
                                    enableLocationComponent(style);
                                    myLocation = 0;
                                }
                            }
                        });
            }
        });
    }
    //======================== Map Rendering - END ==================================



    //======================== QR Code return - BEGIN ==================================
    @SuppressLint("ShowToast")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Create a Alert Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                ArrayList user_completed_quests  = new ArrayList( this.getSharedPreferences("user_session", 0).getStringSet("completed_quests", null) );
                ArrayList user_inprogress_quests = new ArrayList( this.getSharedPreferences("user_session", 0).getStringSet("inprogress_quests", null) );

                Long qr_read_result = Long.parseLong(result.getContents().split("\\|")[0]);
                QuestManager qm = new QuestManager(this);

                // Complete Quest
                if(user_inprogress_quests.contains(qr_read_result)){
                    user_inprogress_quests.removeAll(Arrays.asList( qr_read_result.longValue() ));
                    user_completed_quests.add(qr_read_result);

                    // Update User In Progress Quests
                    qm.updateUserQuests(user_inprogress_quests);

                    // Update User Completed Quests
                    qm.updateUserCompletedQuests(user_completed_quests);

                    returnToMainSection();
                }
                // If User didn't start this specific mission
                else if (!user_inprogress_quests.contains(qr_read_result) && !user_completed_quests.contains(qr_read_result)){

                    builder.setTitle("What ho!...");
                    builder.setMessage("Adventurer... Perhaps you are not subscribed to this quest!");
                }
                // If user already completed this quest
                else if (user_completed_quests.contains(qr_read_result)){

                    builder.setTitle("Greetings...");
                    builder.setMessage("You already completed this quest, Farewell");
                }

                builder.setNegativeButton("Aye!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                Toast.makeText(this, "Invalid Result", Toast.LENGTH_LONG);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void returnToMainSection(){
        Intent intent = new Intent(QuestsMap.this, MainSection.class);
        startActivityForResult(intent, STANDARD_REQUEST_CODE);
    }
    //======================== QR Code return - END ==================================

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /*
    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

     */

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {    }
}