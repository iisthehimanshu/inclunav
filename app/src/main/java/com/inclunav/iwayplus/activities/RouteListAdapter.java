package com.inclunav.iwayplus.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.inclunav.iwayplus.R;


public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder>{
    private RouteListData[] listdata;

    // RecyclerView recyclerView;
    public RouteListAdapter(RouteListData[] listdata) {
        this.listdata = listdata;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.activity_route_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(listdata[position].getDescription());
        holder.imageView.setImageResource(listdata[position].getImgId());
        holder.textView2.setText(listdata[position].getImgId());
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView, textView2;
        public ViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageRoute);
            this.textView = (TextView) itemView.findViewById(R.id.routeText);
            this.textView2 = (TextView) itemView.findViewById(R.id.routeTime);
        }
    }
}