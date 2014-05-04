package com.test.ioio_1;


import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.codebutler.android_websockets.WebSocketClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class HistoryFragment extends Fragment {

	private String websocketPath = "ws://192.168.1.135:8008/ws2";
	private EditText _editIp;
    protected ScrollView mScrollView;
    public static final String ARG_PAGE_NUMBER = "page_number";
	private ListView listView;
	private WebSocketClient client;
	private ArrayList<HistoryList> HLA = new ArrayList<HistoryList>();

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.history_frag, container, false);
        setHasOptionsMenu(true);
        
        listView = (ListView) getActivity().findViewById(R.id.historyListView);
        int i = getArguments().getInt(ARG_PAGE_NUMBER);
        String page = getResources().getStringArray(R.array.pages_array)[i];
        
        getActivity().setTitle(page);
        
        
        
        return rootView;
    }
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
		//getActivity().getMenuInflater().inflate(R.menu.main, menu);
		//menu.findItem(R.id.action_search).setVisible(false);
	}
	
	@Override
    public void onStart(){
    	super.onStart();
    	listView = (ListView) getActivity().findViewById(R.id.historyListView);
    	try{
  	       client = new WebSocketClient(URI.create(websocketPath), new WebSocketClient.Listener(){
  	           @Override
  	           public void onConnect() {
  	           	System.out.println("commected");
  	               //Log.d(TAG, "Connected!");
  	           }
  	
  	           @Override
  	           public void onMessage(final String message) {
  	               //Log.d(TAG, String.format("Got string message! %s", message));
  	           	System.out.println(message);
  	           	
  	           try {

            	        JSONArray obj = new JSONArray(message);
            	        for (int i = 0; i < obj.length(); i++){
            	        	JSONObject currObj = obj.getJSONObject(i);
            	        	HistoryList hl = new HistoryList(currObj.getString("image"), currObj.getString("lat"), currObj.getString("lon"), currObj.getString("counts"), currObj.getString("curr_run"), currObj.getString("time_stamp"));
            	        	HLA.add(hl);
            	        }
            	     System.out.println("yup");
 	       	    } catch (Exception e) {
 	       	    	System.out.println("Could not parse malformed JSON:");
 	       	    	System.out.println(e.toString());
 	       	    	System.out.println("Could not parse malformed JSON:");
 	       	    }

            	        
 				 
            	     getActivity().runOnUiThread(new Runnable() {
            	    	    public void run() {
            	    	    	HistoryListAdapter hla = new HistoryListAdapter(getActivity(), HLA);
                       	     	System.out.println("yyup");
            	    	    	listView.setAdapter(hla);
            	    	    }
            	    	});
            	        

  	           	
  	           }
  	
  	           @Override
  	           public void onMessage(byte[] data) {
  	               //Log.d(TAG, String.format("Got binary message! %s", toHexString(data));
  	           }
  	
  	           @Override
  	           public void onDisconnect(int code, String reason) {
  	               //Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
  	           }
  	
  	           @Override
  	           public void onError(Exception error) {
  	               //Log.e(TAG, "Error!", error);
  	           	System.out.println("ERROR:: "+error);
  	           }
  	       }, null);
         
      	   client.connect();
         }catch(Exception e){
      	   
         }
    }
	

	public void sendMessage(View view) {
	    Intent intent = new Intent(getActivity(), VideoActivity.class);
	    String message = _editIp.getText().toString();
	    intent.putExtra("IP", message);
	    startActivity(intent);
	}
	
}


