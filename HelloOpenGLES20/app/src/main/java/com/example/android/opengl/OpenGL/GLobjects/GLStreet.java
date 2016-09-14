package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.OpenGL.MyGLRenderer;
import com.example.android.opengl.SessionData;
import com.example.android.opengl.OSM.Street;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Woess on 03.08.2016.
 */
public class GLStreet extends  GLObject{

    public Street my_street;


    private final String building_vertex_shader =
                      "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.
                    + "uniform vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
                    + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                    + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
                    + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
                    + "varying vec3 v_Position;       \n"		// This will be passed into the fragment shader.
                    + "varying vec3 v_Normal;         \n"		// This will be passed into the fragment shader.
                    + "void main()                    \n" 	// The entry point for our vertex shader.
                    + "{                              \n"
                    + "   v_Color = a_Color;                                       \n"
                    + "  gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "  v_Normal = vec3(u_MVMatrix * vec4(normalize(a_Normal), 0.0));      \n"
                    + "  v_Position = vec3(u_MVMatrix * a_Position);             \n"
                    + "}                                                                     \n";



    private final String building_fragment_shader =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
                    + "uniform float u_Alpha;       \n"	    // The position of the light in eye space.
                    // precision in the fragment shader.
                    + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                    + "varying vec3 v_Position;		\n"		// Interpolated position for this fragment.
                    + "varying vec3 v_Normal;         \n"		// Interpolated normal for this fragment
                    + "void main()                    \n"		// The entry point for our fragment shader.
                    + "{                              \n"
                    + "   float distance = length(u_LightPos - v_Position);                  \n"
                    + "   vec3 lightVector = normalize(u_LightPos - v_Position);             \n"
                    + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n"
                    + "   diffuse = min(diffuse * (10.0 / (1.0 + (0.25 * distance * distance))), 0.9);  \n"
     //               + "	  vec4 col = v_Color  \n"
                    + "   gl_FragColor = vec4(v_Color[0] * diffuse,v_Color[1] * diffuse,v_Color[2] * diffuse, u_Alpha);     \n"		// Pass the color directly through the pipeline.
                    + "}                              \n";

    public GLStreet(Street street, float[] vertices, float[] normals, LatLng relativePoint) {
        this.my_street = street;
        // initialize vertex byte buffer for shape coordinates
        this.relativePoint = relativePoint;
        gl_coords = new float[vertices.length];
        gl_coords = vertices.clone();
        gl_normals = new float[normals.length];
        gl_normals = normals.clone();

    }

    public float[] getModelMatrix(){
        return this.mModelMatrix;
    }

    public void init(){
        vertexCount = gl_coords.length / COORDS_PER_VERTEX;

        ByteBuffer vertexbuffer = ByteBuffer.allocateDirect(
                gl_coords.length * 4);
        vertexbuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexbuffer.asFloatBuffer();
        vertexBuffer.put(gl_coords);

        ByteBuffer normbuffer = ByteBuffer.allocateDirect(
                gl_normals.length * 4);
        normbuffer.order(ByteOrder.nativeOrder());
        normalBuffer = normbuffer.asFloatBuffer();
        normalBuffer.put(gl_normals);

        // prepare shaders and OpenGL program
        vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, building_vertex_shader);
        fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, building_fragment_shader);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        alpha = 0.5f;
        light_pos = new float[]{0.0f, 0.0f, 4.0f, 1.0f};
        color = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
        mMVMatrix = new float[16];
        mVPMatrix = new float[16];
        isInit=true;

    }

    public void draw(float[] vMatrix, float[] pMatrix, LatLng current) {
        if(!isInit) {
            init();
        }
        float[] view_light = new float[4];
        Matrix.multiplyMV(view_light, 0, vMatrix, 0, light_pos, 0);
        normalBuffer.position(0);
        vertexBuffer.position(0);


        mLightHandle = GLES20.glGetUniformLocation (mProgram, "u_LightPos");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mMMatrixHandle = GLES20.glGetUniformLocation (mProgram, "u_MVMatrix");
        mAlphaHandle = GLES20.glGetUniformLocation (mProgram, "u_Alpha");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "a_Color");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram,   "a_Normal");


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float[] mMVPMatrix = new float[16];
        this.mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};
        float mPositionX = (float) (SessionData.GPS_FACTOR_LAT*(relativePoint.latitude - current.latitude));
        float mPositionY = (float) (SessionData.GPS_FACTOR_LONG*(relativePoint.longitude - current.longitude));
        float mPositionZ = 0.03f;
        //Matrix.setRotateM(this.mModelMatrix, 0, 0.0f, 0, 0, 1.0f);
        Matrix.translateM(this.mModelMatrix,0,this.mModelMatrix,0, mPositionX,mPositionY,mPositionZ);
        /*ArrayList<Node> corners = new ArrayList<Node>();
        for(int i=0; i< gl_coords.length;i=i+3){
            float[] Mcorner = new float[4];
            float[] corner = new float[]{gl_coords[i],gl_coords[i+1],gl_coords[i+2], 0.0f};
            Matrix.multiplyMV(Mcorner, 0, mModelMatrix, 0,  corner, 0);
            corners.add(new Node(Mcorner[0],Mcorner[1]));
        }*/
        Matrix.multiplyMM(this.mVPMatrix, 0, pMatrix, 0, vMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVMatrix, 0, vMatrix, 0, mModelMatrix, 0);

        GLES20.glUseProgram(mProgram);


        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(mNormalHandle);
        GLES20.glVertexAttribPointer(
                mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, normalBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMVMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");
        GLES20.glUniform3fv(mLightHandle,1, view_light,0);
        MyGLRenderer.checkGlError("glUniform3fv");
        GLES20.glUniform1f(mAlphaHandle, alpha);
        MyGLRenderer.checkGlError("glUniform1f");

        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        MyGLRenderer.checkGlError("glUniform4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public boolean contains(Node test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = corners.size() - 1; i < corners.size(); j = i++) {
            if ((corners.get(i).y > test.y) != (corners.get(j).y > test.y) &&
                    (test.x < (corners.get(j).x - corners.get(i).x) * (test.y - corners.get(i).y) / (corners.get(j).y-corners.get(i).y) + corners.get(i).x)) {
                result = !result;
            }
        }
        return result;
    }
}
