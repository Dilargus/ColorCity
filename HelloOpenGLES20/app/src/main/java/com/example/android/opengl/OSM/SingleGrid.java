package com.example.android.opengl.OSM;

import java.util.ArrayList;



/**
 * @author Woess
 *	One grid item from CityGrid that holds the buildings
 */
public class SingleGrid  implements java.io.Serializable  {
	public String id;
	public ArrayList<Building> buildings = new ArrayList<Building>();
	public ArrayList<Street> streets = new ArrayList<Street>();
	public SingleGrid(String id) {
		this.id = id;
	}
}
