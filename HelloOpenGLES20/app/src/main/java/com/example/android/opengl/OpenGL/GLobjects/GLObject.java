package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.OpenGL.MyGLRenderer;
import com.example.android.opengl.SessionData;
import com.example.android.opengl.Vector;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Woess on 03.08.2016.
 */
public class GLObject {
    public String uid;
    public Integer texture;
    protected Date creation_time;
    protected FloatBuffer vertexBuffer;
    protected FloatBuffer textureBuffer;
    protected FloatBuffer normalBuffer;
    protected int mProgram;
    protected boolean isInit = false;
    protected boolean once = true;
    protected float[] light_pos;
    protected float[] gl_coords;
    protected float[] gl_normals;
    protected float[] gl_tex_coords;
    protected int mTexHandle;
    protected float alpha;
    protected int vertexShader;
    protected int fragmentShader;
    protected int mPositionHandle;
    protected int mColorHandle;
    protected int mMVPMatrixHandle;
    protected int mLightHandle;
    protected int mNormalHandle;
    protected int mTextureUniformHandle;
    public float mPositionX;
    public float mPositionY;
    public float mPositionZ;
    protected int mAlphaHandle;
    protected int mMMatrixHandle;
    public ArrayList<Node> corners = new ArrayList<>();
    float color[];
    protected float[] mVPMatrix;
    protected float[] mModelMatrix;
    protected float[] mMVMatrix;
    static final int COORDS_PER_VERTEX = 3;
    static final int TEX_COORDS_PER_VERTEX = 2;
    static final int vertexStride = COORDS_PER_VERTEX * 4;
    public LatLng relativePoint;
    protected int vertexCount;
    protected String vertex_shader =
            "uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.
                    + "uniform vec4 a_Color;        \n"		// Per-vertex color information we will pass in.
                    + "attribute vec2 vTexture;         \n"
                    + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
                    + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
                    + "varying vec2 f_texCoord;         \n"
                    + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
                    + "varying vec3 v_Position;       \n"		// This will be passed into the fragment shader.
                    + "varying vec3 v_Normal;         \n"		// This will be passed into the fragment shader.
                    + "void main()                    \n" 	// The entry point for our vertex shader.
                    + "{                              \n"
                    + "   v_Color = a_Color;                                       \n"
                    + "  gl_Position = u_MVPMatrix * a_Position;                            \n"
                    + "  f_texCoord = vTexture;                                              \n"
                    + "  v_Normal = vec3(u_MVMatrix * vec4(normalize(a_Normal), 0.0));      \n"
                    + "  v_Position = vec3(u_MVMatrix * a_Position);             \n"
                    + "}                                                                     \n";



    protected String fragment_shader =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
                    + "uniform float u_Alpha;       \n"	    // The position of the light in eye space.
                    // precision in the fragment shader.
                    + "uniform sampler2D s_texture;     \n"
                    + "varying vec2 f_texCoord;         \n"
                    + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the
                    + "varying vec3 v_Position;		\n"		// Interpolated position for this fragment.
                    + "varying vec3 v_Normal;         \n"		// Interpolated normal for this fragment
                    + "void main()                    \n"		// The entry point for our fragment shader.
                    + "{                              \n"
                    + "   float distance = length(u_LightPos - v_Position);                  \n"
                    + "   vec3 lightVector = normalize(u_LightPos - v_Position);             \n"
                    + "   float diffuse = max(dot(v_Normal, lightVector), 0.1);              \n"
                    + "   diffuse = diffuse * (40.0 / (1.0 + (0.25 * distance * distance)));  \n"
                    + "	  vec4 text = texture2D(s_texture, f_texCoord); \n"
                    //               + "	  vec4 col = v_Color  \n"
                    //             + "   gl_FragColor = vec4(v_Color[0]* diffuse * 10.0,v_Color[1]* diffuse * 10.0,v_Color[2]* diffuse * 10.0, u_Alpha);     \n"
                    + "   gl_FragColor = vec4(v_Color[0]*text.r * diffuse,v_Color[1]*text.g * diffuse ,v_Color[2]*text.b * diffuse, u_Alpha);     \n"		// Pass the color directly through the pipeline.
                    + "}                              \n";


    public GLObject() {

    }

    public Date getCreationTime() {
        return creation_time;
    }


    protected float centrified(float[] coords, int xyz){
        float biggest = -10000.0f;
        float smallest = 10000.0f;
        for(int i=xyz; i<coords.length;i=i+3){
            if(coords[i]>biggest){
                biggest = coords[i];
            }
            if(coords[i]<smallest){
                smallest = coords[i];
            }
        }
        float factor;
        if(smallest<0.0f){
            factor = -(biggest - (biggest-smallest)/2);
        }
        else
        {
            factor = -(biggest - (biggest+smallest)/2);
        }
        return factor;
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



    public void init(){

       makeBufferReady();

        // prepare shaders and OpenGL program
        vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertex_shader);
        fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragment_shader);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        alpha = 1.0f;
        light_pos = new float[]{0.0f, 0.0f, 4.0f, 1.0f};
        color = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        mMVMatrix = new float[16];
        mVPMatrix = new float[16];
        mPositionX = 0.0f;
        mPositionY = 0.0f;
        mPositionZ = 0.0f;

        isInit=true;

    }
    public void draw(float[] vMatrix, float[] pMatrix, LatLng current) {
    }

    public void standard_draw(float[] vMatrix, float[] pMatrix ) {
        float[] view_light = new float[4];
        Matrix.multiplyMV(view_light, 0, vMatrix, 0, light_pos, 0);
        textureBuffer.position(0);
        normalBuffer.position(0);
        vertexBuffer.position(0);

        mLightHandle = GLES20.glGetUniformLocation (mProgram, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        mMMatrixHandle = GLES20.glGetUniformLocation (mProgram, "u_MVMatrix");
        mAlphaHandle = GLES20.glGetUniformLocation (mProgram, "u_Alpha");
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "a_Color");
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(mProgram,   "a_Normal");
        mTexHandle = GLES20.glGetAttribLocation(mProgram,   "vTexture");


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float[] mMVPMatrix = new float[16];

        Matrix.multiplyMM(this.mVPMatrix, 0, pMatrix, 0, vMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVMatrix, 0, vMatrix, 0, mModelMatrix, 0);
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        GLES20.glUniform1i(mTextureUniformHandle, 0);

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

        GLES20.glEnableVertexAttribArray(mTexHandle);
        GLES20.glVertexAttribPointer(
                mTexHandle, TEX_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                8, textureBuffer);

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
        GLES20.glDisableVertexAttribArray(mTexHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        GLES20.glUseProgram(0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }


    protected float[] createNormals(float[] gl_coords){
        float[] normals = new float[gl_coords.length];
        for(int i=0; i<gl_coords.length/9; i=i+1){

            float a[] = {gl_coords[i*9+0], gl_coords[i*9+1], gl_coords[i*9+2]};
            float c[] = {gl_coords[i*9+3], gl_coords[i*9+4], gl_coords[i*9+5]};
            float b[] = {gl_coords[i*9+6], gl_coords[i*9+7], gl_coords[i*9+8]};

            float[] tempab = new float[3];
            Vector.minus(a,b,tempab);
            float[] tempbc = new float[3];
            Vector.minus(b,c,tempbc);
            float[] cr = new float[3];
            Vector.crossProduct(tempab,tempbc,cr);

            normals[i*9+0] = cr[0];
            normals[i*9+1] = cr[1];
            normals[i*9+2] = cr[2];
            normals[i*9+3] = cr[0];
            normals[i*9+4] = cr[1];
            normals[i*9+5] = cr[2];
            normals[i*9+6] = cr[0];
            normals[i*9+7] = cr[1];
            normals[i*9+8] = cr[2];

        }
        return normals;
    }

    protected void makeBufferReady(){
        ByteBuffer texbuffer = ByteBuffer.allocateDirect(
                gl_tex_coords.length * 4);
        texbuffer.order(ByteOrder.nativeOrder());
        textureBuffer = texbuffer.asFloatBuffer();
        textureBuffer.put(gl_tex_coords);

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
    }
}
