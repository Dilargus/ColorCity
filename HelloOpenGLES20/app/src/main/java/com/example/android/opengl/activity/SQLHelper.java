package com.example.android.opengl.activity;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.android.opengl.Items.MyItem;
import com.example.android.opengl.OpenGL.GLobjects.Crate;
import com.example.android.opengl.OpenGL.GLobjects.Star;
import com.example.android.opengl.SessionData;
import com.example.android.opengl.app.AppConfig;
import com.example.android.opengl.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Woess on 14.09.2016.
 */
public class SQLHelper {
    private OpenGLES20Activity act;

    public SQLHelper(OpenGLES20Activity activity) {
        this.act = activity;
    }

    protected void addItem(final MyItem item) {
        // Tag used to cancel the request
        String tag_string_req = "add_item";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ADDITEM, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.i("addItem", "additem Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        if(SessionData.instance().inventory.containsKey(item.item_type+item.value+item.color)){
                            MyItem existing_item = SessionData.instance().inventory.get(item.item_type+item.value+item.color);
                            existing_item.count++;
                        }
                        else{
                            SessionData.instance().inventory.put(item.item_type+item.value+item.color,item);
                        }
                        act.inventory_adapter.notifyDataSetChanged();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(act.getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("addItem", "addItem Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Log.i("addItem", "start with params");
                Map<String, String> params = new HashMap<String, String>();
                params.put("item_id", item.item_id);
                params.put("item_type", item.item_type);
                params.put("color", item.color);
                params.put("count", String.valueOf(item.count));
                params.put("value", String.valueOf(item.value));
                params.put("owner", SessionData.instance().user_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    protected void destroyThing(final String crate_id){
        String tag_string_req = "destroycrate";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DESTROYCRATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("OpenGLES2 Activity", "Response: " + response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(act.getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(act.getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("OpenGLES2 Activity", "SQL Crate Destroy Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("crate_id", crate_id);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }



    protected void getServerTime(){
        String tag_string_req = "getservertime";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GETSERVERTIME, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("OpenGLES2 Activity", "Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (error) {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");

                        Toast.makeText(act.getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                    else{
                        java.util.Date dt = new java.util.Date();
                        java.text.SimpleDateFormat sdf =
                                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentTime = sdf.format(dt);
                        Date d1 = null;
                        Date d2 = null;
                        try {
                            d1 = sdf.parse(jObj.getString("server_time"));
                            d2 = sdf.parse(currentTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long diff = d2.getTime() - d1.getTime();
                        SessionData.instance().server_difference = (int) Math.round(diff / 3600000.0);

                    }

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(act.getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("OpenGLES2 Activity", "SQL Server Time Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    protected void sendGPS(){
        String tag_string_req = "sendgps";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_SENDGPS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("OpenGLES2 Activity", "Response: " + response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        deleteOldStuff();
                        getNextCrates();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(act.getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(act.getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("OpenGLES2 Activity", "SQL GPS Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid",  SessionData.instance().user_id);
                params.put("latitude", String.valueOf(SessionData.instance().approx_location.latitude));
                params.put("longitude",  String.valueOf(SessionData.instance().approx_location.longitude));
                Log.i("PARAMS:", SessionData.instance().user_id + " " + SessionData.instance().approx_location.latitude + " " + SessionData.instance().approx_location.longitude );
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    protected void getNextCrates(){
        String tag_string_req = "getnextcrates";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_GETNEXTCRATES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("OpenGLES2 Activity", "Response: " + response.toString());


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        for(int i = 0; i<(jObj.names().length()-1)/4; i=i+1){
                            String unique_id = jObj.getString("unique_id"+i);
                            String latitude = jObj.getString("latitude"+i);
                            String longitude = jObj.getString("longitude"+i);
                            String creation_date = jObj.getString("creation_date"+i);
                            //Log.i("RECEIVED CRATE", unique_id + " " + latitude + " " + longitude);
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date creation = null;
                            try {
                                creation = sdf.parse(creation_date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if (!SessionData.instance().waiting_things.containsKey(unique_id) && !SessionData.instance().current_things.containsKey(unique_id) && unique_id != "null" && latitude != "null" && longitude != "null" && creation!=null) {
                                Random rnd = new Random();
                                int type_of_things = rnd.nextInt(10);
                                if(type_of_things<4){
                                    Star star = new Star(unique_id, Double.valueOf(latitude), Double.valueOf(longitude), creation);
                                    SessionData.instance().waiting_things.put(unique_id,star);
                                }
                                else{
                                    Crate crate = new Crate(unique_id, Double.valueOf(latitude), Double.valueOf(longitude), creation);
                                    SessionData.instance().waiting_things.put(unique_id,crate);
                                }


                            }
                        }

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(act.getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(act.getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("OpenGLES2 Activity", "SQL GPS Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("latitude", String.valueOf(SessionData.instance().approx_location.latitude));
                params.put("longitude",  String.valueOf(SessionData.instance().approx_location.longitude));
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    protected void deleteOldStuff(){
        String tag_string_req = "deleteoldstiff";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETEOLDSTUFF, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("OpenGLES2 Activity", "SQL oldstuff Error: " + error.getMessage());
                Toast.makeText(act.getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
