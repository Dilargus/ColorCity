package com.example.android.opengl.OSM;



/**
 * @author Woess
 *	vertex of a building with more information and an ID
 */
public class Node implements java.io.Serializable  {
	
	public double x;
	public double y;
	public double height;
	long id;
	
	public Node(long id, double x, double y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.height = 0.0;
	}
	
	public Node(Node node, double h) {
		super();
		this.id = Long.valueOf(node.id);
		this.x = Double.valueOf(node.x);
		this.y = Double.valueOf(node.y);
		this.height = h;
	}
	
	public Node(Node node) {
		super();
		this.id = Long.valueOf(node.id);
		this.x = Double.valueOf(node.x);
		this.y = Double.valueOf(node.y);
		this.height = Double.valueOf(node.height);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public Node(double x, double y) {
		this.x = x;
		this.y = y;
		this.id = 0;
	}
	
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Node sub(Node v) {
		return new Node(this.x - v.x, this.y - v.y);
	}
	
	public Node add(Node v) {
		return new Node(this.x + v.x, this.y + v.y);
	}
	
	public Node mult(double s) {
		return new Node(this.x * s, this.y * s);
	}
	
	public double dot(Node v) {
		return this.x * v.x + this.y * v.y;
	}
	
	public double mag() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	/**
	 * Compute the 2D pseudo cross product Dot(Perp(u), v)
	 * 
	 * @param v
	 * @return
	 */
	public double cross(Node v) {		
		return this.y * v.x - this.x * v.y;
	}

	public String toString() {
		return "Vector2D(" + x + ", " + y + ")";
	}

}
