package com.inclunav.iwayplus.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.inclunav.iwayplus.R;

import java.util.ArrayList;


public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder>{

//    private CategoryListData[] listdata;

    //Creating an arraylist of POJO objects
    private ArrayList<CategoryListData> list_members=new ArrayList<>();

    private LayoutInflater inflater;
    View view;
    ViewHolder holder;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listeners) {
        listener = listeners;
    }

    public CategoryListAdapter(Context context){
        this.context=context;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.activity_recycle_list, parent, false);
        holder = new ViewHolder(view, listener);
        return holder;
    }

    //Setting the arraylist
    public void setListContent(ArrayList<CategoryListData> list_members){
        this.list_members=list_members;
        notifyItemRangeChanged(0,list_members.size());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryListData myListData = list_members.get(position);
        holder.textView.setText(myListData.getDescription());
        holder.imageView.setImageResource(myListData.getImgId());
    }

    @Override
    public int getItemCount() {
        return list_members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public RelativeLayout relativeLayout;
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

            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativeLayout);
        }
    }
}