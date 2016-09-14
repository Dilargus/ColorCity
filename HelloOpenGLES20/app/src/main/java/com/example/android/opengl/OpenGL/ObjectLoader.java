package com.example.android.opengl.OpenGL;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.android.opengl.OpenGL.GLobjects.ObjectHolder;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;



/**
 * @author Woess
 *	A Class that is responsible for loading .obj files and for loading textures
 * call functions: 	loadModel(String fileName) 		(from Assets)
 * 					loadBitmap(String filename)		(from Assets)
 * 					loadBitmapfromDownloads(String filename)		(from Downloads folder)
 * gets called by MyRenderer
 */
public class ObjectLoader {
	private AssetManager asset_manager; 

	ArrayList<Integer> ind;
	ArrayList<Integer> ind_u;
	ArrayList<Integer> ind_n;

	ArrayList<Float> vertics;
	ArrayList<Float> vts;
	ArrayList<Float> vns;
	
	
	public ObjectLoader(AssetManager assetmanager) {
		super();
		this.asset_manager = assetmanager;
	}
	





	public AssetManager getAssetManager() {
		return asset_manager;
	}

	public void setAssetManager(AssetManager asset_manager) {
		this.asset_manager = asset_manager;
	}

	public ObjectHolder loadModel(String fileName) throws IOException{
		ind = new ArrayList<Integer>();
		ind_u = new ArrayList<Integer>();
		ind_n = new ArrayList<Integer>();

		vertics = new ArrayList<Float>();
		vts = new ArrayList<Float>();
		vns = new ArrayList<Float>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(asset_manager.open(fileName+".obj"))));
		String line = "";
		int session = 0;
		while((line = reader.readLine()) != null)
		{

			String[] split = line.split(" ");
			if(split[0].equals("v")){
				for(int i = 1; i < split.length; i++)
					vertics.add(Float.parseFloat(split[i]));
			}
			if(split[0].equals("f"))
			{
				processFLine(line);
			}
			if(split[0].equals("vt"))
			{
				for(int i = 1; i < split.length; i++)
					vts.add(Float.parseFloat(split[i]));
			}
			if(split[0].equals("vn"))
			{
				for(int i = 1; i < split.length; i++)
					vns.add(Float.parseFloat(split[i]));
			}
			
		}
		float[] verts = new float[ind.size() * 3];
		float[] uvs = new float[ind.size() * 2];
		float[] normals = new float[ind.size() * 3];
		for(int i = 0; i < ind.size(); i++)
		{
			int vert = ind.get(i);
			verts[i * 3] = vertics.get(vert*3);
			verts[i * 3 + 1] = vertics.get(vert*3 + 1);
			verts[i * 3 + 2] = vertics.get(vert*3 + 2);
			if(ind_u.size()>0){
				vert = ind_u.get(i);
				uvs[i*2] = vts.get(vert*2);
				uvs[i*2 + 1] = vts.get(vert*2 + 1);
			}
			if(ind_n.size()>0){
				vert = ind_n.get(i);
				normals[i * 3] = vns.get(vert*3);
				normals[i * 3 + 1] = vns.get(vert*3 + 1);
				normals[i * 3 + 2] = vns.get(vert*3 + 2);
			}
		}

		return new ObjectHolder(verts,uvs,normals);
	}
	
	public static ArrayList<Integer> triangulate(ArrayList<Integer> polygon){
		ArrayList<Integer> triangles=new ArrayList<Integer>();
		for(int i=1; i<polygon.size()-1; i++){
			triangles.add(polygon.get(0));
			triangles.add(polygon.get(i));
			triangles.add(polygon.get(i+1));
			Log.i("TRIANGL", i +"");
		}
		return triangles;
	}
	
	private void processFLine(String line){
		String [] tokens=line.split("[ ]+");
		int c=tokens.length;

		if(tokens[1].matches("[0-9]+")){//f: v
			Log.i("OBJLOADER", "[0-9]+");
			if(c==4){//3 faces
				Log.i("OBJLOADER", "3");
				for(int i=1; i<c; i++){
					Integer s=Integer.valueOf(tokens[i]);
					s--;
					ind.add(s);
				}
			}
			else{//more faces
				Log.i("OBJLOADER", "more");
				ArrayList<Integer> polygon=new ArrayList<Integer>();
				for(int i=1; i<tokens.length; i++){
					Integer s=Integer.valueOf(tokens[i]);
					s--;
					polygon.add(s);
				}
				ind.addAll(triangulate(polygon));//triangulate the polygon and add the resulting faces
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+")){//if: v/vt
			Log.i("OBJLOADER", "[0-9]+/[0-9]+");
			if(c==4){//3 faces
				Log.i("OBJLOADER", "3");

				for(int i=1; i<c; i++){
					Integer s=Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					ind.add(s);
					s=Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					ind_u.add(s);
				}
			}
			else{//triangulate
				Log.i("OBJLOADER", "more");
				ArrayList<Integer> tmpFaces=new ArrayList<Integer>();
				ArrayList<Integer> tmpVt=new ArrayList<Integer>();
				for(int i=1; i<tokens.length; i++){
					Integer s=Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					s=Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					tmpVt.add(s);
					Log.i("OBJLOADER", i+ " ...");
				}
				ind.addAll(triangulate(tmpFaces));
				ind_u.addAll(triangulate(tmpVt));
			}
		}
		if(tokens[1].matches("[0-9]+//[0-9]+")){//f: v//vn
			Log.i("OBJLOADER", "[0-9]+//[0-9]+");
			if(c==4){//3 faces
				Log.i("OBJLOADER", "3");

				for(int i=1; i<c; i++){
					Integer s=Integer.valueOf(tokens[i].split("//")[0]);
					s--;
					ind.add(s);
				}
			}
			else{//triangulate
				Log.i("OBJLOADER", "more");
				ArrayList<Integer> tmpFaces=new ArrayList<Integer>();
				for(int i=1; i<tokens.length; i++){
					Integer s=Integer.valueOf(tokens[i].split("//")[0]);
					s--;
					tmpFaces.add(s);
				}
				ind.addAll(triangulate(tmpFaces));
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")){//f: v/vt/vn
			Log.i("OBJLOADER", "[0-9]+/[0-9]+/[0-9]+");
			if(c==4){//3 faces
				Log.i("OBJLOADER", "3");

				for(int i=1; i<c; i++){
					Integer s=Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					ind.add(s);
					s=Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					ind_u.add(s);
					s=Integer.valueOf(tokens[i].split("/")[2]);
					s--;
					ind_n.add(s);
				}
			}
			else{//triangulate
				Log.i("OBJLOADER", "more");
				ArrayList<Integer> tmpFaces=new ArrayList<Integer>();
				ArrayList<Integer> tmpVt=new ArrayList<Integer>();
				ArrayList<Integer> tmpNt=new ArrayList<Integer>();
				for(int i=1; i<tokens.length; i++){
					Integer s=Integer.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					s=Integer.valueOf(tokens[i].split("/")[1]);
					s--;
					tmpVt.add(s);
					s=Integer.valueOf(tokens[i].split("/")[2]);
					s--;
					tmpNt.add(s);
				}
				ind.addAll(triangulate(tmpFaces));
				ind_u.addAll(triangulate(tmpVt));
				ind_n.addAll(triangulate(tmpNt));
			}
		}
	}
}
