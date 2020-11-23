package com.illipronti.cityquest;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class UserForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private EditText editTxtUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_forgot_password);

        // Log Setup
        ImageView logo = (ImageView) findViewById(R.id.imgForgotPw);
        logo.setImageResource(R.drawable.forgot_pw);

        // Standard Login EditText setup
        editTxtUserEmail = findViewById(R.id.txtEmailAddress);

        // Set the Send my Password button
        Button btnSendPw = findViewById(R.id.btnSendMePw);
        btnSendPw.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSendMePw) {
            validateUser();
        }
    }

    private void validateUser(){
        String email = editTxtUserEmail.getText().toString();

        // If email matches character validation - Check if user exists in User Manager class
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            UserManager um = new UserManager(this, 0, email);
            um.manageUser();
        }
        else {
            editTxtUserEmail.setError("Email invalid");
        }
    }
}