package com.example.android.opengl.Items;

import android.util.Log;

import com.example.android.opengl.R;

import java.util.Random;

/**
 * Created by Woess on 07.09.2016.
 */
public class StarItem extends MyItem {
    public StarItem() {
        Random rn = new Random();
        item_type = "STAR";
        value  = rn.nextInt(2)+1;
        item_image = R.drawable.star_item;
        color = "x";
        item_id = item_type+value+color;
        Log.i("StarItem", value+"");
    }

    public StarItem(String item_type, int value, String color, int count) {
        super(item_type, value, color, count);
        item_image = R.drawable.star_item;
    }
}