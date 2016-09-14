package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OpenGL.MyGLRenderer;
import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.SessionData;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Woess on 03.08.2016.
 */
public class GLBuilding extends  GLObject{
    private float[] coords;
    private float[] normals;
    private float[] tex_coords;
    public Building my_building;

    private final String building_vertex_shader =
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



    private final String building_fragment_shader =
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
                    + "   diffuse = diffuse * (10.0 / (1.0 + (0.25 * distance * distance)));  \n"
                    + "	  vec4 text = texture2D(s_texture, f_texCoord); \n"
     //               + "	  vec4 col = v_Color  \n"
                    + "   gl_FragColor = vec4(v_Color[0]*text.r * diffuse,v_Color[1]*text.g * diffuse,v_Color[2]*text.b * diffuse, u_Alpha);     \n"		// Pass the color directly through the pipeline.
                    + "}                              \n";

    public GLBuilding(Building my_building, float[] vertices, float[]tex_coords, float[] normals, LatLng relativePoint) {
        this.my_building = my_building;
        // initialize vertex byte buffer for shape coordinates
        this.relativePoint = relativePoint;
        coords = vertices;
        this.tex_coords = tex_coords;
        this.normals = normals;
        this.color = new float[] {my_building.r,my_building.g,my_building.b,1.0f};
    }
    public float[] getCoords(){
        return gl_coords;
    }

    public float[] getModelMatrix(){
        return this.mModelMatrix;
    }

    public float[] getmMVMatrix() {
        return mMVMatrix;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public void init(){
        vertexCount = coords.length / COORDS_PER_VERTEX;
        gl_coords = new float[coords.length];
        gl_coords = coords.clone();
        coords = null;

        gl_tex_coords = new float[tex_coords.length];
        gl_tex_coords = tex_coords.clone();
        tex_coords = null;

        gl_normals = new float[normals.length];
        gl_normals = normals.clone();
        normals = null;

        makeBufferReady();

        // prepare shaders and OpenGL program
        vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, building_vertex_shader);
        fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, building_fragment_shader);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        alpha = 1.0f;
        light_pos = new float[]{0.0f, 0.0f, 4.0f, 1.0f};
        mMVMatrix = new float[16];
        mVPMatrix = new float[16];
        isInit=true;

    }

    public void draw(float[] vMatrix, float[] pMatrix, int texture_nr, LatLng current) {
        if(!isInit) {
            texture = texture_nr;
            init();
        }


        this.mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};
        float mPositionX = (float) (SessionData.GPS_FACTOR_LAT*(relativePoint.latitude - current.latitude));
        float mPositionY = (float) (SessionData.GPS_FACTOR_LONG*(relativePoint.longitude - current.longitude));
        float mPositionZ = 0.0f;
        Matrix.translateM(mModelMatrix,0, mPositionX,mPositionY,mPositionZ);

        /*if(once) {
            corners = new ArrayList<Node>();
            for (int i = 0; i < my_building.gl_nodes.size(); i++) {
                Node gl_node = my_building.gl_nodes.get(i);
                float[] Mcorner = new float[4];
                float[] corner = new float[]{(float) gl_node.x, (float) gl_node.y, 0.0f, 1.0f};
                Matrix.multiplyMV(Mcorner, 0, mModelMatrix, 0, corner, 0);
                corners.add(new Node(Mcorner[0], Mcorner[1]));
            }
            once = false;
        }*/
        standard_draw(vMatrix,pMatrix);
    }

    public boolean contains(Node test) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = my_building.getNodes().size() - 1; i < my_building.getNodes().size(); j = i++) {
            if ((my_building.getNodes().get(i).y > test.y) != (my_building.getNodes().get(j).y > test.y) &&
                    (test.x < (my_building.getNodes().get(j).x - my_building.getNodes().get(i).x) * (test.y - my_building.getNodes().get(i).y) / (my_building.getNodes().get(j).y-my_building.getNodes().get(i).y) + my_building.getNodes().get(i).x)) {
                result = !result;
            }
        }
        return result;
    }
}
