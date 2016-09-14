package com.example.android.opengl.Items;

import com.example.android.opengl.R;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Woess on 29.08.2016.
 */
public class ColorCube extends MyItem {
    public ColorCube() {

        Random rn = new Random();
        int rand_color = rn.nextInt(3);
        switch (rand_color){
            case 0:
                color = "r";
                break;
            case 1:
                color = "g";
                break;
            case 2:
                color = "b";
                break;
        }
        item_type = "COLORCUBE";
        int rand_value = rn.nextInt(3);
        value = (rand_value+1)*10;
        int rand_minus = rn.nextInt(2)*2 - 1; // -1 or +1
        value = value * rand_minus;
        if(color.equals("r")){
            item_image = R.drawable.red_shape;
        }
        else if(color.equals("g")){
            item_image = R.drawable.green_shape;
        }
        else if(color.equals("b")){
            item_image = R.drawable.blue_shape;
        }
        item_id = item_type+value+color;

    }

    public ColorCube(String item_type, int value, String color, int count) {
        super(item_type, value, color, count);
        if(color.equals("r")){
            item_image = R.drawable.red_shape;
        }
        else if(color.equals("g")){
            item_image = R.drawable.green_shape;
        }
        else if(color.equals("b")){
            item_image = R.drawable.blue_shape;
        }
        item_id = item_type+value+color;

    }
}
