package com.example.android.opengl.OpenGL.GLobjects;

/**
 * Created by Woess on 06.09.2016.
 */
public class ObjectHolder {
    public float[] vertices;
    public float[] texture_coords;
    public float[] normals;
    public int texture;

    public ObjectHolder(float[] vertices, float[] texture_coords, float[] normals) {
        this.vertices = vertices;
        this.texture_coords = texture_coords;
        this.normals = normals;
    }
}
