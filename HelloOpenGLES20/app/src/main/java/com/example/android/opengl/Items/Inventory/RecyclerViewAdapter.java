package com.example.android.opengl.Items.Inventory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.opengl.Items.MyItem;
import com.example.android.opengl.R;
import com.example.android.opengl.SessionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {

    private Context context;

    public RecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory, parent,false);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        ArrayList<MyItem> itemList = new ArrayList<MyItem>(SessionData.instance().inventory.values());
        Collections.sort(itemList, new Comparator<MyItem>() {
            @Override
            public int compare(MyItem item1, MyItem item2)
            {

                return  item2.item_id.compareTo(item1.item_id);
            }
        });
        holder.item_name.setText(itemList.get(position).item_type);
        holder.item_count.setText(String.valueOf(itemList.get(position).count));
        if(itemList.get(position).value != 0){
            holder.item_value.setText(String.valueOf(itemList.get(position).value));
        }
        else{
            holder.item_value.setText("");
        }
        holder.item_value.setBackgroundResource(itemList.get(position).item_image);
    }



    @Override
    public int getItemCount() {
        return SessionData.instance().inventory.size();
    }
}