package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.OpenGL.MyGLRenderer;
import com.example.android.opengl.SessionData;
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
public class Crate extends GLObject {

    private boolean fading = false;
    private boolean[] side_damaged = new boolean[]{false,false,false,false,false,false};
    public float final_zpos = 0.25f;
    private float rotation = 0.0f;
    private final float SCALE = 0.25f;

    public Crate(String uid, double lat, double longi, Date creation_time) {
        this.uid = uid;
        this.relativePoint = new LatLng(lat,longi);
        this.creation_time = creation_time;

        vertex_shader =
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

        fragment_shader =
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
                        + "   diffuse = max(min(diffuse * (10.0 / (1.0 + (0.25 * distance * distance))), 0.9),0.2);  \n"
                        + "	  vec4 text = texture2D(s_texture, f_texCoord); \n"
                        //               + "	  vec4 col = v_Color  \n"
                        //             + "   gl_FragColor = vec4(v_Color[0]* diffuse * 10.0,v_Color[1]* diffuse * 10.0,v_Color[2]* diffuse * 10.0, u_Alpha);     \n"
                        + "   gl_FragColor = vec4(v_Color[0]*text.r * diffuse,v_Color[1]*text.g * diffuse ,v_Color[2]*text.b * diffuse, u_Alpha);     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";
    }

    public float[] getModelMatrix(){
        return mModelMatrix;
    }

    public float[] getCoordinates() {
        return gl_coords;
    }

    public void setFading(){
        fading = true;
    }

    public boolean isFading() {
        return fading;
    }

    public float[] getMVMatrix() {
        return mMVMatrix;
    }
    public boolean damagedSide() {
        Random rn = new Random();
        int side = rn.nextInt(6);
        if(side_damaged[side] == false){
            float x = 0.0f;
            float y = 0.0f;
            int random_damage = rn.nextInt(3);
            if(random_damage == 0){
                x = 0.5f;
                y = 0.0f;
            }else if(random_damage == 1){
                x = 0.0f;
                y = 0.5f;
            }else if(random_damage == 2){
                x = 0.5f;
                y = 0.5f;
            }
            for(int i = 0; i<12; i= i+2){
                gl_tex_coords[12*side+i] = gl_tex_coords[12*side+i] + x;
                gl_tex_coords[12*side+i+1] = gl_tex_coords[12*side+i+1] + y;
            }
            textureBuffer.position(0);
            textureBuffer.put(gl_tex_coords);
            side_damaged[side] = true;
        }
        if(side_damaged[0] && side_damaged[1] && side_damaged[2] && side_damaged[3] && side_damaged[4] && side_damaged[5]){

            return true;
        }
        return false;
    }


    @Override
    public void init(){
        gl_coords = new float[]
                {
                        // Front face
                        -0.25f, 0.25f, 0.25f,
                        -0.25f, -0.25f, 0.25f,
                        0.25f, 0.25f, 0.25f,
                        -0.25f, -0.25f, 0.25f,
                        0.25f, -0.25f, 0.25f,
                        0.25f, 0.25f, 0.25f,

                        // Right face
                        0.25f, 0.25f, 0.25f,
                        0.25f, -0.25f, 0.25f,
                        0.25f, 0.25f, -0.25f,
                        0.25f, -0.25f, 0.25f,
                        0.25f, -0.25f, -0.25f,
                        0.25f, 0.25f, -0.25f,

                        // Back face
                        0.25f, 0.25f, -0.25f,
                        0.25f, -0.25f, -0.25f,
                        -0.25f, 0.25f, -0.25f,
                        0.25f, -0.25f, -0.25f,
                        -0.25f, -0.25f, -0.25f,
                        -0.25f, 0.25f, -0.25f,

                        // Left face
                        -0.25f, 0.25f, -0.25f,
                        -0.25f, -0.25f, -0.25f,
                        -0.25f, 0.25f, 0.25f,
                        -0.25f, -0.25f, -0.25f,
                        -0.25f, -0.25f, 0.25f,
                        -0.25f, 0.25f, 0.25f,

                        // Top face
                        -0.25f, 0.25f, -0.25f,
                        -0.25f, 0.25f, 0.25f,
                        0.25f, 0.25f, -0.25f,
                        -0.25f, 0.25f, 0.25f,
                        0.25f, 0.25f, 0.25f,
                        0.25f, 0.25f, -0.25f,

                        // Bottom face
                        0.25f, -0.25f, -0.25f,
                        0.25f, -0.25f, 0.25f,
                        -0.25f, -0.25f, -0.25f,
                        0.25f, -0.25f, 0.25f,
                        -0.25f, -0.25f, 0.25f,
                        -0.25f, -0.25f, -0.25f,
                };
        vertexCount = gl_coords.length / COORDS_PER_VERTEX;

        gl_tex_coords = new float[]{
                // Front face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                // Right face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                // Back face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                // Left face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                // Top face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                // Bottom face
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f
        };

        gl_normals = new float[]{
                // Front face
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                // Right face
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,

                // Back face
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,

                // Left face
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,

                // Top face
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,

                // Bottom face
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f
        };


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
        Random rn = new Random();
        float add_height = rn.nextFloat();
        mPositionZ = 10.0f + add_height;

        isInit=true;

    }

    @Override
    public void draw(float[] vMatrix, float[] pMatrix, LatLng current) {
        if(!isInit) {
            init();
        }
        float mPositionX = (float) (SessionData.GPS_FACTOR_LAT*(relativePoint.latitude - current.latitude));
        float mPositionY = (float) (SessionData.GPS_FACTOR_LONG*(relativePoint.longitude - current.longitude));
        mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};

        if(mPositionZ > final_zpos){
            mPositionZ = mPositionZ - 0.03f;
            rotation = rotation + 1.0f;
            if(mPositionZ < final_zpos){
                mPositionZ = final_zpos;
            }
        }

        Matrix.translateM(mModelMatrix,0, mPositionX,mPositionY,mPositionZ);
        Matrix.rotateM(mModelMatrix, 0, rotation, 0.0f, 0.0f, 1.0f);

        if(once){
            corners.clear();
            corners.add(new Node(relativePoint.latitude + SCALE/(double)SessionData.GPS_FACTOR_LAT , relativePoint.longitude + SCALE/(double)SessionData.GPS_FACTOR_LONG ));
            corners.add(new Node(relativePoint.latitude - SCALE/(double)SessionData.GPS_FACTOR_LAT , relativePoint.longitude + SCALE/(double)SessionData.GPS_FACTOR_LONG));
            corners.add(new Node(relativePoint.latitude - SCALE/(double)SessionData.GPS_FACTOR_LAT , relativePoint.longitude - SCALE/(double)SessionData.GPS_FACTOR_LONG));
            corners.add(new Node(relativePoint.latitude + SCALE/(double)SessionData.GPS_FACTOR_LAT , relativePoint.longitude - SCALE/(double)SessionData.GPS_FACTOR_LONG));
            /*for(int i=0; i< gl_coords.length;i=i+3){
                float[] Mcorner = new float[4];
                float[] corner = new float[]{gl_coords[i],gl_coords[i+1],gl_coords[i+2], 1.0f};
                Matrix.multiplyMV(Mcorner, 0, mModelMatrix, 0,  corner, 0);
                corners.add(new Node(Mcorner[0],Mcorner[1]));
            }*/
            outer_loop:
            for (int i = 0; i < SessionData.instance().current_buildings.size(); i++) {
                Building b = SessionData.instance().current_buildings.get(i);
                for(int o=0; o <corners.size(); o=o+1){
                    if(b.my_glbuilding!=null && corners.get(o)!=null && b.my_glbuilding.contains(corners.get(o))){
                        final_zpos = b.height + 0.25f;
                        break outer_loop;
                    }
                }
            }
            once = false;
        }
        standard_draw(vMatrix,pMatrix);

        if(fading){
            alpha = alpha - 0.01f;
            if(alpha < 0.0f){
                SessionData.instance().current_things.remove(uid);
            }
        }
    }

}
