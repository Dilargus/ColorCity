package com.example.android.opengl.activity;

import android.graphics.Point;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.android.opengl.Items.ColorCube;
import com.example.android.opengl.Items.MyItem;
import com.example.android.opengl.Items.RecyclerItemClickListener;
import com.example.android.opengl.Items.StarItem;
import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OpenGL.GLobjects.Crate;
import com.example.android.opengl.OpenGL.GLobjects.GLBuilding;
import com.example.android.opengl.OpenGL.GLobjects.GLObject;
import com.example.android.opengl.OpenGL.GLobjects.Star;
import com.example.android.opengl.OpenGL.GLobjects.Triangle;
import com.example.android.opengl.OpenGL.Ray;
import com.example.android.opengl.SessionData;

import java.util.Map;
import java.util.Random;

public class OnClickHelper {
    private OpenGLES20Activity act;
    protected final float SCROLL_THRESHOLD = 10;
    protected float mDownX;
    protected float mDownY;
    protected Handler handler;
    protected static final int INVALID_POINTER_ID = -1;
    protected int active_pointer = INVALID_POINTER_ID;


    public OnClickHelper(OpenGLES20Activity activity) {
        this.act = activity;
        handler = new Handler();
    }
    public boolean onTouch(MotionEvent e) {
        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            /*case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                dx = dx * -1;
                // reverse direction of rotation above the mid-line
                /*if (y > getHeight() / 2) {

                }

                // reverse direction of rotation to left of the mid-line
                if (x > getWidth() / 2) {
                    dy = dy * -1;
                }

                mRenderer.myClick(
                        mRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));  // = 180.0f / 320
                //requestRender();
                mPreviousX = x;
                mPreviousY = y;
                break;
*/
            case MotionEvent.ACTION_UP:
                //mGLView.getRenderer().setAngle(0.0f);
                active_pointer = INVALID_POINTER_ID;
                handler.removeCallbacks(mLongPressed);
                break;

            case MotionEvent.ACTION_DOWN:
                active_pointer = e.getPointerId(0);
                mDownX = e.getX();
                mDownY = e.getY();
                handler.postDelayed(mLongPressed, 800);
                new LongOperation().execute(new MyTaskParams(mDownX, mDownY, act.mGLView.getHeight(), act.mGLView.getWidth(), act.mGLView.getRenderer().getmProjectionMatrix()));
                break;
            case MotionEvent.ACTION_MOVE:
                if ((Math.abs(mDownX - e.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - e.getY()) > SCROLL_THRESHOLD)) {
                    handler.removeCallbacks(mLongPressed);
                }
                break;

            /*case MotionEvent.ACTION_POINTER_UP:
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = e.getPointerId(pointerIndex);
                if (pointerId == active_pointer) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    x = e.getX(newPointerIndex);
                    y = e.getY(newPointerIndex);
                    myClick(x,y);
                    active_pointer = e.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                active_pointer = e.getPointerId(e.getActionIndex());
                int pointer_index = e.findPointerIndex(active_pointer);
                x = e.getX(pointer_index);
                y = e.getY(pointer_index);
                myClick(x,y);
                break;

            case MotionEvent.ACTION_CANCEL: {
                active_pointer = INVALID_POINTER_ID;
                break;
            }*/


        }




        return true;
    }

    Runnable mLongPressed = new Runnable() {
        public void run() {
            new clickBuilding().execute(new MyTaskParams(mDownX, mDownY, act.mGLView.getHeight(), act.mGLView.getWidth(), act.mGLView.getRenderer().getmProjectionMatrix()));
        }
    };

    private static class MyTaskParams {
        float x;
        float y;
        int height;
        int width;
        float[] projection_matrix;

        public MyTaskParams(float x, float y, int height, int width, float[] projection_matrix) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.projection_matrix = projection_matrix;
        }
    }
    private static class ClickResult {
        float[] intpoint;
        GLObject hit;
        int type;

        public ClickResult(float[] intpoint, GLObject hit, int type) {
            this.intpoint = intpoint;
            this.hit = hit;
            this.type = type;
        }
    }

    private class LongOperation extends AsyncTask<MyTaskParams, Void, ClickResult> {

        @Override
        protected ClickResult doInBackground(MyTaskParams... params) {
            int width = params[0].width;
            int height = params[0].height;
            float[] projection_matrix = params[0].projection_matrix;
            float x = params[0].x;
            float y = params[0].y;
            float[] converted = new float[9];
            float[] resultVector = new float[4];
            float[] inputVector = new float[4];
            float[] intpoint = new float[3];
            inputVector[3] = 1;
            for (Map.Entry<String, GLObject> entry : SessionData.instance().current_things.entrySet())
            {
                GLObject thing = entry.getValue();
                if(thing != null && thing.getCoordinates()!= null && thing.getMVMatrix()!= null) {

                    for (int s = 0; s < thing.getCoordinates().length; s = s + 9) {
                        float[] triangleCoords = new float[]{
                                thing.getCoordinates()[s + 0], thing.getCoordinates()[s + 1], thing.getCoordinates()[s + 2],
                                thing.getCoordinates()[s + 3], thing.getCoordinates()[s + 4], thing.getCoordinates()[s + 5],
                                thing.getCoordinates()[s + 6], thing.getCoordinates()[s + 7], thing.getCoordinates()[s + 8],
                        };

                        for (int i = 0; i < 9; i = i + 3) {
                            inputVector[0] = triangleCoords[i];
                            inputVector[1] = triangleCoords[i + 1];
                            inputVector[2] = triangleCoords[i + 2];
                            Matrix.multiplyMV(resultVector, 0, thing.getMVMatrix(), 0, inputVector, 0);
                            converted[i] = resultVector[0] / resultVector[3];
                            converted[i + 1] = resultVector[1] / resultVector[3];
                            converted[i + 2] = resultVector[2] / resultVector[3];

                        }
                        Ray ray = new Ray(width, height, x, y, thing.getMVMatrix(), projection_matrix);
                        int a = Triangle.intersectRayAndTriangle(ray, converted, intpoint);

                        if (thing instanceof Crate) {
                            Crate crate = (Crate) thing;
                            if(a == 1 && !crate.isFading()) {
                                //recyclerView.setVisibility(View.VISIBLE);
                                //addInfo((intpoint[0]/(-intpoint[2])+0.413f)*(float)mGLView.getHeight()/0.826f, (intpoint[1]/(-intpoint[2])*(-1.0f)+0.60f)*(float)mGLView.getWidth()/1.2f);
                                Log.i("COORDTEST", "x: " + intpoint[0] + " y: " + intpoint[1] + " z: " + intpoint[2]);
                                Log.i("COORDTEST", "x: " + intpoint[0] / (-intpoint[2]) + " y: " + intpoint[1] / (-intpoint[2]));

                                crate.damagedSide();
                                boolean destroyed = crate.damagedSide();

                                if (destroyed) {

                                    Random rn = new Random();
                                    int sound = rn.nextInt(2);
                                    Log.i("SOUND", "destroy rand " + sound);
                                    act.timer_counter = 0;
                                    act.item_selection_timer = rn.nextInt(10) + 10;
                                    act.mLayoutManager.setScrollEnabled(true);
                                    //MediaPlayer mp;
                                    if (SessionData.instance().sound_loaded) {
                                        switch (sound) {
                                            case 0:
                                                Log.i("SOUND", "destroy1");
                                                SessionData.instance().sounds.play(SessionData.instance().crate_destroy1, 1, 1, 0, 0, 1);
                                                //mp = MediaPlayer.create(getContext(), R.raw.crate_destroy1);
                                                //mp.start();
                                                break;
                                            case 1:
                                                Log.i("SOUND", "destroy2");
                                                SessionData.instance().sounds.play(SessionData.instance().crate_destroy2, 1, 1, 0, 0, 1);
                                                //mp = MediaPlayer.create(getContext(), R.raw.crate_destroy2);
                                                //mp.start();
                                                break;
                                        }
                                    }
                                    crate.setFading();
                                    ColorCube cc = new ColorCube();
                                    act.destroyThing(crate.uid);

                                    return new ClickResult(intpoint, thing, 1);

                                } else {
                                    Random rn = new Random();
                                    int sound = rn.nextInt(2);
                                    //MediaPlayer mp;
                                    if (SessionData.instance().sound_loaded) {
                                        switch (sound) {
                                            case 0:
                                                SessionData.instance().sounds.play(SessionData.instance().crate_hit1, 1, 1, 0, 0, 1);

                                                //mp = MediaPlayer.create(getContext(), R.raw.crate_hit1);
                                                //mp.start();
                                                break;
                                            case 1:
                                                SessionData.instance().sounds.play(SessionData.instance().crate_hit2, 1, 1, 0, 0, 1);
                                                //mp = MediaPlayer.create(getContext(), R.raw.crate_hit2);
                                                //mp.start();
                                                break;
                                        }
                                    }
                                    return new ClickResult(intpoint, thing, 0);
                                }

                            }
                        }
                        else if (thing instanceof Star) {
                            Star star = (Star) thing;
                            if(a == 1 && !star.isFading()) {
                                SessionData.instance().sounds.play(SessionData.instance().star_hit, 1, 1, 0, 0, 1);
                                star.setFading();
                                StarItem si = new StarItem("STAR", 0,"x",1);
                                act.destroyThing(star.uid);
                                act.addItem(si);
                                return new ClickResult(intpoint, thing, 0);
                            }
                        }
                    }
                }
            }

            return new ClickResult(new float[]{0.0f,0.0f,0.0f},null, 0);
        }

        @Override
        protected void onPostExecute(ClickResult result) {
            if(result.hit instanceof Crate && result.type == 1){
                act.makeNewCrateItems();
                act.recyclerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class clickBuilding extends AsyncTask<MyTaskParams, Void, ClickResult> {

        @Override
        protected ClickResult doInBackground(MyTaskParams... params) {
            int width = params[0].width;
            int height = params[0].height;
            float[] projection_matrix = params[0].projection_matrix;
            float x = params[0].x;
            float y = params[0].y;
            float[] converted = new float[9];
            float[] resultVector = new float[4];
            float[] inputVector = new float[4];
            float[] intpoint = new float[3];
            inputVector[3] = 1;
            for (int o = 0; o < SessionData.instance().current_buildings.size(); o++) {
                Building b = SessionData.instance().current_buildings.get(o);
                if (b.my_glbuilding != null && b.my_glbuilding.getCoords() != null && b.my_glbuilding.getmMVMatrix() != null) {
                    for (int s = 0; s < b.my_glbuilding.getCoords().length/2; s = s + 9) {
                        float[] triangleCoords = new float[]{
                                b.my_glbuilding.getCoords()[s + 0], b.my_glbuilding.getCoords()[s + 1], b.my_glbuilding.getCoords()[s + 2],
                                b.my_glbuilding.getCoords()[s + 3], b.my_glbuilding.getCoords()[s + 4], b.my_glbuilding.getCoords()[s + 5],
                                b.my_glbuilding.getCoords()[s + 6], b.my_glbuilding.getCoords()[s + 7], b.my_glbuilding.getCoords()[s + 8],
                        };

                        for (int i = 0; i < 9; i = i + 3) {
                            inputVector[0] = triangleCoords[i];
                            inputVector[1] = triangleCoords[i + 1];
                            inputVector[2] = triangleCoords[i + 2];
                            inputVector[3] = 1;
                            Matrix.multiplyMV(resultVector, 0, b.my_glbuilding.getmMVMatrix(), 0, inputVector, 0);
                            converted[i] = resultVector[0] / resultVector[3];
                            converted[i + 1] = resultVector[1] / resultVector[3];
                            converted[i + 2] = resultVector[2] / resultVector[3];

                        }
                        Ray ray = new Ray(width, height, x, y, b.my_glbuilding.getmMVMatrix(), projection_matrix);
                        int a = Triangle.intersectRayAndTriangle(ray, converted, intpoint);
                        if (a == 1) {

                            //addInfo((intpoint[0]/(-intpoint[2])+0.413f)*(float)mGLView.getHeight()/0.826f, (intpoint[1]/(intpoint[2])+0.59f)*(float)mGLView.getWidth()/1.18f);
                            //addInfo((intpoint[0]/(-intpoint[2])+0.413f)*(float)mGLView.getHeight()/0.826f, (intpoint[1]*(-1.0f)+4.8f)*(float)mGLView.getWidth()/9.6f);
                            /*for (int c = 0; c < SessionData.instance().current_buildings.size(); c++) {

                                Building b2 = SessionData.instance().current_buildings.get(c);
                                if (b2.getId() != b.getId() && b2.my_glbuilding != null) {
                                    b2.my_glbuilding.setColor(new float[]{b2.r, b2.g, b2.b, 1.0f});
                                }
                            }*/
                            //b.my_glbuilding.setColor(Building.BUILDING_CLICKED);

                            return new ClickResult(intpoint, b.my_glbuilding, 0);
                        }
                    }
                }
            }
            return new ClickResult(new float[]{0.0f,0.0f,0.0f},null, 0);
        }

        @Override
        protected void onPostExecute(ClickResult result) {
            if(result.hit != null){
                GLBuilding glb = (GLBuilding) result.hit;
                act.showPopup(act,new Point(Math.round((result.intpoint[0]/(-result.intpoint[2])+0.413f)*(float)act.mGLView.getHeight()/0.826f),Math.round((result.intpoint[1]/(result.intpoint[2])+0.59f)*(float)act.mGLView.getWidth()/1.18f)),glb.my_building);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    protected void setItemChooserClick(){
        act.recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(act.getApplicationContext(), act.recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if(act.item_selectable) {
                            MyItem new_item = act.mAdapter.getItem(position);
                            if(new_item instanceof StarItem){
                                new_item.count = new_item.value;
                                new_item.value = 0;
                            }
                            act.addItem(new_item);

                            act.timer_counter = 0;
                            act.item_selection_timer = 0;
                            act.recyclerView.setVisibility(View.GONE);
                            act.item_selectable = false;
                        }
                    }
                })
        );
    }
    protected void setRotateLeftClick(){
        act.rot_left_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    act.mGLView.getRenderer().setAngle(-1.0f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    act.mGLView.getRenderer().setAngle(0.0f);
                }
                return true;
            }
        });
    }

    protected void setRotateRightClick(){
        act.rot_right_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    act.mGLView.getRenderer().setAngle(1.0f);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    act.mGLView.getRenderer().setAngle(0.0f);
                }
                return true;
            }
        });
    }
}
