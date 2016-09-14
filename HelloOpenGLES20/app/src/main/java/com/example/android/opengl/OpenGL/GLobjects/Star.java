package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.Matrix;

import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.SessionData;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by Woess on 03.08.2016.
 */
public class Star extends GLObject {
    int rotation = 0;
    private boolean fading = false;
    private float extra_rotation = 50.0f;

    public Star(String uid, double lat, double longi, Date creation_time) {
        this.uid = uid;
        this.relativePoint = new LatLng(lat,longi);
        this.creation_time = creation_time;

    }

    public float[] getModelMatrix(){
        return mModelMatrix;
    }

    public void setGLCoords(float[] coords, float[] uvs, float[] normals, int texture){
        gl_coords = new float[coords.length];
        float factor = centrified(coords,0);
        for(int i=0; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
        }
        factor = centrified(coords,1);
        for(int i=1; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
        }
        factor = centrified(coords,2);
        for(int i=2; i<coords.length;i=i+3){
            gl_coords[i] = coords[i] + factor;
        }
        gl_tex_coords = uvs.clone();
        vertexCount = gl_coords.length / COORDS_PER_VERTEX;
        gl_normals = normals.clone();
        this.texture = texture;
    }

    public float[] getCoordinates() {
        return gl_coords;
    }

    public float[] getMVMatrix() {
        return mMVMatrix;
    }


    public void setFading(){
        fading = true;
    }

    public boolean isFading() {
        return fading;
    }

    @Override
    public void draw(float[] vMatrix, float[] pMatrix, LatLng current) {

        if(!isInit) {
            init();
            alpha = 0.0f;
        }

        mPositionX = (float) (SessionData.GPS_FACTOR_LAT*(relativePoint.latitude - current.latitude));
        mPositionY = (float) (SessionData.GPS_FACTOR_LONG*(relativePoint.longitude - current.longitude));

        mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};
        rotation = rotation +1;
        if(rotation > 360){
            rotation = 0;
        }

        Matrix.translateM(mModelMatrix,0, mPositionX,mPositionY,mPositionZ);
        Matrix.rotateM(mModelMatrix, 0, rotation, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.scaleM(mModelMatrix,0,0.02f,0.02f,0.02f);


        //float[] Mcorner = new float[4];
        //Matrix.multiplyMV(Mcorner, 0, mModelMatrix, 0,  new float[]{0.0f,0.0f,0.0f,1.0f}, 0);
        Node center = new Node(relativePoint.latitude,relativePoint.longitude);
        if(once){
            for (int i = 0; i < SessionData.instance().current_buildings.size(); i++) {
                Building b = SessionData.instance().current_buildings.get(i);
                if(b.my_glbuilding!=null && b.my_glbuilding.contains(center)){
                    mPositionZ = b.height + 1.00f;
                    break;
                }
                else{
                    mPositionZ = 1.00f;
                }

            }
            once = false;
        }

       standard_draw(vMatrix,pMatrix);

        if(fading){
            alpha = alpha - 0.01f;
            extra_rotation = extra_rotation - 0.5f;
            if (extra_rotation > 0){
                rotation = rotation + (int)extra_rotation;
            }
            if(alpha < 0.0f){
                SessionData.instance().current_things.remove(uid);
            }
        }
        else{
            if(alpha<1.0f){
                alpha = alpha + 0.01f;
            }
        }
    }


}
