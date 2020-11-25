package com.illipronti.cityquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;


public class QuestsRecyclerViewAdapter extends RecyclerView.Adapter<QuestsRecyclerViewAdapter.ViewHolder>{

    private final int resourceID;
    private Context context;
    private ArrayList questList;
    private ArrayList user_completed_quests;
    private ArrayList user_inprogress_quests;

    public QuestsRecyclerViewAdapter(int resourceID, Context c) {
        this.resourceID = resourceID;
        this.context    = c;
        this.questList  = new ArrayList( context.getSharedPreferences("quests", 0).getStringSet("quest_list", null) );
        this.user_completed_quests  = new ArrayList( context.getSharedPreferences("user_session", 0).getStringSet("completed_quests", null) );
        this.user_inprogress_quests = new ArrayList( context.getSharedPreferences("user_session", 0).getStringSet("inprogress_quests", null) );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resourceID, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ArrayList item = (ArrayList) questList.get(position);

        holder.position = position;
        holder.rvListItemTitle.setText(item.get(1).toString());
        holder.rvListItemDescription.setText(item.get(2).toString());

        Long questId = Long.valueOf(item.get(0).toString());

        // Set User Mission Status Images
        if(user_completed_quests.contains(questId)){
            Picasso.get().load( context.getString(R.string.ico_quest_completed) ).into(holder.imgStatus);
        }
        else if (user_inprogress_quests.contains(questId)){
            Picasso.get().load( context.getString(R.string.ico_quest_inprogress) ).into(holder.imgStatus);
        }
        else {
            Picasso.get().load( context.getString(R.string.ico_new_quest) ).into(holder.imgStatus);
        }

        // Set Mission Image
        if (item.get(3).toString().isEmpty()) {
            holder.imgListItemImage.setVisibility(View.GONE);
        } else {
            holder.imgListItemImage.setVisibility(View.VISIBLE);
            Picasso.get().load(item.get(3).toString()).into(holder.imgListItemImage);
        }
    }

    @Override
    public int getItemCount() {
        return questList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView rvListItemDescription;
        TextView rvListItemTitle;
        ImageView imgListItemImage;
        ImageView imgStatus;
        int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rvListItemTitle = itemView.findViewById(R.id.rvListItemTitle);
            rvListItemDescription = itemView.findViewById(R.id.rvListItemDescription);
            imgListItemImage = itemView.findViewById(R.id.imgListItemImage);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            position = -1;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            QuestManager qm = new QuestManager(context);
            ArrayList item = (ArrayList) questList.get(position);
            Long questId = Long.valueOf(item.get(0).toString());

            // Cancel quest
            if(user_inprogress_quests.contains(questId)){
                Picasso.get().load( context.getString(R.string.ico_new_quest) ).into(imgStatus);
                Toast.makeText(v.getContext(), "You cancelled this quest" , Toast.LENGTH_SHORT).show();

                user_inprogress_quests.removeAll(Arrays.asList( questId.longValue() ));
                qm.updateUserQuests(user_inprogress_quests);
            }
            // Start new quest
            else if (!user_inprogress_quests.contains(questId) && !user_completed_quests.contains(questId)){
                Picasso.get().load( context.getString(R.string.ico_quest_inprogress) ).into(imgStatus);
                Toast.makeText(v.getContext(), "Quest started!" , Toast.LENGTH_SHORT).show();

                user_inprogress_quests.add(questId);
                qm.updateUserQuests(user_inprogress_quests);
            }
            // Quest already completed
            else {
                Toast.makeText(v.getContext(), "Quest is already completed!" , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
