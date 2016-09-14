package com.example.android.opengl.Items;

import java.util.UUID;

/**
 * Created by Woess on 29.08.2016.
 */
public class MyItem {
    public String item_id;
    public String item_type;
    public int value;
    public String color;
    public int count =1;
    public int item_image;

    public MyItem(String item_type, int value, String color, int count) {

        this.item_type = item_type;
        this.value = value;
        this.color = color;
        this.count = count;
        item_id = item_type+value+color;
    }

    public MyItem() {
    }
}
