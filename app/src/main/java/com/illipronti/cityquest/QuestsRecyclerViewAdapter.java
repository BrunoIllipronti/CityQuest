package com.illipronti.cityquest;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class QuestsRecyclerViewAdapter extends RecyclerView.Adapter<QuestsRecyclerViewAdapter.ViewHolder>{

    private final int resourceID;
    String test = "";

    public QuestsRecyclerViewAdapter(int resourceID) {
        this.resourceID = resourceID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resourceID, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = DemoData.ITEMS[position];

        holder.position = position;
        holder.tvListItemDescription.setText(item[0]);
        holder.tvListItemTitle.setText(item[2]);

        if (item[3].isEmpty()) {
            holder.imgListItemImage.setVisibility(View.GONE);
        } else {
            holder.imgListItemImage.setVisibility(View.VISIBLE);
            Picasso.get().load(item[3]).into(holder.imgListItemImage);
        }




        // holder.imgStatus.setVisibility(View.VISIBLE);
        // Picasso.get().load("https://th.bing.com/th/id/OIP.g2ccDaV_aR_CsoFgHDSRjwHaHa?w=180&h=180&c=7&o=5&dpr=1.5&pid=1.7").into(holder.imgStatus);




    }

    @Override
    public int getItemCount() {
        return DemoData.ITEMS.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvListItemDescription;
        TextView tvListItemTitle;
        ImageView imgListItemImage;
        ImageView imgStatus;
        int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListItemDescription = itemView.findViewById(R.id.tvListItemDescription);
            tvListItemTitle = itemView.findViewById(R.id.tvListItemTitle);
            imgListItemImage = itemView.findViewById(R.id.imgListItemImage);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            position = -1;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), DemoData.ITEMS[position][1], Toast.LENGTH_SHORT).show();


            //Toast.makeText(v.getContext(), v.getResources().getString(R.string.ico_quest_completed) , Toast.LENGTH_SHORT).show();

            test = "https://th.bing.com/th/id/OIP.g2ccDaV_aR_CsoFgHDSRjwHaHa?w=180&h=180&c=7&o=5&dpr=1.5&pid=1.7";

            Picasso.get().load(test).into(imgStatus);

            //Intent intent = new Intent(v.getContext(), MainSection.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //v.getContext().startActivity(intent);

        }



        }
}
