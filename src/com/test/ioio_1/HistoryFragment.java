package com.test.ioio_1;


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

	private EditText _editIp;
    protected ScrollView mScrollView;
    public static final String ARG_PAGE_NUMBER = "page_number";
	ListView listView;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_frag, container, false);
        setHasOptionsMenu(true);
        int i = getArguments().getInt(ARG_PAGE_NUMBER);
        String page = getResources().getStringArray(R.array.pages_array)[i];
        

		_editIp = (EditText) rootView.findViewById(R.id.edit_ip);
		_editIp.setText("192.168.1.135:9999");
		
		Button connect = (Button) rootView.findViewById(R.id.connectB);
		
		connect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				sendMessage(arg0);
				
			}});
        
        getActivity().setTitle(page);
        
        return rootView;
    }
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
		//getActivity().getMenuInflater().inflate(R.menu.main, menu);
		//menu.findItem(R.id.action_search).setVisible(false);
	}
	

	public void sendMessage(View view) {
	    Intent intent = new Intent(getActivity(), VideoActivity.class);
	    String message = _editIp.getText().toString();
	    intent.putExtra("IP", message);
	    startActivity(intent);
	}
	
}


