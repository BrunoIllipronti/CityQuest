package com.illipronti.cityquest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class UserSignUp extends AppCompatActivity implements View.OnClickListener {

    private EditText editTxtName;
    private EditText editTxtEmail;
    private EditText editTxtPw;
    private EditText editTxtConfirmPw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        // Logo Text Image Setup
        ImageView imgLogoTxt = (ImageView) findViewById(R.id.imgLogoText);
        imgLogoTxt.setImageResource(R.drawable.city_quest_txt);

        // Adventurers Image Setup
        ImageView imgAdventurers = (ImageView) findViewById(R.id.imgAdventurers);
        imgAdventurers.setImageResource(R.drawable.join_us);

        // Edit Text setup
        editTxtName = findViewById(R.id.txtName);
        editTxtEmail = findViewById(R.id.txtEmail);
        editTxtPw = findViewById(R.id.txtPw);
        editTxtConfirmPw = findViewById(R.id.txtConfirmPw);

        // Set the Sign up button
        Button btnSendPw = findViewById(R.id.btnSignUp);
        btnSendPw.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSignUp) {
            validateFields();
        }
    }

    private void validateFields(){
        String name       = editTxtName.getText().toString();
        String email      = editTxtEmail.getText().toString();
        String pw         = editTxtPw.getText().toString();
        String confirm_pw = editTxtConfirmPw.getText().toString();

        // If all fields match validation - Create user using UserManager class
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches() && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(pw) && !TextUtils.isEmpty(confirm_pw) && pw.equals(confirm_pw) ){
            UserManager um = new UserManager(this, 1, email);
            um.username = name;
            um.password = pw;
            um.manageUser();
        }
        else {

            if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                editTxtEmail.setError("Email invalid");
            }
            if (TextUtils.isEmpty(name)){
                editTxtName.setError("Name can not be blank");
            }

            if(!pw.equals(confirm_pw)){
                editTxtConfirmPw.setError("Passwords are not matching");
            }
            else {
                if (TextUtils.isEmpty(pw) || TextUtils.isEmpty(pw)){
                    Log.d("BRUNO", "Error");
                    editTxtPw.setError("Password can not be blank");
                    editTxtConfirmPw.setError("Password can not be blank");
                }
            }
        }

    }
}