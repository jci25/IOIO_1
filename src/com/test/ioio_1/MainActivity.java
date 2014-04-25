package com.test.ioio_1;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.OnClickListener;



public class MainActivity extends Activity {


	private EditText _editIp;
    
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_editIp = (EditText) findViewById(R.id.edit_ip);
		
		Button connect = (Button) findViewById(R.id.connectB);
		
		connect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				sendMessage(arg0);
				
			}});
		
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy); 
		
		
	}
	
	public void sendMessage(View view) {
	    Intent intent = new Intent(this, VideoActivity.class);
	    String message = _editIp.getText().toString();
	    intent.putExtra("IP", message);
	    startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
}
