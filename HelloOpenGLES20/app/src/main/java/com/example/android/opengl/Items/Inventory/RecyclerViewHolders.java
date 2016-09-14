package com.example.android.opengl.Items.Inventory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.opengl.R;

public class RecyclerViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView item_name;
    public TextView item_value;
    public TextView item_count;

    public RecyclerViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        item_name = (TextView)itemView.findViewById(R.id.item_text);
        item_value = (TextView)itemView.findViewById(R.id.item_value);
        item_count = (TextView)itemView.findViewById(R.id.item_count);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), "Clicked Country Position = " + getPosition(), Toast.LENGTH_SHORT).show();
    }
}
