package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.Matrix;

import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.SessionData;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Woess on 03.08.2016.
 */
public class Eyeball extends GLObject {
    private int my_rotation = 0;
    private double biggest_lat = 0.0f;
    private double biggest_long = 0.0f;
    private final float SCALE_FACTOR = 0.005f;
    private int counter = 0;
    public Eyeball(float[] coords, float[] uvs) {
        gl_coords = new float[coords.length];
        float factor = centrified(coords,0);
        for(int i=0; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
            if(gl_coords[i]-0.01*SCALE_FACTOR/(double)SessionData.GPS_FACTOR_LAT > biggest_lat){
                biggest_lat = gl_coords[i]-0.01*SCALE_FACTOR/(double)SessionData.GPS_FACTOR_LAT;
            }
        }
        factor = centrified(coords,1);
        for(int i=1; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
            if(gl_coords[i]-0.01*SCALE_FACTOR/(double)SessionData.GPS_FACTOR_LONG > biggest_long){
                biggest_long = gl_coords[i]-0.01*SCALE_FACTOR/(double)SessionData.GPS_FACTOR_LONG;
            }
        }
        factor = centrified(coords,2);
        for(int i=2; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
        }
        gl_tex_coords = uvs.clone();
        vertexCount = gl_coords.length / COORDS_PER_VERTEX;
        gl_normals = createNormals(gl_coords);

    }

    public float[] getModelMatrix(){
        return mModelMatrix;
    }

    public float[] getCoordinates() {
        return gl_coords;
    }

    public float[] getMVMatrix() {
        return mMVMatrix;
    }

    public void draw(float[] vMatrix, float[] pMatrix, int rotation, LatLng current) {
        counter++;
        if(!isInit) {
            init();
            my_rotation = rotation;

        }

        mPositionX = 0.0f;
        mPositionY = 0.0f;
        mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};


        if(rotation>my_rotation){
            my_rotation = my_rotation + 2;
        }
        else if(rotation<my_rotation){
            my_rotation = my_rotation - 2;
        }
        if(Math.abs(rotation-my_rotation)<2.0){
            my_rotation = rotation;
        }
        Matrix.rotateM(mModelMatrix, 0, (float)my_rotation, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(mModelMatrix,0,SCALE_FACTOR,SCALE_FACTOR,SCALE_FACTOR);

        float[] Mcorner = new float[4];
        Matrix.multiplyMV(Mcorner, 0, mModelMatrix, 0,  new float[]{0.0f,0.0f,0.0f,1.0f}, 0);
        if(counter > 50) {
            corners.clear();
            corners.add(new Node(current.latitude + biggest_lat, current.longitude + biggest_long));
            corners.add(new Node(current.latitude - biggest_lat, current.longitude + biggest_long));
            corners.add(new Node(current.latitude - biggest_lat, current.longitude - biggest_long));
            corners.add(new Node(current.latitude + biggest_lat, current.longitude - biggest_long));
            loop:
            for (int a = 0; a < corners.size(); a++) {
                for (int i = 0; i < SessionData.instance().current_buildings.size(); i++) {
                    Building b = SessionData.instance().current_buildings.get(i);
                    if (b.my_glbuilding != null && b.my_glbuilding.contains(corners.get(a))) {
                        mPositionZ = b.height + 1.50f;
                        break loop;
                    } else {
                        mPositionZ = 1.50f;
                    }

                }
            }
            counter = 0;
        }
        float[] translationM =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                mPositionX,mPositionY,mPositionZ,1.0f};
        Matrix.multiplyMM(mModelMatrix,0,translationM,0,mModelMatrix,0);
       standard_draw(vMatrix,pMatrix);
    }


}
