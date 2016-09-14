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
package com.example.android.opengl.OpenGL;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.android.opengl.OpenGL.GLobjects.Crate;
import com.example.android.opengl.OpenGL.GLobjects.Eyeball;
import com.example.android.opengl.OpenGL.GLobjects.Floor;
import com.example.android.opengl.OpenGL.GLobjects.GLObject;
import com.example.android.opengl.OpenGL.GLobjects.ObjectHolder;
import com.example.android.opengl.OpenGL.GLobjects.Square;
import com.example.android.opengl.OpenGL.GLobjects.Star;
import com.example.android.opengl.OpenGL.GLobjects.Triangle;
import com.example.android.opengl.R;
import com.example.android.opengl.SessionData;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Triangle mTriangle;
    private Floor floor;
     Square mSquare;
    private int texture_count = 0;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    public Context my_context;
    private int[] textureid = new int[20];
    private float mAngle;
    private Eyeball eye;
    public GL10 currentGL;
    private ObjectLoader obj_loader;
    public LatLng current_gl_location;
    public LatLng start;
    public LatLng end;
    private float traveled = 0.0f;
    private double distance = 1.0f;
    private ObjectHolder star;
    public MyGLRenderer(Context context) {
        super();
        this.my_context = context;
    }

    public void setObjLoader(ObjectLoader obj_loader) {
        this.obj_loader = obj_loader;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //mTriangle = new Triangle();
        //mSquare   = new Square();
        mAngle = 0.0f;
        //atrix.setLookAtM(mViewMatrix, 0, 0, 0, 0.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        floor = new Floor();
        //0, 4, 0, 7f, 0f, 0f, 0f, 0f, 1.0f, 0f);
        Matrix.setLookAtM(mViewMatrix, 0, 4, 0, 8f, 0f, 0f, 0f, 0f, 1.0f, 0f);
        //loadTexture("white_wall");
        textureid[0] = TextureHelper.loadTexture(my_context, R.drawable.white_wall2);
        texture_count++;
        textureid[1] = TextureHelper.loadTexture(my_context, R.drawable.floor3);
        texture_count++;
        textureid[2] = TextureHelper.loadTexture(my_context, R.drawable.crate_destroyable);
        texture_count++;
        textureid[3] = TextureHelper.loadTexture(my_context, R.drawable.eye_texture2);
        texture_count++;
        textureid[4] = TextureHelper.loadTexture(my_context, R.drawable.star);
        texture_count++;
        textureid[5] = TextureHelper.loadTexture(my_context, R.drawable.white_wall2);

        try {
            ObjectHolder objh = obj_loader.loadModel("eye");
            eye = new Eyeball(objh.vertices,objh.texture_coords);
            eye.texture = textureid[3];

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            star = obj_loader.loadModel("star");
            star.texture = textureid[4];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float[] getmProjectionMatrix() {
        return mProjectionMatrix;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if(SessionData.instance().approx_location != null && end!=null) {
            if(SessionData.distance(end,SessionData.instance().approx_location)> 0.00005 ){
                traveled =0.0f;
                start = new LatLng(current_gl_location.latitude,current_gl_location.longitude);
                end = new LatLng(SessionData.instance().approx_location.latitude,SessionData.instance().approx_location.longitude);
                distance = Math.sqrt(Math.pow(end.latitude-start.latitude,2) + Math.pow(end.longitude-start.longitude,2));
            }
            if(traveled<1.0f) {
                traveled = traveled + 0.01f;
            }
            current_gl_location = new LatLng(start.latitude * (1.0f-traveled) + end.latitude * traveled, start.longitude * (1.0f-traveled) + end.longitude * traveled);
            int rotation = (int)((Math.atan2(end.longitude - start.longitude,end.latitude - start.latitude)/ Math.PI) * 180.0)+180;
            float[] mMVPMatrix = new float[16];
            currentGL = unused;
            // Draw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            //GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            float[] test = new float[]{mViewMatrix[0],mViewMatrix[1],mViewMatrix[2]};
            Matrix.translateM(mViewMatrix,0,-test[0],-test[1],-test[2]);
            Matrix.rotateM(mViewMatrix, 0, mAngle, 0, 0 , 1.0f);
            Matrix.translateM(mViewMatrix,0,test[0],test[1],test[2]);
            //Matrix.multiplyMM(mViewMatrix, 0, mViewMatrix, 0, test, 0);
            //mAngle = 0.0f;
            // Calculate the projection and view transformation
            Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);



            // Create a rotation for the triangle

            // Use the following code to generate constant rotation.
            // Leave this code out when using TouchEvents.
            // long time = SystemClock.uptimeMillis() % 4000L;
            // float angle = 0.090f * ((int) time);


            // Combine the rotation matrix with the projection and camera view
            // Note that the mMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.

            // Draw triangle
            //mTriangle.draw(mMVPMatrix, mMVMatrix, mProjectionMatrix);



            floor.draw(mViewMatrix, mProjectionMatrix, textureid[1]);
            for (int i = 0; i < SessionData.instance().current_buildings.size(); i++) {
                if(SessionData.instance().current_buildings.get(i).my_glbuilding != null) {
                    //Log.i("TESTBUILDING ID","i "+i+" "+ SessionData.instance().current_buildings.get(i).getId()+"");
                    SessionData.instance().current_buildings.get(i).my_glbuilding.draw(mViewMatrix, mProjectionMatrix, textureid[0], current_gl_location);
                }
            }
            for (int i = 0; i < SessionData.instance().current_streets.size(); i++) {
                //Log.i("TESTSTREET ID","size "+SessionData.instance().current_streets.size());

                if(SessionData.instance().current_streets.get(i).my_glstreet != null) {
                    //Log.i("TESTSTREET ID","i "+i+" "+ SessionData.instance().current_streets.get(i).getId()+"");
                    SessionData.instance().current_streets.get(i).my_glstreet.draw(mViewMatrix, mProjectionMatrix, current_gl_location);
                }
            }
            for (Map.Entry<String, GLObject> entry : SessionData.instance().current_things.entrySet())
            {
                GLObject thing = entry.getValue();
                if(thing != null) {
                    if(thing.getCoordinates() == null && thing instanceof Star){
                        ((Star) thing).setGLCoords(star.vertices,star.texture_coords,star.normals, star.texture);
                    }
                    if(thing instanceof Crate && thing.texture ==null){
                        thing.texture = textureid[2];
                    }
                    thing.draw(mViewMatrix, mProjectionMatrix, current_gl_location);
                }
            }

            if(eye != null){
                eye.draw(mViewMatrix, mProjectionMatrix,rotation,current_gl_location);
            }
        }
        // Draw square

    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //projection_matrix = perspective(45.0f, (float)this->screen_width/(float)this->screen_height, 0.1f, 100.0f);
        Matrix.perspectiveM(mProjectionMatrix,0,45.0f,ratio,0.1f,100.0f);
        Matrix.rotateM(mProjectionMatrix, 0, 90, 0.0f,0.0f,1.0f );

        Matrix.scaleM(mProjectionMatrix,0,-1.0f,1.0f,1.0f);

        //Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] != 1) { // 1 is normal output
            if (type == GLES20.GL_VERTEX_SHADER) {
                System.out.println("vertex -- ");
            }
            else if (type == GLES20.GL_FRAGMENT_SHADER)  {
                System.out.println("fragment -- ");
            }
            System.out.println(type + " error " + compileStatus[0]);
            if (compileStatus[0] == GLES20.GL_TRUE) System.out.println("true");
        }

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    public Triangle getTriangle() {
        return mTriangle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }



    /*void Scene::refreshRotation(){
        if(!((current_point.latitude==0 && current_point.longitude==0) || (next_point.latitude==0 && next_point.longitude == 0))){
            double x2 = 1;
            double y2 = 0;
            double x1 = next_point.latitude - current_point.latitude;
            double y1 = next_point.longitude - current_point.longitude;
            double length1 = sqrt(((x1 * x1) + (y1 * y1)));
            double length2 = sqrt(((x2 * x2) + (y2 * y2)));
            double scalar = (double)(x1 * x2) + (y1 * y2);
            double rot = acos(scalar / (length1 * length2));
            if(next_point.longitude>current_point.longitude){
                rot = -rot;
            }
            rotation = (float)rot;
        }
    }*/
}