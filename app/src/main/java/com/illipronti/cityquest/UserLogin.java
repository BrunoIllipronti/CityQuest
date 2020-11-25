package com.illipronti.cityquest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UserLogin extends AppCompatActivity implements View.OnClickListener {

    private final int USER_STATUS_DISCONNECTED = 0;
    private final int USER_STATUS_CONNECTED = 1;
    private final int USER_STATUS_LOGOUT = 9;
    private final int STANDARD_REQUEST_CODE = 0;
    private static final int RC_SIGN_IN = 9001;

    private SharedPreferences.Editor editor;
    private GoogleSignInClient mGoogleSignInClient;

    private EditText editTxtUser;
    private EditText editTxtPw;

    @SuppressLint({"CommitPrefEdits", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Logo Image Setup
        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.logo);

        // Standard Login EditText setup
        editTxtUser = findViewById(R.id.editTextTextEmailAddress);
        editTxtPw = findViewById(R.id.editTextTextPassword);

        // Google SignIn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the Google Sign-in button.
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(this);

        // Set the sign-out button.
        Button btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);

        // Load User Session Shared Preference
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // If log out function from main section was activated - Sign user off the app
        // If user previously connected - Go straight to Main Section
        int user_status = sharedPreferences.getInt("isLogged",USER_STATUS_DISCONNECTED);
        if (user_status == USER_STATUS_LOGOUT){
            signOut("GOOGLE_AUTH");
        }
        else if (user_status == USER_STATUS_CONNECTED){
            enterMainSection();
        }

        // Load Quest List Asynchronously
        QuestManager qm = new QuestManager(this);
        qm.selectQuests();

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGoogleSignIn:
                signIn();
                break;

            case R.id.btnSignIn:
                validateStandardAuth();
                break;

            case R.id.txtSignup:
                Log.d("BRUNO", "User SignUp!");
                Intent sign_up_intent = new Intent(UserLogin.this, UserSignUp.class);
                startActivityForResult(sign_up_intent, STANDARD_REQUEST_CODE);
                break;

            case R.id.txtForgotPw:
                Log.d("BRUNO", "User Forgot Password!");
                Intent forgot_pw_intent = new Intent(UserLogin.this, UserForgotPassword.class);
                startActivityForResult(forgot_pw_intent, STANDARD_REQUEST_CODE);
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Log.d("BRUNO", " Google SignIn: " + account.getEmail() );

            // Check if Google User exists in Firebase
            UserManager um = new UserManager(this, 2, account.getEmail());
            um.username = account.getDisplayName();
            um.password = "google";
            um.manageUser();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason - Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("BRUNO", "Auth Error: " + e.getStatusCode() );
            Toast.makeText(this, "Error when authenticating: " + e.getStatusCode() , Toast.LENGTH_SHORT).show();
        }
    }


    private void signOut(String auth_method) {
        if (auth_method.equals("STANDARD_AUTH")){
            editor.putInt("isLogged",USER_STATUS_DISCONNECTED);
            editor.apply();
        }
        else if (auth_method.equals("GOOGLE_AUTH")){
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("BRUNO", "Sign Out - Google Account Disconnected from app");
                        }
                    });

            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("BRUNO", "Revoke - Google Account Disconnected from app");
                        }
                    });

            editor.putInt("isLogged",USER_STATUS_DISCONNECTED);
            editor.apply();
        }
    }

    private void validateStandardAuth(){
        String user = editTxtUser.getText().toString();
        String pw   = editTxtPw.getText().toString();

        // If user match character validation - UserManager handle authentication
        if (Patterns.EMAIL_ADDRESS.matcher(user).matches() && !TextUtils.isEmpty(pw)){
            UserManager um = new UserManager(this, 3, user);
            um.password = pw;
            um.manageUser();
        }
        else {
            if(TextUtils.isEmpty(user) || !Patterns.EMAIL_ADDRESS.matcher(user).matches()){
                editTxtUser.setError("Email invalid");
            }
            if (TextUtils.isEmpty(pw)){
                editTxtPw.setError("Password can not be blank");
            }
        }
    }

    private void enterMainSection(){
        Intent intent = new Intent(UserLogin.this, MainSection.class);
        startActivityForResult(intent, STANDARD_REQUEST_CODE);
        finish();
    }

}