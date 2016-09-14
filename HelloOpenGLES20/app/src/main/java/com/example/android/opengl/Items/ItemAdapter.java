package com.example.android.opengl.Items;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.opengl.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {

    private List<MyItem> item_list;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView item_icon;

        public MyViewHolder(View view) {
            super(view);
            item_icon = (TextView) view.findViewById(R.id.item_text);
        }
    }


    public ItemAdapter(List<MyItem> item_list) {
        this.item_list = item_list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MyItem item = item_list.get(position);
        if(item.value>0){
            holder.item_icon.setText("+" + String.valueOf(item.value));
        }
        else {
            holder.item_icon.setText(String.valueOf(item.value));
        }
        holder.item_icon.setBackgroundResource(item.item_image);
    }

    @Override
    public int getItemCount() {
        return item_list.size();
    }

    public MyItem getItem(int position) {
        return item_list.get(position);
    }
}