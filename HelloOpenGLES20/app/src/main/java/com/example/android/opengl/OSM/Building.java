package com.example.android.opengl.OSM;

import com.example.android.opengl.OpenGL.GLobjects.GLBuilding;

import java.util.ArrayList;



/**
 * @author Woess
 *	Building class that holds information from the buildings that it gets from Openstreetmap
 */
public class Building  implements java.io.Serializable  {
	public transient GLBuilding my_glbuilding;
	public ArrayList<Node> nodes = new ArrayList<Node>();
	public Node center;
	private long id;
	private String name;
	public  Float height = 0.0f;
	public static final float[] BUILDING_CLICKED = new float[] {1.0f,1.0f,1.0f,1.0f};

	public Building(long id) {
		super();
		this.id = id;
	}
	public float r = 0.50f;
	public float g = 0.50f;
	public float b = 0.50f;

	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public double PolygonArea() {
			double area = 0.0;
		for (int i = 0; i < nodes.size(); i++) {
			int j = i + 1;
			if(i==nodes.size()-1){
				j=0;
			}
			area += (nodes.get(i).x * nodes.get(j).y - nodes.get(j).x * nodes.get(i).y);
		}
		area =area / 2.0;
		return (Math.abs(area));
	}

	public Node polygonCenterOfMass() {
		double cx = 0.0, cy = 0.0;
		int i, j;
		double A = PolygonArea();


		double factor = 0;

		for (i = 0; i < nodes.size(); i++) {
			j = i + 1;
			if(i==nodes.size()-1){
				j=0;
			}

			factor = (nodes.get(i).x * nodes.get(j).y - nodes.get(j).x * nodes.get(i).y);
			cx += (nodes.get(i).x + nodes.get(j).x) * factor;
			cy += (nodes.get(i).y + nodes.get(j).y) * factor;
		}
		factor = 1.0 / (6.0 * A);
		cx *= factor;
		cy *= factor;
		return new Node( Math.abs(cx), Math.abs(cy));
	}


}
