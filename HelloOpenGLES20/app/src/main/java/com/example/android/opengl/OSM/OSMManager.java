package com.example.android.opengl.OSM;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.opengl.OpenGL.EarClippingTriangulator;
import com.example.android.opengl.OpenGL.GLobjects.GLBuilding;
import com.example.android.opengl.OpenGL.GLobjects.GLStreet;
import com.example.android.opengl.SessionData;
import com.example.android.opengl.Vector;
import com.example.android.opengl.app.AppConfig;
import com.example.android.opengl.app.AppController;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class OSMManager extends BroadcastReceiver{
	private static String TAG = "BUILDINGMANAGER";

	public LatLng latest_location;
	public ArrayList<String> downloaded_list = new ArrayList<String>();
	private boolean alt_server = true;
	private DownloadManager download_manager;
	private long download_id=0;
	private Context context;
	private double a = 0.000;
	private double i = 0.000;

	private final double BORDER_DISTANCE = 0.001;
	//private boolean gets_downloaded = false;
	public EarClippingTriangulator triangulator = new EarClippingTriangulator();
	
	public OSMManager(Context context) {
		super();
		this.context = context;
	}
	public OSMManager() {
		super();
	}

	private boolean validDownload(long downloadId) {

		Log.d(TAG,"Checking download status for id: " + downloadId);
		//Verify if download is a success
		Cursor c= download_manager.query(new DownloadManager.Query().setFilterById(downloadId));

		if(c.moveToFirst()){
			int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

			if(status == DownloadManager.STATUS_SUCCESSFUL){
				//gets_downloaded = false;
				String id = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
				Log.i("validDownload", id);
				return true;
			}else{
				int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));

				Log.d(TAG, "Download not correct, status [" + status + "] reason [" + reason + "]");
				//gets_downloaded = false;
				return false;
			}
		}
		return false;
	}

	@Override
	public void onReceive(Context ctxt, Intent intent) {
    	if (download_id!= 0  && intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == download_id && validDownload(download_id)) {
    		Log.i(TAG,"building file received");
			Bundle extras = intent.getExtras();
			DownloadManager.Query q = new DownloadManager.Query();
			q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
			Cursor c = download_manager.query(q);
			String id = "invalid";
			if (c.moveToFirst()) {
				int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
				if (status == DownloadManager.STATUS_SUCCESSFUL) {
					// process download
					id = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));

					// get other required data by changing the constant passed to getColumnIndex
				}
			}
			Log.i("produce File" , id);
    		produceDataFile(id);
			File old_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),id);
			if(old_file.exists()){
				old_file.delete();
			}
    	}
    }
	
	public void produceDataFile(String id){
		SAXParserFactory factory = SAXParserFactory.newInstance();
  	    SAXParser parser = null;
  	    try {
  	    	parser = factory.newSAXParser();
			ArrayList<Building> buildings = null;
			ArrayList<Street> streets = null;
			if(alt_server){
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),id);
				NodeParser nodehandler = new NodeParser(context);
				parser.parse(file, nodehandler);
				HashMap<Long,Node> nodes = nodehandler.getParsedData();
				BuildingParser handler = new BuildingParser(context, nodes);
				StreetParser street_parser = new StreetParser(context, nodes);
				parser.parse(file, handler);
				buildings = handler.getParsedData();
				parser.parse(file, street_parser);
				streets = street_parser.getParsedData();
			}
			else{
				BuildingParser handler = new BuildingParser(context);
				StreetParser street_parser = new StreetParser(context);
				File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),id);
				parser.parse(file, handler);
				buildings = handler.getParsedData();
				parser.parse(file, street_parser);
				streets = street_parser.getParsedData();
			}


			SingleGrid grid = new SingleGrid(id);
			grid.buildings.addAll(buildings);
			grid.streets.addAll(streets);
			sendNewSection(grid);

  	        /*for(int i=0; i<buildings.size(); i++){
  	        	Point grid = findGrid(buildings.get(i).center.x,buildings.get(i).center.y);
  	        	if(grid.x <SessionData.NUMBEROFGRIDS && grid.y <SessionData.NUMBEROFGRIDS){
  	        		SessionData.instance().citygrid.grid[grid.y][grid.x].buildings.add(buildings.get(i));
  	        	}
  	        }*/

			FileOutputStream fos = context.openFileOutput("grid_"+id+".data", Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(grid);
			os.close();
			fos.close();
			SessionData.instance().grid.put("grid_"+id+".data", grid);

  	        /*Toast toast= Toast.makeText(context.getApplicationContext(), "The application will restart now", Toast.LENGTH_LONG);
			toast.show();
			Intent mStartActivity = new Intent(context, OpenGLES20Activity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
			//TODOSystem.exit(0);
			*/
  	    } catch (SAXException e) {
  	        e.printStackTrace();
  	        Log.e(TAG, "SAXException");
  	    } catch (IOException e) {
  	        e.printStackTrace();
  	        Log.e(TAG, "IOException");
  	    } catch (ParserConfigurationException e) {
  	        e.printStackTrace();
  	        Log.e(TAG, "ParserConfigurationException");
  	    }
	}

	public void sendNewSection(final SingleGrid grid){
		String tag_string_req = "sendnewsection";

		StringRequest strReq = new StringRequest(Request.Method.POST,
				AppConfig.URL_SENDNEWSECTION, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.d("OpenGLES2 Activity", "Response: " + response.toString());

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");

					if (error) {
						// Error in login. Get the error message
						String errorMsg = jObj.getString("error_msg");
						Toast.makeText(context,
								errorMsg, Toast.LENGTH_LONG).show();
					}
					else{
						getBuildingInformation(grid);
					}


				} catch (JSONException e) {
					// JSON error
					e.printStackTrace();
					Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("OpenGLES2 Activity", "SQL Section Error: " + error.getMessage());
				Toast.makeText(context,
						error.getMessage(), Toast.LENGTH_LONG).show();

			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("section_id", grid.id);
				for(int i=0; i<grid.buildings.size();i++) {
					params.put("building_id_" + i, String.valueOf(grid.buildings.get(i).getId()));

				}
				return params;
			}
		};
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public void getBuildingInformation(final SingleGrid grid){

		for(int a=0; a<grid.buildings.size();a++){
			if(grid.buildings.get(a).r==0.5 && grid.buildings.get(a).g==0.5 && grid.buildings.get(a).b==0.5){
				Random rnd = new Random();

				grid.buildings.get(a).r = rnd.nextFloat();
				grid.buildings.get(a).g = rnd.nextFloat();
				grid.buildings.get(a).b = rnd.nextFloat();
				grid.buildings.get(a).height = (rnd.nextFloat()+0.1f)*3.0f;
			}
		}
		processBuildings(grid.buildings);
		processStreets(grid.streets);
		/*String tag_string_req = "getBuildingInformation";

		StringRequest strReq = new StringRequest(Request.Method.POST,
				AppConfig.URL_GETBUILDINGINFORMATION, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e("OpenGLES2 Activity", "Response: " + response.toString());

				try {
					JSONObject jObj = new JSONObject(response);
					boolean error = jObj.getBoolean("error");

					if (!error) {
						for(int i = 0; i<(jObj.names().length())/6; i=i+1){
							for(int a=0; a<grid.buildings.size();a++){
								if(String.valueOf(grid.buildings.get(a).getId()).equals(jObj.getString("building_id"+i))){
									grid.buildings.get(a).r = Float.valueOf(jObj.getString("r"+i));
									grid.buildings.get(a).g = Float.valueOf(jObj.getString("g"+i));
									grid.buildings.get(a).b = Float.valueOf(jObj.getString("b"+i));
									grid.buildings.get(a).height = Float.valueOf(jObj.getString("height"+i))*3.0f;
									Log.i("building got",grid.buildings.get(a).getId() +  " r " + grid.buildings.get(a).r + " g " + grid.buildings.get(a).g + " b " + grid.buildings.get(a).b + " height " + grid.buildings.get(a).height);
									break;
								}
							}

						}

						processBuildings(grid.buildings);
						processStreets(grid.streets);
					}
					else{
						// Error in login. Get the error message
						String errorMsg = jObj.getString("error_msg");
						Toast.makeText(context,
								errorMsg, Toast.LENGTH_LONG).show();
					}
				} catch (JSONException e) {
					// JSON error
					e.printStackTrace();
					Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("OpenGLES2 Activity", "SQL Building Error: " + error.getMessage());
				Toast.makeText(context,
						error.getMessage(), Toast.LENGTH_LONG).show();

			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("section_id", grid.id);
				return params;
			}
		};
		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);*/
	}
	/*public Point findGrid(double para_lat, double para_long){
		for(int lat=0; lat < SessionData.NUMBEROFGRIDS; lat++){
      		for(int longi=0; longi < SessionData.NUMBEROFGRIDS; longi++){
      			if(para_lat > SessionData.instance().citygrid.getBottom() + lat*SessionData.instance().citygrid.getLatRange()/SessionData.NUMBEROFGRIDS &&
      					para_lat <= SessionData.instance().citygrid.getBottom() + (lat+1)*SessionData.instance().citygrid.getLatRange()/SessionData.NUMBEROFGRIDS &&
      					para_long > SessionData.instance().citygrid.getLeft() + longi*SessionData.instance().citygrid.getLongRange()/SessionData.NUMBEROFGRIDS &&
      					para_long <= SessionData.instance().citygrid.getLeft() + (longi+1)*SessionData.instance().citygrid.getLongRange()/SessionData.NUMBEROFGRIDS){
      				return new Point(lat,longi);
      			}
      		}
		}
		return new Point(SessionData.NUMBEROFGRIDS,SessionData.NUMBEROFGRIDS);
	}*/
	
	public void refreshEnvironment(LatLng current_pos) {


		latest_location = current_pos;
		DecimalFormat df = new DecimalFormat("#.###");
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		df.setRoundingMode(RoundingMode.DOWN);
		double bottom_lat = Double.valueOf(df.format(current_pos.latitude));
		double top_lat =  Double.valueOf(df.format(bottom_lat + 0.001));
		double left_long = Double.valueOf(df.format(current_pos.longitude));
		double right_long = Double.valueOf(df.format(left_long + 0.001));

		double ch_bot_lat = Double.valueOf(df.format(bottom_lat+i));
		double ch_top_lat = Double.valueOf(df.format(top_lat+i));
		double ch_left_long = Double.valueOf(df.format(left_long+a));
		double ch_right_long = Double.valueOf(df.format(right_long+a));
		if(!(SessionData.instance().grid.containsKey("grid_"+ch_left_long + "," + ch_bot_lat + "," + ch_right_long + "," + ch_top_lat+".xml.data") || downloaded_list.contains("grid_" +ch_left_long + "," + ch_bot_lat + "," + ch_right_long + "," + ch_top_lat+".xml.data"))){
			downloaded_list.add("grid_" + ch_left_long + "," + ch_bot_lat + "," + ch_right_long + "," + ch_top_lat + ".xml.data");
			downloadEnvironment(ch_left_long,ch_bot_lat,ch_right_long,ch_top_lat);
		}
		else{
			if(SessionData.instance().grid.containsKey("grid_"+ch_left_long + "," + ch_bot_lat + "," + ch_right_long + "," + ch_top_lat+".xml.data")){
				SingleGrid single_grid = SessionData.instance().grid.get("grid_"+ch_left_long + "," + ch_bot_lat + "," + ch_right_long + "," + ch_top_lat+".xml.data");
				for(int b=0; b<single_grid.buildings.size();b++){
					Building building = single_grid.buildings.get(b);
					if(!findBuilding(building.getId())){
						//0.0014
						if(SessionData.distance(new LatLng(building.center.x,building.center.y), current_pos) < BORDER_DISTANCE){
							SessionData.instance().current_buildings.add(building);
							Log.i("OSMManager" , "added building, size:" + SessionData.instance().current_buildings.size());

						}
					}
					else{
						if(SessionData.distance(new LatLng(building.center.x,building.center.y), current_pos) >= BORDER_DISTANCE){
							SessionData.instance().current_buildings.remove(building);
							Log.i("OSMManager" , "removed building, size:" + SessionData.instance().current_buildings.size());

						}
					}
				}

				for(int b=0; b<single_grid.streets.size();b++){
					Street street = single_grid.streets.get(b);
					if(!findStreet(street.getId())){
						Log.i("STREET" , "adding street " + street.getId() + " " + street.getNodes().size());
						SessionData.instance().current_streets.add(street);
					}
					else{
						//Log.i("STREET" , "removing street " + street.getId() + " " + street.getNodes().size());
						//	SessionData.instance().current_streets.remove(street);
					}
				}
			}

			if(a==0.000 && i==0.000){
				a = 0.001;
				i=0.000;
				//refreshEnvironment(current_pos, 0.001, 0.000);
			}
			else if(a==0.001 && i==0.000){
				a=0.001;
				i=0.001;
				//refreshEnvironment(current_pos, 0.001, 0.001);
			}
			else if(a==0.001 && i==0.001){
				a=0.000;
				i=0.001;
				//refreshEnvironment(current_pos, 0.000, 0.001);
			}
			else if(a==0.000 && i==0.001){
				a = -0.001;
				i= 0.000;
				//refreshEnvironment(current_pos, -0.001, 0.000);
			}
			else if(a==-0.001 && i==0.000){
				i = -0.001;
				a=-0.001;
				//refreshEnvironment(current_pos, -0.001, -0.001);
			}
			else if(a==-0.001 && i==-0.001){
				a=0.000;
				i=-0.001;
				//refreshEnvironment(current_pos, -0.000, -0.001);
			}
			else if(a==-0.000 && i==-0.001){
				a=0.001;
				i=-0.001;
				//refreshEnvironment(current_pos, 0.001, -0.001);
			}
			else if(a==0.001 && i==-0.001){
				a = -0.001;
				i= 0.001;
				//refreshEnvironment(current_pos, -0.001, 0.001);
			}
			else{
				a=0.000;
				i=0.000;
			}
		}

		//if(latest_location==null || SessionData.distance(current_pos, latest_location) > 0.0002){


	}


	
	public float[] createUVS(ArrayList<Node> triangulated, ArrayList<Node> triangulated_top){
		float [] uvs = new float[triangulated.size() * 2];
		
		for(int n=0; n < triangulated_top.size()* 2; n=n+6){
			uvs[n  ] = 0.50f;
			uvs[n+1] = 0.50f;

			uvs[n+2] = 0.51f;
			uvs[n+3] = 0.50f;

			uvs[n+4] = 0.51f;
			uvs[n+5] = 0.51f;

		}
		
		for(int n=triangulated_top.size()* 2; n < triangulated.size() * 2; n=n+12){
			uvs[n  ] = 0.0f;
			uvs[n+1] = 0.0f;
			
			uvs[n+2] = 1.0f;
			uvs[n+3] = 0.0f;
			
			uvs[n+4] = 1.0f;
			uvs[n+5] = 1.0f;
			
			
			uvs[n+6] = 0.0f;
			uvs[n+7] = 0.0f;
			
			uvs[n+8] = 1.0f;
			uvs[n+9] = 1.0f;
			
			uvs[n+10] = 0.0f;
			uvs[n+11] = 1.0f;
		} 
		return uvs;
	}
	
	private LatLng computeRelativePoint(ArrayList<Node> nodes){
		double lowest_lat = -360;
		double lowest_long = -360;
		for(int j=0; j<nodes.size(); j++){
			Node node = nodes.get(j);
			
			if(node.getY()<lowest_long || lowest_long == -360){
				lowest_long = node.getY();
			}
			
			if(node.getX()<lowest_lat || lowest_lat == -360){
				lowest_lat = node.getX();
			}
		}
		return new LatLng(lowest_lat,lowest_long);
	}


	
	public float[] toArray(ArrayList<Node> triangulated, double lowest_long, double lowest_lat){
		float [] nodes = new float[triangulated.size() * 3];
		int h=0;
		for(int g = 0; g < triangulated.size(); g++){
			
			nodes[h] = 		(float)((triangulated.get(g).getX() - lowest_lat)*SessionData.GPS_FACTOR_LAT);
			nodes[h + 1] = 	(float)((triangulated.get(g).getY() - lowest_long)*SessionData.GPS_FACTOR_LONG);
			nodes[h + 2] = 	(float) (triangulated.get(g).getHeight());
			//nodes[h + 3] = 	(float) 1.0;
			//Log.i(TAG,"long: " + nodes[h] + " lat " + nodes[h + 1] + " height: " + nodes[h + 2]);
			h = h + 3;
		}
		return nodes;
	}

	public void processBuildings(ArrayList<Building> buildings){
		for(int i=0; i< buildings.size();i++){
			Building building = buildings.get(i);
			Log.i("CHECK BUILDING", building.getId() + " " + building.nodes.size());
			ArrayList<Node> bottom = new ArrayList<Node>();
			ArrayList<Node> top = new ArrayList<Node>();
			bottom.addAll(building.getNodes());
			for(int j=0; j < bottom.size()-1; j++){
				top.add(new Node(bottom.get(j),building.height));
			}

			LatLng rel_point = computeRelativePoint(building.nodes);
			ArrayList<Node> triangulated_bottom = new ArrayList<Node>();
			ArrayList<Node> triangulated_top = new ArrayList<Node>();
			triangulated_bottom = triangulator.computeTriangles(building.getNodes());
			for(int s=0; s<triangulated_bottom.size();s++){
				triangulated_top.add(new Node(triangulated_bottom.get(s),building.height));
			}

			ArrayList<ArrayList<Node>> side_planes = new ArrayList<ArrayList<Node>>();
			int t = 1;
			for (int s=0; s<top.size(); s++){
				ArrayList<Node> side_plane = new ArrayList<Node>();
				if(t==top.size()){
					t=0;
				}

				side_plane.add(top.get(s));
				side_plane.add(top.get(t));
				side_plane.add(bottom.get(t));
				side_plane.add(bottom.get(s));
				side_planes.add(side_plane);
				t++;

			}

			ArrayList<ArrayList<Node>> triangulated_side_planes = new ArrayList<ArrayList<Node>>();
			for(int m=0; m< side_planes.size(); m++){
				triangulated_side_planes.add(triangulator.triangulateRectangle(side_planes.get(m)));
			}

			ArrayList<Node> triangulated_walls = new ArrayList<Node>();
			for(int n=0; n < triangulated_side_planes.size(); n++){
				triangulated_walls.addAll(triangulated_side_planes.get(n));
			}
			ArrayList<Node> triangulated = new ArrayList<Node>();
			triangulated.addAll(triangulated_top);
			triangulated.addAll(triangulated_walls);
			float[] nodes = toArray(triangulated, rel_point.longitude, rel_point.latitude);
			float[] uvs = createUVS(triangulated, triangulated_top);
			float[] normals = createNormals(nodes);
			building.my_glbuilding = new GLBuilding(building, nodes, uvs, normals, rel_point);
		}
	}

	public void processStreets(ArrayList<Street> streets){
		for(int i=0; i< streets.size();i++){
			Street street = streets.get(i);
			ArrayList<Node> bottom = new ArrayList<Node>();
			bottom.addAll(street.getNodes());

			LatLng rel_point = computeRelativePoint(street.nodes);
			for(int f=0; f<street.nodes.size();f++){
				street.gl_nodes.add(new Node((street.nodes.get(f).getX()- rel_point.latitude)*SessionData.GPS_FACTOR_LAT, (street.nodes.get(f).getY()- rel_point.longitude)*SessionData.GPS_FACTOR_LONG));
				Log.i("STREETNODES","street: " + i + " node: " +  f + " x: " + street.gl_nodes.get(street.gl_nodes.size()-1).x + " y: " + street.gl_nodes.get(street.gl_nodes.size()-1).y);
			}
			ArrayList<Node> triangulated_bottom = new ArrayList<Node>();
			triangulated_bottom = createPath(street.getNodes());


			float[] nodes = toArray(triangulated_bottom, rel_point.longitude, rel_point.latitude);
			float[] normals = new float[nodes.length];
			for(int b=0; b<normals.length; b=b+3){
				normals[b] = 0.0f;
				normals[b+1] = 0.0f;
				normals[b+2] = 1.0f;
			}
			street.my_glstreet = new GLStreet(street, nodes, normals, rel_point);
			Log.i("STREET","street: " + street.getId() + " added: my_glstreet");

		}
	}

	private float[] createNormals(float[] gl_coords){
		float[] normals = new float[gl_coords.length];
		for(int i=0; i<gl_coords.length/9; i=i+1){

			float a[] = {gl_coords[i*9+0], gl_coords[i*9+1], gl_coords[i*9+2]};
			float c[] = {gl_coords[i*9+3], gl_coords[i*9+4], gl_coords[i*9+5]};
			float b[] = {gl_coords[i*9+6], gl_coords[i*9+7], gl_coords[i*9+8]};

			float[] tempab = new float[3];
			Vector.minus(a,b,tempab);
			float[] tempbc = new float[3];
			Vector.minus(b,c,tempbc);
			float[] cr = new float[3];
			Vector.crossProduct(tempab,tempbc,cr);

			normals[i*9+0] = cr[0];
			normals[i*9+1] = cr[1];
			normals[i*9+2] = cr[2];
			normals[i*9+3] = cr[0];
			normals[i*9+4] = cr[1];
			normals[i*9+5] = cr[2];
			normals[i*9+6] = cr[0];
			normals[i*9+7] = cr[1];
			normals[i*9+8] = cr[2];

		}
		return normals;
	}
	
	private boolean findBuilding(long id){
		for(int i = 0; i< SessionData.instance().current_buildings.size(); i++){
			if(id == SessionData.instance().current_buildings.get(i).getId()){
				return true;
			}
		}
		return false;
	}

	private boolean findStreet(long id){
		for(int i = 0; i< SessionData.instance().current_streets.size(); i++){
			if(id == SessionData.instance().current_streets.get(i).getId()){
				return true;
			}
		}
		return false;
	}





	public void downloadEnvironment(double left_long, double bottom_lat, double right_long, double top_lat){
		//if(!gets_downloaded){
		//	gets_downloaded = true;
			//Toast toast= Toast.makeText(context.getApplicationContext(), "Download Buildings, this can take some minutes, afterwards the app will restart.", Toast.LENGTH_LONG);
			//toast.show();

			/*double top_lat = current_location.latitude + 0.0005;
			double bottom_lat = current_location.latitude - 0.0005;
			double left_long = current_location.longitude - 0.0005;
			double right_long = current_location.longitude + 0.0005;*/
			String id = left_long  +","+ bottom_lat + "," + right_long + "," + top_lat;
			//String url = "http://overpass-api.de/api/map?bbox="+id;
			String url = "http://api.openstreetmap.fr/oapi/interpreter?data=(way[building]("+bottom_lat+","+left_long+","+top_lat+","+right_long+");%20way[highway]("+bottom_lat+","+left_long+","+top_lat+","+right_long+");%20);%20out%20meta%20qt;%20%3E;%20out%20meta;";


			Log.i(TAG,url);
			File old_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),id+".xml");
			if(old_file.exists()){
				old_file.delete();
			}
	     
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription("Downloads a XML file from Openstreetmaps");
			//request.setTitle("Building XML File");
	
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
			}
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, id+".xml");
	
			download_manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
	  
			download_id = download_manager.enqueue(request);
		//}
	}

	private ArrayList<Node> createPath(ArrayList<Node> path){
		ArrayList<Node> triangulated_path = new ArrayList<Node>();
		Node node_saver1 = null;
		Node node_saver2 = null;

		for(int z = 0; z < path.size() - 1 ; z++){
			Node src= path.get(z);
			Node dest= path.get(z+1);

			ArrayList<Node> rectangle = getRectangle(src.x, src.y, dest.x, dest.y, 0.00002);
			ArrayList<Node> rect_between = new ArrayList<Node>();
			if(node_saver1!=null && node_saver2!=null){
				rect_between.add(node_saver1);
				rect_between.add(node_saver2);
				rect_between.add(rectangle.get(2));
				rect_between.add(rectangle.get(3));
				triangulated_path.addAll(triangulator.triangulateRectangle(rect_between));
			}
			triangulated_path.addAll(triangulator.triangulateRectangle(rectangle));

			node_saver1 = rectangle.get(0);
			node_saver2 = rectangle.get(1);
		}

		return triangulated_path;
	}
	private ArrayList<Node> getRectangle(double startX, double startY, double stopX, double stopY, double distance)
	{
		Node p = new Node(startX - stopX, startY - stopY);
		Node n = new Node(-p.y, p.x);
		double norm_length =  (double) Math.sqrt((n.x * n.x) + (n.y * n.y));
		n.x /= norm_length;
		n.y /= norm_length;
		Node a = new Node(stopX + (distance * n.x),stopY + (distance * n.y));
		Node b = new Node(stopX - (distance * n.x),stopY - (distance * n.y));
		Node c = new Node(startX - (distance * n.x),startY - (distance * n.y));
		Node d = new Node(startX + (distance * n.x),startY + (distance * n.y));

		ArrayList<Node> rectangle = new ArrayList<Node>();
		rectangle.add(a);
		rectangle.add(b);
		rectangle.add(c);
		rectangle.add(d);
		return rectangle;
	}

}
