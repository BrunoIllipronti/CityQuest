package com.illipronti.cityquest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;

public class MainSection extends AppCompatActivity implements View.OnClickListener  {

    private final int USER_STATUS_LOGOUT = 9;
    private Button btnScan;
    private Button btnMap;

    private SharedPreferences.Editor editor;

    @SuppressLint({"CommitPrefEdits", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_section);

        // Logo Image Setup
        ImageView img_main_background = (ImageView) findViewById(R.id.imgMain);
        img_main_background.setImageResource(R.drawable.main_background);

        // Set the Scan button.
        btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);

        // Set the Map button.
        btnMap = findViewById(R.id.btnMap);
        btnMap.setOnClickListener(this);

        // Load User Shared Preferences
        refreshData();
    }

    //==================  Action Bar code blocks - BEGIN  ==================
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refreshData();
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

    //================  Refresh MainSection data - BEGIN  ================
    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    private void refreshData(){
        // Set the Recycler View
        RecyclerView rvQuests = findViewById(R.id.rv_quests);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        rvQuests.setLayoutManager(layoutManager);
        rvQuests.setAdapter(new QuestsRecyclerViewAdapter(R.layout.activity_recycler_item, this));

        // Load User Session Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load User data
        TextView txtUsername = findViewById(R.id.txtUsername);
        txtUsername.setText("Hi " + sharedPreferences.getString("userName", null) + " !");

        TextView txtCQpoints = findViewById(R.id.txtCQPoints);
        txtCQpoints.setText("Your CQ Points: " + sharedPreferences.getInt("cqPoints", 0) );
    }
    //==================  Refresh MainSection data - END  ==================

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                QrReader qr = new QrReader(this);
                qr.readCode();
                break;

            case R.id.btnMap:
                Toast.makeText(this, "Map Button Pressed.... go to map! ", Toast.LENGTH_LONG).show();
                break;
        }
    }

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

                    refreshData();
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
}