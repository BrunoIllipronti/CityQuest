package com.illipronti.cityquest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainSection extends AppCompatActivity implements View.OnClickListener  {

    private final int USER_STATUS_LOGOUT = 9;

    private RecyclerView rvQuests;
    private Button btnScan;
    private Button btnMap;
    private TextView txtUsername;
    private TextView txtCQpoints;

    private SharedPreferences.Editor editor;

    @SuppressLint({"CommitPrefEdits", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_section);

        // Action bar
        //Toolbar myToolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(myToolbar);

        // Logo Image Setup
        ImageView img_main_background = (ImageView) findViewById(R.id.imgMain);
        img_main_background.setImageResource(R.drawable.main_background);

        // Set the Scan button.
        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);

        // Set the Map button.
        btnMap = findViewById(R.id.btnMap);
        btnMap.setOnClickListener(this);

        // Set the Recycler View
        rvQuests = findViewById(R.id.rv_quests);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        rvQuests.setLayoutManager(layoutManager);
        rvQuests.setAdapter(new QuestsRecyclerViewAdapter(R.layout.activity_recycler_item));

        // Load User Shared Preferences
        refreshUserData();
    }

    //==================  Action Bar code blocks - BEGIN  ==================
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                Toast.makeText(this, "Refresh Button hit ", Toast.LENGTH_LONG).show();
                return true;

            case R.id.account:
                Toast.makeText(this, "MyAccount - Pressed ", Toast.LENGTH_LONG).show();
                return true;

            case R.id.exit:
                editor.putInt("isLogged",USER_STATUS_LOGOUT);
                editor.apply();

                Intent intent = new Intent(MainSection.this, UserLogin.class);
                startActivity(intent);
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized / Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
    //==================  Action Bar code blocks - END  ==================

    @SuppressLint("SetTextI18n")
    private void refreshUserData(){
        // Load User Session Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load User data
        txtUsername = findViewById(R.id.txtUsername);
        txtUsername.setText("Hi " + sharedPreferences.getString("userName", null) + " !");

        txtCQpoints = findViewById(R.id.txtCQPoints);
        txtCQpoints.setText("Your CQ Points: " + sharedPreferences.getInt("cqPoints", 0) );
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                Toast.makeText(this, "Scan Button Pressed.... scan QR! ", Toast.LENGTH_LONG).show();
                break;

            case R.id.btnMap:
                Toast.makeText(this, "Map Button Pressed.... go to map! ", Toast.LENGTH_LONG).show();
                break;
        }
    }







}