package com.example.android.opengl;

import java.text.DateFormat;
import java.util.Date;

import com.example.android.opengl.activity.OpenGLES20Activity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;



/**
 * @author Woess
 *	A class that makes periodically location requests and holds the current location in "current_location"
 */
public class GPSManager extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener   {
	private static String TAG = "GPSMANAGER";
	private GoogleApiClient google_api_client;
	private LocationRequest location_request;

	private Location current_location;
	private int priority;


	private String last_time;
	OpenGLES20Activity activity;
	
	
    public GPSManager(OpenGLES20Activity activity, int priority) {
    	
		super();
		this.activity = activity;
		this.priority = priority;
		
        if (!isGooglePlayServicesAvailable()) {
        	this.activity.finish();
        }
        createLocationRequest();
        google_api_client = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
	}
    
    public void start(){
    	if (google_api_client.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }
    
    

	 private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, activity, 0).show();
            return false;
        }
    }

	@Override
    public void onLocationChanged(Location location) {
        current_location = location;
        last_time = DateFormat.getTimeInstance().format(new Date());
        Log.i("Location", current_location.getLatitude() + " " + current_location.getLongitude());
        activity.updateGPS(current_location);
    }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void stopLocationUpdates() {
		LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client, this);
	    Log.d(TAG, "Location update stopped .......................");
	}
	
	protected void createLocationRequest() {
        location_request = new LocationRequest();
        location_request.setInterval(SessionData.INTERVAL);
        location_request.setFastestInterval(SessionData.FASTEST_INTERVAL);
        location_request.setPriority(priority); 
    }
	
	@Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + google_api_client.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                google_api_client, location_request, this);
        Log.d(TAG, "Location update started ..............: ");
    }
    
    public void connect(){
    	google_api_client.connect();
    }
    
    public void disconnect(){
    	google_api_client.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }
    
	public Location getCurrentLocation() {
		return current_location;
	}

	public void setCurrentLocation(Location current_location) {

		this.current_location = current_location;
	}

	public String getLastTime() {
		return last_time;
	}

	public void setLastTime(String last_time) {
		this.last_time = last_time;
	}

	
	
}
