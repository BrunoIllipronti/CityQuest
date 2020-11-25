package com.illipronti.cityquest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserManager {

    // 0 (Forgot Pw) / 1 (Create User) / 2 (User Log In - Google) / 3 (User Log In - Standard)
    private int USER_MANAGER_ACTION;

    private final int USER_STATUS_CONNECTED = 1;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String email;
    String username;
    String password;

    public UserManager(Context c, int action, String user_email){
        USER_MANAGER_ACTION = action;
        context             = c;
        email               = user_email;
    }

    public void manageUser(){
        db.collection("users")
                .whereEqualTo("username", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean email_exists = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                email_exists = true;
                                Log.d("BRUNO", document.getId() + " => " + document.getData());

                                // IF Forgot Password
                                if (USER_MANAGER_ACTION == 0){
                                    // Send email Function...
                                    Toast.makeText(context, "Password sent to email " + email, Toast.LENGTH_LONG).show();
                                }
                                // IF Creating New Account
                                else if (USER_MANAGER_ACTION == 1){
                                    Toast.makeText (context, "There is an account registered using this email", Toast. LENGTH_LONG).show();
                                }
                                // IF Authenticating using Standard method (3) or Google Sign In (2)
                                else if (USER_MANAGER_ACTION == 3 || USER_MANAGER_ACTION == 2){
                                    if( password.equals(document.get("password").toString()) || USER_MANAGER_ACTION == 2 ){

                                        String firestoreUserId = document.getId();
                                        Set completed_quests  = new HashSet((ArrayList)document.get("completed_quests"));
                                        Set inprogress_quests = new HashSet((ArrayList)document.get("inprogress_quests"));

                                        SharedPreferences.Editor editor;
                                        SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", 0);
                                        editor = sharedPreferences.edit();
                                        editor.putInt("isLogged",USER_STATUS_CONNECTED);
                                        editor.putString("firestoreUserId",firestoreUserId);
                                        editor.putString("userId",email);
                                        editor.putString("userName",document.get("name").toString());
                                        editor.putInt("cqPoints", ((Long)document.get("score")).intValue() );
                                        editor.putStringSet("completed_quests", completed_quests);
                                        editor.putStringSet("inprogress_quests", inprogress_quests);
                                        editor.apply();

                                        // Authenticate to app MainSection
                                        Toast.makeText (context, "Login Successful !", Toast. LENGTH_LONG).show();
                                        enterUserLogin();
                                    } else {
                                        Toast.makeText (context, "Password Invalid", Toast. LENGTH_LONG).show();
                                    }
                                }
                            }

                            // If no email was found - Forgot Pw (0)  // If account is not registered (3)
                            if (!email_exists && USER_MANAGER_ACTION == 0 || !email_exists && USER_MANAGER_ACTION == 3){
                                Toast.makeText (context, "Account doesn't exist - Please enter a valid email", Toast. LENGTH_LONG).show();
                            }
                            // If no email was found - Create account (1)  // Create google account user if not found (2)
                            else if (!email_exists && USER_MANAGER_ACTION == 1 || !email_exists && USER_MANAGER_ACTION == 2){
                                createUser();

                                // After creating user Standard - Returns to UserLogin
                                if(USER_MANAGER_ACTION == 1) enterUserLogin();
                                // Google SigIn - After creating user for the first time, call ManageUser again to get User data and access Main Section
                                else manageUser();
                            }
                        }
                        // Condition if Firebase gets an error...
                        else {
                            Log.d("BRUNO", "Error getting documents: ", task.getException());
                            Toast.makeText (context, "Something wrong happened - Try it again", Toast. LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void createUser(){

        ArrayList<Integer> empty_num_array = new ArrayList<>();

        // Create a new user with a first, middle, and last name
        Map<String, Object> user = new HashMap<>();
        user.put("epochdate", System.currentTimeMillis());
        user.put("name", username);
        user.put("password", password);
        user.put("username", email);
        user.put("score", 0);
        user.put("completed_quests",empty_num_array);
        user.put("inprogress_quests",empty_num_array);

        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("BRUNO", "DocumentSnapshot added with ID: " + documentReference.getId());
                        Toast.makeText(context, "User Account Created " + email, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("BRUNO", "Something Wrong Happened: " + e);
                        Toast.makeText (context, "Something wrong happened - Try it again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void enterUserLogin(){
        Intent intent = new Intent(context, UserLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
