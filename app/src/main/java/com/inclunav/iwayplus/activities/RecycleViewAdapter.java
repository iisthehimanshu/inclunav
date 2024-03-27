package com.inclunav.iwayplus.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inclunav.iwayplus.R;

import java.util.ArrayList;

public class RecycleViewAdapter extends
        RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private ArrayList<String> buildingValue;
    private OnItemClickListener listener;

    // Pass in the contact array into the constructor
    public RecycleViewAdapter(ArrayList<String> buildings) {
        buildingValue = buildings;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listeners) {
        listener = listeners;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View recycleView = inflater.inflate(R.layout.activity_recycle_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(recycleView, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String building = buildingValue.get(position);
        TextView textView = holder.nameTextView;
        textView.setText(building);
    }


    @Override
    public int getItemCount() {
        return buildingValue.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;

        public ViewHolder(final View itemView, final OnItemClickListener listener) {
            super(itemView);
            this.nameTextView = (TextView) itemView.findViewById(R.id.text);

            // Attach a click listener to the entire row view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }
}
