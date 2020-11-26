package com.illipronti.cityquest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuestManager implements Serializable {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Set questList = new HashSet();
    private ArrayList arrQuestAttributes;
    private Context context;
    private String userFirebaseId;
    private int userScore;

    public QuestManager(Context c){
        context        = c;
        userFirebaseId = context.getSharedPreferences("user_session", 0).getString("firestoreUserId", null);
        userScore      = context.getSharedPreferences("user_session", 0).getInt("cqPoints", 0);
    }

    public void selectQuests(){
        db.collection("quests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @SuppressLint("ShowToast")
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            arrQuestAttributes = new ArrayList();
                            arrQuestAttributes.add(0, ((Long)document.get("questid")).intValue() );
                            arrQuestAttributes.add(1, document.get("quest").toString() );
                            arrQuestAttributes.add(2, document.get("description").toString() );
                            arrQuestAttributes.add(3, document.get("imagepath").toString() );
                            questList.add(arrQuestAttributes);
                        }

                        // Store Quest list in a shared preference
                        SharedPreferences.Editor editor;
                        SharedPreferences sharedPreferences = context.getSharedPreferences("quests", 0);
                        editor = sharedPreferences.edit();
                        editor.putStringSet("quest_list", questList);
                        editor.apply();
                    }
                }
        });
    }

    // Firebase - User In Progress Quests Update
    public void updateUserQuests(ArrayList user_inprogress_quests){

        Map<String, Object> userQuestsUpdate = new HashMap<>();
        userQuestsUpdate.put("inprogress_quests", user_inprogress_quests );

        db.collection("users")
                .document(userFirebaseId)
                .set(userQuestsUpdate, SetOptions.merge());

        updateQuestSharedPreferences(user_inprogress_quests, 3);
    }

    // Firebase - User Completed Quests Update
    public void updateUserCompletedQuests(ArrayList user_completed_quests){

        Map<String, Object> userQuestsUpdate = new HashMap<>();
        userQuestsUpdate.put("completed_quests", user_completed_quests );
        userQuestsUpdate.put("score", userScore + 100 );

        db.collection("users")
                .document(userFirebaseId)
                .set(userQuestsUpdate, SetOptions.merge());

        updateQuestSharedPreferences(user_completed_quests, 1);
        updateQuestSharedPreferences(user_completed_quests, 2);
    }

    // Shared Preferences - User Completed Quests Update (to update UI in realtime)
    private void updateQuestSharedPreferences(ArrayList temp_array, int obj_type){ // 1 Score - 2 Completed Quests - 3 In Progress Quests

        Set quests_update = new HashSet((ArrayList)temp_array);

        SharedPreferences.Editor editor;
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_session", 0);
        editor = sharedPreferences.edit();

        switch(obj_type){
            case 1:
                int score_update = userScore + 100;
                editor.putInt("cqPoints", score_update );
                break;
            case 2:
                editor.putStringSet("completed_quests", quests_update);
                break;
            case 3:
                editor.putStringSet("inprogress_quests", quests_update);
                break;
        }
        editor.apply();
    }
}
