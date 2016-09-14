package com.example.android.opengl;

import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.android.opengl.Items.MyItem;
import com.example.android.opengl.OSM.Building;
import com.example.android.opengl.OSM.SingleGrid;
import com.example.android.opengl.OSM.Street;
import com.example.android.opengl.OpenGL.GLobjects.Crate;
import com.example.android.opengl.OpenGL.GLobjects.GLObject;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Woess
 *	A class for storing application-wide variables and constants
 */
public class SessionData {
	private static SessionData instance;

	public static final long INTERVAL = 100 * 14;
	public static final long FASTEST_INTERVAL = 100 * 8;
	public static final float BAD_ACCURACY = 40.0f;
	public static final int GPS_FACTOR_LAT = 12000;
	public static final int GPS_FACTOR_LONG = 10000;
	public static final int GPS_FACTOR = GPS_FACTOR_LAT*GPS_FACTOR_LAT/(GPS_FACTOR_LONG*GPS_FACTOR_LONG);


	public HashMap<String, SingleGrid> grid = new HashMap<String, SingleGrid>();

	public List<Building> current_buildings = Collections.synchronizedList(new ArrayList<Building>());
	public List<Street> current_streets = Collections.synchronizedList(new ArrayList<Street>());
	public Map<String, GLObject> current_things = new ConcurrentHashMap<String, GLObject>();
	public Map<String, GLObject> waiting_things = new ConcurrentHashMap<String, GLObject>();
	public Map<String, MyItem> inventory = new ConcurrentHashMap<String, MyItem>();
	public AudioAttributes attributes = new AudioAttributes.Builder()
			.setUsage(AudioAttributes.USAGE_GAME)
			.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
			.build();
	public SoundPool sounds = new SoundPool.Builder()
			.setAudioAttributes(attributes)
			.build();
	public boolean sound_loaded = false;
	public int crate_destroy1;
	public int crate_destroy2;
	public int crate_hit1;
	public int crate_hit2;
	public int star_hit;

	public String user_id;
	public String player_name;
	public String email_acc;
	public String hex_color;
	public LatLng approx_location;
	public LatLng old_location;
public int server_difference;
    public static SessionData instance()
    {
        if(instance == null)
        {
            instance = new SessionData();
        }
        return instance;
    }
    
    public static double distance(LatLng p1, LatLng p2){
		return Math.sqrt((p1.latitude - p2.latitude)*(p1.latitude - p2.latitude)*GPS_FACTOR +
				(p1.longitude - p2.longitude)*(p1.longitude - p2.longitude) );
	}
}


