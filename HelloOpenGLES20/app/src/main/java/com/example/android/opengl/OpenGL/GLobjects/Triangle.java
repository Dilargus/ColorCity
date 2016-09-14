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
package com.example.android.opengl.OpenGL.GLobjects;

import com.example.android.opengl.OpenGL.Ray;
import com.example.android.opengl.Vector;

import java.util.Arrays;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public final class Triangle {

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Triangle() {
    }
    private static final float SMALL_NUM =  0.00000001f; // anything that avoids division overflow


    // intersectRayAndTriangle(): intersect a ray with a 3D triangle
//    Input:  a ray R, and a triangle T
//    Output: *I = intersection point (when it exists)
//    Return: -1 = triangle is degenerate (a segment or point)
//             0 = disjoint (no intersect)
//             1 = intersect in unique point I1
//             2 = are in the same plane
    public static int intersectRayAndTriangle(Ray R, float[] T, float[] I)
    {
        float     r, a, b;             // params to calc ray-plane intersect

        // get triangle edge vectors and plane normal
        float[] V0 = new float[3];
        float[] V1 = new float[3];
        float[] V2 = new float[3];
        float[] u = new float[3];
        float[] v = new float[3];
        float[] n = new float[3];
        float[] dir = new float[3];
        float[] w0 = new float[3];
        float[] w = new float[3];
        float[] tempI = new float[3];
        float[] tempA = new float[3];
        V0[0] = T[0];
        V0[1] = T[1];
        V0[2] = T[2];
        V1[0] = T[3];
        V1[1] = T[4];
        V1[2] = T[5];
        V2[0] = T[6];
        V2[1] = T[7];
        V2[2] = T[8];

        Vector.minus(V1, V0, u);
        Vector.minus(V2, V0, v);
        Vector.crossProduct(u, v, n);             // cross product

        if (Arrays.equals(n, new float[]{0.0f,0.0f,0.0f})){           // triangle is degenerate
            return -1;                 // do not deal with this case
        }
        Vector.minus(R.P1, R.P0, dir);             // ray direction vector
        Vector.minus( R.P0 , V0, w0);
        a = - Vector.dot(n,w0);
        b =  Vector.dot(n,dir);
        if (Math.abs(b) < SMALL_NUM) {     // ray is parallel to triangle plane
            if (a == 0){                // ray lies in triangle plane
                return 2;
            }else{
                return 0;             // ray disjoint from plane
            }
        }

        // get intersect point of ray with triangle plane
        r = a / b;
        if (r < 0.0f){                   // ray goes away from triangle
            return 0;                  // => no intersect
        }
        // for a segment, also test if (r > 1.0) => no intersect


        Vector.scalarProduct(r, dir, tempA);
        Vector.addition(R.P0,  tempA, tempI);           // intersect point of ray and plane
        I[0] = tempI[0];
        I[1] = tempI[1];
        I[2] = tempI[2];

        // is I inside T?
        float    uu, uv, vv, wu, wv, D;
        uu =  Vector.dot(u,u);
        uv =  Vector.dot(u,v);
        vv =  Vector.dot(v,v);
        Vector.minus(I, V0 , w);
        wu =  Vector.dot(w,u);
        wv = Vector.dot(w,v);
        D = (uv * uv) - (uu * vv);

        // get and test parametric coords
        float s, t;
        s = ((uv * wv) - (vv * wu)) / D;
        if (s < 0.0f || s > 1.0f)        // I is outside T
            return 0;
        t = (uv * wu - uu * wv) / D;
        if (t < 0.0f || (s + t) > 1.0f)  // I is outside T
            return 0;

        return 1;                      // I is in T
    }


    public static boolean checkTriangleCollision(float[] triangle1, float[] triangle2) {
        float[] n1 = getN(triangle1);
        float[] n2 = getN(triangle2);
        float[] v = new float[3];
        Vector.crossProduct(n1, n2, v);

        float[] p = new float[3];

        float C1 = n1[0] * triangle1[0] + n1[1] * triangle1[1] + n1[2] * triangle1[2];

        float C2 = n2[0] * triangle2[0] + n2[1] * triangle2[1]+ n2[2] * triangle2[2];

        float Kn = n2[0] / n1[0];

        p[1] = (C2 - C1 * Kn) / (n2[1] - n1[1] * Kn);

        p[0] = (C1 - n1[1] * p[1]) / n2[0];

        p[2] = 0;

        float[] x = new float[3];
        Vector.addition(p, v, x);
        float[] triangle1_vert1 = new float[]{triangle1[0], triangle1[1], triangle1[2]};
        float[] xq1 = new float[3];
        Vector.minus(x, triangle1_vert1, xq1);
        float i1 = Vector.dot(xq1, n1);
        float[] triangle1_vert2 = new float[]{triangle2[0], triangle2[1], triangle2[2]};
        float[] xq2 = new float[3];
        Vector.minus(x, triangle1_vert2, xq2);
        float i2 = Vector.dot(xq2, n2);

        if (i1 == 0 && i2 == 0) {
            return true;
        }
        return false;
    }

    public static float[] getN(float[] triangle) {
        float[] vn1 = new float[3];
        float[] vn2 = new float[3];
        float[] a = new float[]{triangle[0],triangle[1],triangle[2]};
        float[] b = new float[]{triangle[3],triangle[4],triangle[5]};
        float[] c = new float[]{triangle[6],triangle[7],triangle[8]};
        Vector.minus(b, a, vn1);
        Vector.minus(c, a, vn2);
        float[] n = new float[3];
        Vector.crossProduct(vn1, vn2, n);
        return n;
    }

}
