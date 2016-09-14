package com.example.android.opengl.OpenGL.GLobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.android.opengl.OSM.Node;
import com.example.android.opengl.OSM.OSMManager;
import com.example.android.opengl.OpenGL.MyGLRenderer;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Woess on 03.08.2016.
 */
public class Floor extends GLObject{
    /*private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer normalBuffer;
    private int mProgram;
    private boolean isInit = false;
    private float[] light_pos;
    private float[] gl_coords;
    private float[] gl_normals;
    private float[] gl_tex_coords;
    private int mTexHandle;
    private float alpha;
    private LatLng relativePoint;
    private int vertexShader;
    private int fragmentShader;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mLightHandle;
    private int mNormalHandle;
    private int mTextureUniformHandle;

    private int mAlphaHandle;
    private int mMMatrixHandle;*/

    //float color[];
    //private float[] mVPMatrix;
    //private float[] mModelMatrix;
    //private float[] mMVMatrix;
    //static final int COORDS_PER_VERTEX = 3;
    //static final int TEX_COORDS_PER_VERTEX = 2;
    //private final int vertexStride = COORDS_PER_VERTEX * 4;
    static final int FLOOR_SIZE = 10;
    //private int vertexCount;

    private final String floor_vertex_shader =
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



    private final String floor_fragment_shader =
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
                    + "   diffuse = diffuse * (20.0 / (1.0 + (0.25 * distance * distance)));  \n"
                    + "	  vec4 text = texture2D(s_texture, f_texCoord); \n"
     //               + "	  vec4 col = v_Color  \n"
                    + "   gl_FragColor = vec4(text.r,text.g,text.b, u_Alpha);     \n"
       //             + "   gl_FragColor = vec4(v_Color[0]* diffuse * 10.0,v_Color[1]* diffuse * 10.0,v_Color[2]* diffuse * 10.0, u_Alpha);     \n"
                    + "   gl_FragColor = vec4(text.r * diffuse,text.g * diffuse ,text.b * diffuse, u_Alpha);     \n"		// Pass the color directly through the pipeline.
                    + "}                              \n";

    public Floor() {
        // initialize vertex byte buffer for shape coordinates
        this.relativePoint = new LatLng(-FLOOR_SIZE,-FLOOR_SIZE);
    }
    public void init(){

        OSMManager bm = new OSMManager();

        ArrayList<Node> triangulated = new ArrayList<Node>();
        for(int x=-FLOOR_SIZE; x< FLOOR_SIZE; x++){
            for(int y= -FLOOR_SIZE; y < FLOOR_SIZE; y++){
                ArrayList<Node> nodes = new ArrayList<Node>();
                nodes.add(new Node(0,x,y+1));
                nodes.add(new Node(0,x+1,y+1));
                nodes.add(new Node(0,x+1,y));
                nodes.add(new Node(0,x,y));
                triangulated.addAll(bm.triangulator.triangulateRectangle(nodes));
            }
        }
        gl_coords =new float[triangulated.size() * 3];
        int h=0;
        for(int g = 0; g < triangulated.size(); g++){
            gl_coords[h] = 		(float)(triangulated.get(g).getY());
            gl_coords[h + 1] = 	(float)(triangulated.get(g).getX());
            gl_coords[h + 2] = 	0.0f;
            h = h + 3;
        }
        vertexCount = gl_coords.length / COORDS_PER_VERTEX;
        ArrayList<Node> s = new ArrayList<Node>();
        float[] tex_coords = bm.createUVS(triangulated, s);
        gl_tex_coords = new float[tex_coords.length];
        gl_tex_coords = tex_coords.clone();
        tex_coords = null;
        gl_normals = new float[gl_coords.length];
        for(int i=0; i<gl_coords.length;i=i+3){
            gl_normals[i+0] = 0.0f;
            gl_normals[i+1] = 0.0f;
            gl_normals[i+2] = 1.0f;
        }

        makeBufferReady();

        // prepare shaders and OpenGL program
        vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, floor_vertex_shader);
        fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, floor_fragment_shader);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

        alpha = 1.0f;
        light_pos = new float[]{0.0f, 0.0f, 4.0f, 1.0f};
        color = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
        mMVMatrix = new float[16];
        mVPMatrix = new float[16];
        isInit=true;

    }

    public void draw(float[] vMatrix, float[] pMatrix, int texture_nr) {
        if(!isInit) {
            init();
            texture = texture_nr;
        }

        this.mModelMatrix =  new float[]{1.0f,0.0f,0.0f,0.0f,
                0.0f,1.0f,0.0f,0.0f,
                0.0f,0.0f,1.0f,0.0f,
                0.0f,0.0f,0.0f,1.0f};
        float mPositionX = -0.0f;
        float mPositionY = -0.0f;
        float mPositionZ = -0.0f;
        Matrix.translateM(this.mModelMatrix,0,this.mModelMatrix,0, mPositionX,mPositionY,mPositionZ);

       standard_draw(vMatrix,pMatrix);
    }

}
