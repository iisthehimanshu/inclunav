package com.inclunav.iwayplus.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.inclunav.iwayplus.R;

import java.util.ArrayList;


public class DropDownListAdapter extends RecyclerView.Adapter<DropDownListAdapter.ViewHolder>{
//    private DropDownListData[] listdata;

    //Creating an arraylist of POJO objects
    private ArrayList<DropDownListData> list_members=new ArrayList<>();
    private final LayoutInflater inflater;
    View view;
    ViewHolder holder;
    private Context context;
    private OnItemClickListener listener;

    public DropDownListAdapter(Context context){
        this.context=context;
        inflater=LayoutInflater.from(context);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listeners) {
        listener = listeners;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view=inflater.inflate(R.layout.dropdown_item_list, parent, false);
        holder=new ViewHolder(view, listener);
        return holder;
    }

    //Setting the arraylist
    public void setListContent(ArrayList<DropDownListData> list_members){
        this.list_members=list_members;
        notifyItemRangeChanged(0,list_members.size());
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DropDownListData myListData = list_members.get(position);
        holder.textView1.setText(myListData.getText1());
        holder.textView2.setText(myListData.getText2());
        holder.distText.setText(myListData.getTime() + " km");
    }


    @Override
    public int getItemCount() {
        return list_members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView1, textView2, distText;
        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
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
            textView1 = (TextView) itemView.findViewById(R.id.text1);
            textView2 = (TextView) itemView.findViewById(R.id.text2);
            distText = (TextView) itemView.findViewById(R.id.venue_distance);
        }

    }
}