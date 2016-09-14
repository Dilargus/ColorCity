/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.opengl.GPSManager;
import com.example.android.opengl.Items.ColorCube;
import com.example.android.opengl.Items.Inventory.RecyclerViewAdapter;
import com.example.android.opengl.Items.ItemAdapter;
import com.example.android.opengl.Items.MyCustomLayoutManager;
import com.example.android.opengl.Items.MyItem;
import com.example.android.opengl.Items.RecyclerItemClickListener;
import com.example.android.opengl.Items.StarItem;
import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.OSMManager;
import com.example.android.opengl.OSM.SingleGrid;
import com.example.android.opengl.OpenGL.GLobjects.Crate;
import com.example.android.opengl.OpenGL.GLobjects.GLObject;
import com.example.android.opengl.OpenGL.GLobjects.Star;

import com.example.android.opengl.OpenGL.MyGLSurfaceView;
import com.example.android.opengl.OpenGL.ObjectLoader;
import com.example.android.opengl.R;
import com.example.android.opengl.SessionData;
import com.example.android.opengl.app.AppConfig;
import com.example.android.opengl.app.AppController;
import com.example.android.opengl.helper.SQLiteHandler;
import com.example.android.opengl.helper.SessionManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class OpenGLES20Activity extends Activity {
    protected OnClickHelper ocl = null;
    protected SQLHelper sqlh = null;
    protected SoundHelper sh = null;

    protected GPSManager gps_manager;
    protected SQLiteHandler db;
    protected SessionManager session;
    protected OSMManager osm_manager;
    protected int mysqlcounter = 61;

    protected AlertDialog gps_alert;
    protected AlertDialog game_menu;

    protected MyGLSurfaceView mGLView;

    protected RecyclerView recyclerView;
    protected ItemAdapter mAdapter;
    protected List<MyItem> item_list = new ArrayList<>();
    protected MyCustomLayoutManager mLayoutManager;
    protected int list_cursor = 3;
    protected int timer_counter=0;
    protected int item_selection_timer = 0;
    protected boolean item_selectable = false;

    protected ImageButton rot_left_btn;
    protected ImageButton rot_right_btn;

    protected TextView inventory_headline;
    protected TextView inventory_bottomline;
    protected RecyclerView inventory_view;
    protected RecyclerViewAdapter inventory_adapter;
    protected GridLayoutManager lLayout;
    protected boolean inventory_visible = false;

    @Override
    public void onStart() {
        super.onStart();
        gps_manager.connect();
        gps_manager.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        gps_manager.stopLocationUpdates();
        gps_manager.disconnect();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ocl = new OnClickHelper(this);
        sqlh = new SQLHelper(this);
        sqlh.getServerTime();

        sh = new SoundHelper(this);
        sh.initSound();
        db = new SQLiteHandler(getApplicationContext());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // session manager
        session = new SessionManager(getApplicationContext());
        // Fetching user details from sqlite

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.gps_alert, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialoglayout);
            gps_alert = builder.create();
            gps_alert.show();
        }

        //mGLView = new MyGLSurfaceView(this);
        initBroadcastReceiver();
        gps_manager = new GPSManager(this, LocationRequest.PRIORITY_HIGH_ACCURACY);
        setContentView(R.layout.main);
        //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        mGLView = (MyGLSurfaceView) findViewById(R.id.renderSurface);
        mGLView.getRenderer().setObjLoader(new ObjectLoader(getAssets()));
        //setContentView(mGLView);

        try {
            File dirFiles = this.getBaseContext().getFilesDir();
            for (String strFile : dirFiles.list())
            {

                if(strFile.startsWith("grid")) {
                    FileInputStream fis;
                    fis = openFileInput(strFile);
                    ObjectInputStream is = new ObjectInputStream(fis);
                    SingleGrid grid = (SingleGrid) is.readObject();
                    SessionData.instance().grid.put(strFile, grid);
                    osm_manager.downloaded_list.add(strFile);
                    osm_manager.sendNewSection(grid);
                    is.close();
                    fis.close();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        inventory_bottomline = (TextView) findViewById(R.id.inventory_bottom);
        inventory_headline = (TextView) findViewById(R.id.inventory_title);
        inventory_bottomline.setVisibility(View.GONE);
        inventory_headline.setVisibility(View.GONE);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ocl.setItemChooserClick();

        mAdapter = new ItemAdapter(item_list);
        //RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager = new MyCustomLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);//new LinearLayoutManagerWithSmoothScroller(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setVisibility(View.GONE);

        /*recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/


        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(timer_counter < item_selection_timer){
                                    timer_counter++;
                                    updateList();
                                }
                                else if(timer_counter >= item_selection_timer && timer_counter!= 0){
                                    mLayoutManager.setScrollEnabled(false);
                                    item_selectable = true;
                                }
                            }
                        });

                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
        rot_left_btn = (ImageButton) findViewById(R.id.btn_rotate_left);
        ocl.setRotateLeftClick();

        rot_right_btn = (ImageButton) findViewById(R.id.btn_rotate_right);
        ocl.setRotateRightClick();

        //rowListItem.addAll(SessionData.instance().inventory.values());
        lLayout = new GridLayoutManager(OpenGLES20Activity.this, 6);

        inventory_view = (RecyclerView)findViewById(R.id.inventory_view);
        inventory_view.setVisibility(View.GONE);
        inventory_view.setLayoutManager(lLayout);

        inventory_adapter = new RecyclerViewAdapter(OpenGLES20Activity.this);
        inventory_view.setAdapter(inventory_adapter);
        inventory_adapter.notifyDataSetChanged();


    }



    public void updateList(){

        if(list_cursor>=9+5){
            list_cursor = 0;
            recyclerView.scrollToPosition(list_cursor);
            list_cursor = 3;

        }

        list_cursor++;
        recyclerView.smoothScrollToPosition(list_cursor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        unregisterReceiver(osm_manager);
        if(mGLView!=null) {
            mGLView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        registerReceiver(osm_manager, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        if(mGLView!=null) {
            mGLView.onResume();
        }
    }

    private void initBroadcastReceiver(){
        osm_manager = new OSMManager(this);
    }

    protected void makeNewCrateItems(){
        item_list.clear();
        for(int i=0; i<5+5; i++){
            Random rn = new Random();
            int random_item = rn.nextInt(2);
            MyItem my_item = null;
            if (random_item==0){
                my_item = new ColorCube();
            }
            else{
                my_item = new StarItem();
            }
            item_list.add(my_item);
        }
        item_list.add(0,item_list.get(item_list.size()-1));
        item_list.add(0,item_list.get(item_list.size()-2));
        item_list.add(item_list.get(2));
        item_list.add(item_list.get(3));
        item_list.add(item_list.get(4));
        mAdapter.notifyDataSetChanged();
    }

    public void updateGPS(Location current_location) {
        LatLng cur_loc = new LatLng(current_location.getLatitude(), current_location.getLongitude());
        //Log.i("OSMManager","" +(null != current_location) + " " + (null == SessionData.instance().approx_location) + " " + (SessionData.distance(SessionData.instance().approx_location, cur_loc) > 0.0002));
        //if (null != current_location &&
        //        (null == SessionData.instance().approx_location ||
        //                SessionData.distance(SessionData.instance().approx_location, cur_loc) > 0.0002)) {
            float accuracy = current_location.getAccuracy();
            if (accuracy > SessionData.BAD_ACCURACY) {
                accuracy = SessionData.BAD_ACCURACY;
            }
            int color = (int) (accuracy * 255 / SessionData.BAD_ACCURACY);
            int percent = (int) (accuracy * 100 / SessionData.BAD_ACCURACY);
            float uncertainty = (accuracy * 1 / SessionData.BAD_ACCURACY);


            //TODO JNILibrary.setAccuracy(uncertainty);

            //accuracy_text.setText(String.format("%02d", (int) accuracy));
            //accuracy_circle.setColor(Color.argb(200, color, 255-color, 0));

            lowPassGPS(SessionData.BAD_ACCURACY, percent, cur_loc);

            //TODOJNILibrary.setCurPosition(approx_location.latitude,approx_location.longitude);

            osm_manager.refreshEnvironment(SessionData.instance().approx_location);
            Log.i("gspcounter", "" + mysqlcounter);
            if(mysqlcounter > 40) {

                sqlh.sendGPS();
                mysqlcounter = 0;
            }
            mysqlcounter++;
            Date d2 = getCurrentTime();
            for (Map.Entry<String, GLObject> entry : SessionData.instance().waiting_things.entrySet()){
                GLObject thing = entry.getValue();

                Date d1 = thing.getCreationTime();
                long diff =    d1.getTime() + (long)(SessionData.instance().server_difference*3600000)- d2.getTime();
                Log.i("Cratetimer" ,thing.uid +" " + diff );
                if(diff < 0){
                    SessionData.instance().current_things.put(thing.uid,thing);
                    SessionData.instance().waiting_things.remove(thing.uid);
                }
            }

            //static_map_manager.checkStaticMap(approx_location);
            //map_manager.checkWaypoint(approx_location);

            //if(accuracy < 40.0f){
            //map_manager.setPositionMarker(approx_location);
            //if(zoom){
            //map_manager.moveToCurrentLocation(approx_location);
            //zoom = false;
            //}
            //}

        //}
    }

    private Date getCurrentTime(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(dt);
        Date d2 = null;
        try {

            d2 = sdf.parse(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d2;
    }

    private void lowPassGPS(float accuracy, int percent, LatLng current_location) {
        if (SessionData.instance().old_location != null) {
            LatLng last_save = new LatLng(SessionData.instance().approx_location.latitude, SessionData.instance().approx_location.longitude);
            float value = percent / 100.0f;
            SessionData.instance().approx_location = new LatLng((current_location.latitude * (1.0f - value) + SessionData.instance().old_location.latitude * value),
                    (current_location.longitude * (1.0f - value) + SessionData.instance().old_location.longitude * value));

            SessionData.instance().old_location = last_save;
        } else if (SessionData.instance().approx_location != null) {
            SessionData.instance().old_location = SessionData.instance().approx_location;
            SessionData.instance().approx_location = new LatLng(current_location.latitude, current_location.longitude);
        } else {
            SessionData.instance().approx_location = new LatLng(current_location.latitude, current_location.longitude);
            mGLView.getRenderer().current_gl_location = SessionData.instance().approx_location;
            mGLView.getRenderer().start = SessionData.instance().approx_location;
            mGLView.getRenderer().end = SessionData.instance().approx_location;
        }
    }





    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return ocl.onTouch(e);
    }


    protected void showPopup(final Activity context, Point p, Building building) {
        int popupWidth = 250;
        int popupHeight = 180;

        // Inflate the popup_layout.xml
        FrameLayout viewGroup = (FrameLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.building_popup, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);


        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());
        p.y = p.y - popupWidth / 2;
        p.x = p.x - popupHeight / 2;
        if (p.y + popupWidth / 2 > mGLView.getWidth()) {
            p.y = mGLView.getWidth() - popupWidth;
        }
        if (p.y - popupWidth / 2 < 0) {
            p.y = 0;
        }
        if (p.x + popupHeight / 2 > mGLView.getHeight()) {
            p.x = mGLView.getHeight() - popupHeight;
        }
        if (p.x - popupHeight / 2 < 0) {
            p.x = 0;
        }
        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.y, p.x);
        /*popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                thing_clicked = true;
            }
        });*/
        TextView red_text = (TextView) layout.findViewById(R.id.building_red_number);
        TextView green_text = (TextView) layout.findViewById(R.id.building_green_number);
        TextView blue_text = (TextView) layout.findViewById(R.id.building_blue_number);

    /*ImageView iv_r = (ImageView)layout.findViewById(R.id.building_red);
    GradientDrawable red_circle = (GradientDrawable)iv_r.getDrawable();
    red_circle.setColor(Color.argb(255, 255, 0, 0));

    ImageView iv_g = (ImageView)layout.findViewById(R.id.building_green);
    GradientDrawable green_circle = (GradientDrawable)iv_g.getDrawable();
    green_circle.setColor(Color.argb(255, 0, 255, 0));

    ImageView iv_b = (ImageView)layout.findViewById(R.id.building_blue);
    GradientDrawable blue_circle = (GradientDrawable)iv_b.getDrawable();
    blue_circle.setColor(Color.argb(255, 0, 0, 255));*/

        red_text.setText(String.format("%02d", Math.round(building.r * 100)));
        green_text.setText(String.format("%02d", Math.round(building.g * 100)));
        blue_text.setText(String.format("%02d", Math.round(building.b * 100)));

        // Getting a reference to Close button, and close the popup when clicked.
        Button close = (Button) layout.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

    }

    public void logOut(View v){
        session.setLogin(false);
        db.deleteUsers();
        Intent mStartActivity = new Intent(getApplicationContext(), LoginActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1, mPendingIntent);
        System.exit(0);
    }

    public void showMenu(View v){
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.game_menu, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        game_menu = builder.create();
        game_menu.show();
    }

    public void showInventory(View v){
        if(inventory_visible){
            inventory_bottomline.setVisibility(View.GONE);
            inventory_headline.setVisibility(View.GONE);
            inventory_view.setVisibility(View.GONE);
            mGLView.setVisibility(View.VISIBLE);
            inventory_visible = false;
        }
        else{
            inventory_bottomline.setVisibility(View.VISIBLE);
            inventory_headline.setVisibility(View.VISIBLE);
            inventory_view.setVisibility(View.VISIBLE);
            mGLView.setVisibility(View.GONE);
            inventory_visible = true;
        }
    }

    public void gotoGPSSettings(View v){
        gps_alert.dismiss();
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
    public void exitApp(View v){
        System.exit(0);
    }

    protected void addItem(MyItem item){
        sqlh.addItem(item);
    }
    protected void destroyThing(String id){
        sqlh.destroyThing(id);
    }

}