package com.example.android.opengl;

/**
 * Created by Woess on 02.08.2016.
 */
public class Vector
{
    // dot product (3D) which allows vector operations in arguments
    public static float dot(float[] u, float[] v) {
        return ((u[X] * v[X]) + (u[Y] * v[Y]) + (u[Z] * v[Z]));
    }

    public static void minus(float[] u, float[] v, float[] result) {
        result[X] = u[X] - v[X];
        result[Y] = u[Y] - v[Y];
        result[Z] = u[Z] - v[Z];
    }

    public static void addition(float[] u, float[] v, float[] result) {
        result[X] = u[X] + v[X];
        result[Y] = u[Y] + v[Y];
        result[Z] = u[Z] + v[Z];
    }

    public static void scalarProduct(float r, float[] u, float[] result) {
        result[X] = u[X] * r;
        result[Y] = u[Y] * r;
        result[Z] = u[Z] * r;
    }

    public static void crossProduct(float[] u, float[] v, float[] result) {
        result[X] = (u[Y] * v[Z]) - (u[Z] * v[Y]);
        result[Y] = (u[Z] * v[X]) - (u[X] * v[Z]);
        result[Z] = (u[X] * v[Y]) - (u[Y] * v[X]);
    }

    // mangnatude or length
    public static float length(float[] u) {
        return (float) Math.abs(Math.sqrt((u[X] * u[X]) + (u[Y] * u[Y]) + (u[Z] * u[Z])));
    }

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public float x;
    public float y;
    public float z;

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
