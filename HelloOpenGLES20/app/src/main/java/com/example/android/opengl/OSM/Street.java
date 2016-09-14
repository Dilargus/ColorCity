package com.example.android.opengl.OSM;

import com.example.android.opengl.OpenGL.GLobjects.GLStreet;

import java.util.ArrayList;


/**
 * @author Woess
 *	Building class that holds information from the buildings that it gets from Openstreetmap
 */
public class Street implements java.io.Serializable  {
	public transient GLStreet my_glstreet;
	public ArrayList<Node> nodes = new ArrayList<Node>();
	public ArrayList<Node> gl_nodes = new ArrayList<Node>();

	private long id;
	private String name;

	public Street(long id) {
		super();
		this.id = id;
	}


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

		int i, j;
		double area = 0;

		for (i = 0; i < nodes.size(); i++) {
			j = (i + 1) % nodes.size();
			area += nodes.get(i).x * nodes.get(j).y;
			area -= nodes.get(i).y * nodes.get(j).x;
		}

		area /= 2.0;
		return (Math.abs(area));
	}

	public Node polygonCenterOfMass() {
		double cx = 0, cy = 0;
		int i, j;
		double A = PolygonArea();


		double factor = 0;

		for (i = 0; i < nodes.size(); i++) {
			j = (i + 1) % nodes.size();

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
