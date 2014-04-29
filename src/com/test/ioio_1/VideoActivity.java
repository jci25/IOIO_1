package com.test.ioio_1;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.codebutler.android_websockets.WebSocketClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class VideoActivity extends Activity implements OnNavigationListener {
	GoogleMap map;
	Polyline lineTrace;
	private boolean small = true;
	private String ip;
	private String path = "http://" + ip+":8080/?action=stream";
	private String websocketPath;
	Marker marker;
    private MjpegView mv = null;
    ProgressBar tb;
    WebSocketClient client;
    private XYPlot geigerPlot = null;
    private SimpleXYSeries geigerSeries = null;
    private static final int HISTORY_SIZE = 45;            // number of points to plot in history
    private InputDevice _device;
	private Socket _sock;
	private Thread _th;
	private RelativeLayout video, plot;
	private TextView geiger, _mode;
	public static int mode = 0;
	private static String[] modeNames = {"Stabilize", "Alt Hold", "Loiter", "RTL", "Land", "Sport"};
	public static float[] axisValues = new float[4]; //change to [5] if using left trigger
	
	public int[] gamepadAxisIndices = null;
    public float[] gamepadAxisMinVals = null;
    public float[] gamepadAxisMaxVals = null;
    public int[] gamepadButtonIndices = null;
    
    static int[] gamepadButtonMapping =
	    {
	    	KeyEvent.KEYCODE_BUTTON_1,
	    	KeyEvent.KEYCODE_BUTTON_2,
	    	KeyEvent.KEYCODE_BUTTON_3,
	    	KeyEvent.KEYCODE_BUTTON_4,
	    	KeyEvent.KEYCODE_BUTTON_5,
	    	KeyEvent.KEYCODE_BUTTON_6,
	    	KeyEvent.KEYCODE_BUTTON_7,
	    	KeyEvent.KEYCODE_BUTTON_8,
	    	KeyEvent.KEYCODE_BUTTON_9,
	    	KeyEvent.KEYCODE_BUTTON_10,
	    	KeyEvent.KEYCODE_BUTTON_11,
	    	KeyEvent.KEYCODE_BUTTON_12,
	    	KeyEvent.KEYCODE_BUTTON_13,
	    	KeyEvent.KEYCODE_BUTTON_14,
	    	KeyEvent.KEYCODE_BUTTON_15,
	    	KeyEvent.KEYCODE_BUTTON_16,
	    	KeyEvent.KEYCODE_BUTTON_A,
	    	KeyEvent.KEYCODE_BUTTON_B,
	    	KeyEvent.KEYCODE_BUTTON_C,
	    	KeyEvent.KEYCODE_BUTTON_X,
	    	KeyEvent.KEYCODE_BUTTON_Y,
	    	KeyEvent.KEYCODE_BUTTON_Z,
	    	KeyEvent.KEYCODE_BUTTON_L1,
	    	KeyEvent.KEYCODE_BUTTON_L2,
	    	KeyEvent.KEYCODE_BUTTON_R1,
	    	KeyEvent.KEYCODE_BUTTON_R2,
	    	KeyEvent.KEYCODE_BUTTON_START,
	    	KeyEvent.KEYCODE_BUTTON_SELECT,
	    	KeyEvent.KEYCODE_BUTTON_MODE,
	    	KeyEvent.KEYCODE_HOME,
	    	KeyEvent.KEYCODE_DPAD_UP,
	    	KeyEvent.KEYCODE_DPAD_DOWN,
	    	KeyEvent.KEYCODE_DPAD_LEFT,
	    	KeyEvent.KEYCODE_DPAD_RIGHT,
	    	KeyEvent.KEYCODE_DPAD_CENTER
	    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_video);
		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		ArrayList<String> itemList = new ArrayList<String>();
		for(int i = 0; i < modeNames.length; i++){
			itemList.add(modeNames[i]);
		}
		ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this, R.layout.spinner_list, R.id.label, itemList);
		actionBar.setListNavigationCallbacks(aAdpt, this);
		
		video = (RelativeLayout) findViewById(R.id.vid_lay);
		plot = (RelativeLayout) findViewById(R.id.graph_lay);
		////for maps
		// Get the location manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        LatLng latLng;
        if (location != null) {
        	latLng = new LatLng(location.getLatitude(), location.getLongitude());
          } else {
        	latLng = new LatLng(39.864052799999996, -75.12900809999996);
          }
        geiger = (TextView) findViewById(R.id.Gieger);
		((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView().setBackgroundResource(android.R.color.transparent);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		map.setOnMapLongClickListener(new OnMapLongClickListener(){

			@Override
			public void onMapLongClick(LatLng arg0) {
				if(small){
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
					((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView().setLayoutParams(params);
				}else{
					int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
					int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
					params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getView().setLayoutParams(params);
					
				}
				small = !small;
			}
			
		});
		marker = map.addMarker(new MarkerOptions().position(latLng));
		map.getUiSettings().setZoomControlsEnabled(false);
		// Move the camera instantly to hamburg with a zoom of 15.
	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

	    // Zoom in, animating the camera.
	    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	    lineTrace = map.addPolyline(new PolylineOptions().geodesic(true)
                .add(latLng).color(Color.BLUE).width((float) 1.5)
        );
		
	    
	    ///for pi comm
		Bundle extras = getIntent().getExtras();
		ip = extras.getString("IP");
		if(ip.equals("")){
			ip="192.168.1.135:9999";
		}
		String target = ip;
		String host = target.substring(0, target.indexOf(":"));
		int port = Integer.parseInt(target.substring(target.indexOf(":") + 1));
		path = "http://" + host+":8080/?action=stream";
		websocketPath = "ws://" + host+":8000/ws";
		
		////for IOIO OTG
		new connect().execute();
		_mode = (TextView)findViewById(R.id.modeView);
		_mode.setText( modeNames[mode]);
		mv = (MjpegView) findViewById(R.id.mv);
		tb = (ProgressBar) findViewById(R.id.throttleBar);
		new DoRead().execute(path);
		mv.setSource(path);
        //if(result!=null) result.setSkip(1);
		
		////for video feed
		mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
       mv.showFps(false);
       
       ////for geiger graph
       geigerPlot = (XYPlot) findViewById(R.id.geigerPlot);

       geigerSeries = new SimpleXYSeries("Geiger");
       geigerSeries.useImplicitXVals();
       
       geigerPlot.setRangeBoundaries(0, 50, BoundaryMode.AUTO);
       geigerPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
       geigerPlot.addSeries(geigerSeries,
               new LineAndPointFormatter(
                       Color.rgb(100, 100, 200), null, null, null));
       geigerPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
       geigerPlot.setDomainStepValue(HISTORY_SIZE/10);
       geigerPlot.setTicksPerRangeLabel(3);
       geigerPlot.setDomainLabel("Sample Index");
       geigerPlot.getDomainLabelWidget().pack();
       geigerPlot.setRangeLabel("Geiger Data (Cps)");
       geigerPlot.getRangeLabelWidget().pack();

       geigerPlot.setRangeValueFormat(new DecimalFormat("#"));
       geigerPlot.setDomainValueFormat(new DecimalFormat("#"));
       
      // for(int i = 0; i< 50; i++){
    	//   geigerSeries.addFirst(null, 2);
      // }
       
       ////for geiger data
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
	           	String[] mess = message.split(" ");
	           	runOnUiThread(new Runnable() {
	           	     @Override
	           	     public void run() {
	           	    	 String[] mess = message.split(" ");
	           	    	 geiger.setText(mess[0]+" cps");
	           	    	 
	           	    	 // get rid the oldest sample in history:
	           	        //if (geigerSeries.size() > HISTORY_SIZE) {
	           	        //    geigerSeries.removeFirst();
	           	        //}
	           	        // add the latest history sample:
	           	       geigerSeries.addFirst(null, Integer.valueOf(mess[0]));
	           	    	//geigerSeries.addFirst(null, 2);
	           	       geigerPlot.redraw();
	           	       mess = mess[1].split("\t");
	           	    	LatLng latLng = new LatLng(Float.valueOf(mess[0]), Float.valueOf(mess[1]));
	           	    	map.getUiSettings().setZoomControlsEnabled(false);
	           			// Move the camera instantly to hamburg with a zoom of 15.
	           		 map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
	           		 marker.setPosition(latLng);

	           		 List<LatLng> points = lineTrace.getPoints();
	           		 points.add(latLng);
	           		 lineTrace.setPoints(points);
	           		    // Zoom in, animating the camera.
	           		   // map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
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
       
       ////for shield
        String deviceInfoText = "";//dumpDeviceInfo();
		deviceInfoText += "\n";
		try{
			InputDevice joystick = findJoystick();
			_device = joystick;
			Log.i("SHIELD_CONTROL", "Joystick ID: " + joystick.getId());
			if (joystick != null) {
				deviceInfoText += "Joystick attached: " + joystick.getName() + "\n";
				deviceInfoText += dumpJoystickInfo(joystick);
			}
	
			deviceInfoText += "\n";
	
			deviceInfoText += dumpGamepadButtons();
			
			Log.i("SHIELD_CONTROL", deviceInfoText);
			
			_th = new SendSignalThread();
			
		}catch(Exception e){
			Toast.makeText(this, "NO GAMEPAD FOUND", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_view:
	            changeView();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void changeView() {
		if(video.getVisibility() == RelativeLayout.VISIBLE){
			video.setVisibility(RelativeLayout.INVISIBLE);
			plot.setVisibility(RelativeLayout.VISIBLE);
		}else{
			video.setVisibility(RelativeLayout.VISIBLE);
			plot.setVisibility(RelativeLayout.INVISIBLE);
		}
		
	}

	@Override
	protected void onPause() {
		if(mv!=null){
        	mv.stopPlayback();
        }
		super.onPause();
	}
	
	public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient(); 
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5*1000);
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                	System.out.println("turn off cam user access control");
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                System.out.println(e.toString());
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.toString());
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            //mv.setSource(result);
            //if(result!=null) result.setSkip(1);
            mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
            mv.showFps(true);
        }
    }
	
	public InputDevice findBySource(int sourceType) {
        int[] ids = InputDevice.getDeviceIds(); 

        // Return the first matching source we find...
		int i = 0;
        for (i = 0; i < ids.length; i++) {
			InputDevice dev = InputDevice.getDevice(ids[i]);
			int sources = dev.getSources();

			if ((sources & ~InputDevice.SOURCE_CLASS_MASK & sourceType) != 0) {
				_device = dev;
				return dev;
			}
        }
        
        return null;
	}
	
	public InputDevice findJoystick() {
		return findBySource(InputDevice.SOURCE_JOYSTICK);
	}
	
	public String dumpGamepadButtons() {
		String infoString = "";
		boolean buttonPrinted = false;
		int j = 0;
		int arrayCount = 0;
		for (j = 0; j < gamepadButtonMapping.length; j++) {
			int button = gamepadButtonMapping[j];
			if (KeyCharacterMap.deviceHasKey(button)) {
				arrayCount++;
			}
		}
		
		gamepadButtonIndices = new int[arrayCount];
		int arrayIndex = 0;
		
		for (j = 0; j < gamepadButtonMapping.length; j++) {
			int button = gamepadButtonMapping[j];
			if (KeyCharacterMap.deviceHasKey(button)) {
				if (!buttonPrinted)
					infoString += "Has Buttons: ";
				
				infoString += KeyEvent.keyCodeToString(button) + " ";
				buttonPrinted = true;
				
				gamepadButtonIndices[arrayIndex++] = button;
			}
		}
		
		return infoString;
	}
	
	public String dumpJoystickInfo(InputDevice dev) {
		String infoString = "";
		boolean firstAxis = true;
    
		List<InputDevice.MotionRange> ranges = dev.getMotionRanges();

		int arrayCount = ranges.size();
			
		gamepadAxisIndices = new int[arrayCount];
		gamepadAxisMinVals = new float[arrayCount];
		gamepadAxisMaxVals = new float[arrayCount];		
		
		int arrayIndex = 0;
		
		Iterator<InputDevice.MotionRange> iterator = ranges.iterator();
		while ( iterator.hasNext() ){
			InputDevice.MotionRange range = iterator.next();
			if (firstAxis)
				infoString += "\tAxes:\n";
			infoString += "\t\t" + MotionEvent.axisToString(range.getAxis()) +
				" (" + range.getMin() + ", " + range.getMax() + ")\n";
			firstAxis = false;
			
			gamepadAxisIndices[arrayIndex] = range.getAxis();
			gamepadAxisMinVals[arrayIndex] = range.getMin();
			gamepadAxisMaxVals[arrayIndex] = range.getMax();

			arrayIndex++;
		}
		infoString += "\n";
		
		return infoString;
	}
	
	
		// Close connection
		public void close(View view) {
			if (!_sock.isConnected())
				return;
			try {
				PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())));
				writer.write("exit\n");
				writer.flush();
				Log.d("ioio-client", "Sent \"exit\"");
				_th.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override 
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			super.onKeyDown(keyCode, event);
			
			if (keyCode == KeyEvent.KEYCODE_BUTTON_A)
				Log.i("SHIELD_CONTROL", "Button A is pressed");
			if (keyCode == KeyEvent.KEYCODE_BUTTON_B)
				Log.i("SHIELD_CONTROL", "Button B is pressed");
			if (keyCode == KeyEvent.KEYCODE_BUTTON_X){
				Log.i("SHIELD_CONTROL", "Button X is pressed");
				if(mode == 0){
					mode = 5;
				}else{
					mode = (mode - 1) % 6;
				}
				
				System.out.println(mode);
				_mode.setText(modeNames[mode]);
				ActionBar actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(mode);
			}
			if (keyCode ==  KeyEvent.KEYCODE_BUTTON_Y){
				Log.i("SHIELD_CONTROL", "Button Y is pressed");
				mode = (mode + 1) % 6;

				System.out.println(mode);
				_mode.setText( modeNames[mode]);
				ActionBar actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(mode);
			}
			if (keyCode == KeyEvent.KEYCODE_BUTTON_L1)
				Log.i("SHIELD_CONTROL", "Button L1 is pressed");
			if (keyCode == KeyEvent.KEYCODE_BUTTON_R1)
				Log.i("SHIELD_CONTROL", "Button R1 is pressed");
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
				Log.i("SHIELD_CONTROL", "Button DPAD UP is pressed");
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
				Log.i("SHIELD_CONTROL", "Button DPAD DOWN is pressed");
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				Log.i("SHIELD_CONTROL", "Button DPAD LEFT is pressed");
				
			}
			if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				Log.i("SHIELD_CONTROL", "Button DPAD Right is pressed");
				
			}
			return true;
		}
		
		@Override
		public boolean onGenericMotionEvent(MotionEvent event) {
			int source = event.getSource();
			
			if ((source & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
							
				InputDevice.MotionRange range = _device.getMotionRange(MotionEvent.AXIS_X, source);
				if (range != null) {
					axisValues[3] = event.getAxisValue(MotionEvent.AXIS_X);
					//_yaw.setText("- Yaw = " + axisValues[3]);
					Log.i("SHIELD_CONTROL", "Left Stick X: " + axisValues[3]);
				}
							
				range = _device.getMotionRange(MotionEvent.AXIS_RZ, source);
				if (range != null) {
					axisValues[0] = event.getAxisValue(MotionEvent.AXIS_Z);
					//_roll.setText("- Roll = " + axisValues[0]);
					Log.i("SHIELD_CONTROL", "Right Stick X: " + axisValues[0]);
				}
				
				range = _device.getMotionRange(MotionEvent.AXIS_Z, source);
				if (range != null) {
					axisValues[1] = event.getAxisValue(MotionEvent.AXIS_RZ);
					//_pitch.setText("- Pitch = " + axisValues[1]);
					Log.i("SHIELD_CONTROL", "Right Stick Y: " + axisValues[1]);
				}
				
				/*range = _device.getMotionRange(MotionEvent.AXIS_LTRIGGER, source);
				if (range != null) {
					float axisValue = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
					Log.i("SHIELD_CONTROL", "Left Trigger: " + axisValue);
				}*/
				
				range = _device.getMotionRange(MotionEvent.AXIS_RTRIGGER, source);
				if (range != null) {
					axisValues[2] = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);
					//_throttle.setText("- Throttle = " + axisValues[2]);
					tb.setProgress((int) (axisValues[2] * 2000));
					Log.i("SHIELD_CONTROL", "Right Trigger: " + axisValues[2]);
				}
			}
			return false;
		}
		
		/*public class SendSignalThread extends Thread {
			
			private float[] pastAxisValues = new float[5];
			
			private float toDutyCycle(float axisVal, int channel) {
				
				float adjustedValue = 0.0f;
				float step = 0.0f;
				float offset = 0.0f;
				pastAxisValues[channel - 1] = axisVal;
				
				if (channel == 3) {
					adjustedValue = axisVal * 50;
					step = (0.094f - 0.053f) / 50;
					offset = 0.053f;
				}
				else if (channel == 4) {
					adjustedValue = (1.0f + axisVal) / 2.0f * 50;
					step = (0.094f - 0.053f) / 50;
					offset = 0.053f;
				}
				else {
					adjustedValue = (1.0f + axisVal) / 2.0f * 50;
					step = (0.094f - 0.051f) / 50;
					offset = 0.051f;
				}
				return adjustedValue * step + offset;
			}
			
			private void sendSignal(int channel, float dc) {
				if (!_sock.isConnected())
					return;
				try {
					PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())));
					writer.write(channel + ":" + dc +"\n");
					writer.flush();
					Log.d("ioio-client", "Sent \"" + channel + ":" + dc + "\"");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			public void run() {
				while (true) {
					if (interrupted())
						break;
					for (int i = 0; i < 4; i++)
						if (Math.abs(VideoActivity.axisValues[i] - pastAxisValues[i]) > 0.01)
							sendSignal(i+1, toDutyCycle(axisValues[i], i+1));
					try {
						sleep(20);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		
		}
		*/
		public class SendSignalThread extends Thread {
			
			private float[] pastAxisValues = new float[4];
			private int pastModeValue = 0;
		
			private float toDutyCycle(float axisVal, int channel) {
				
				float adjustedValue = 0.0f;
				float step = 0.0f;
				float offset = 0.0f;
				
				if(channel == 5){
					pastModeValue = (int)axisVal;
				}else{
					pastAxisValues[channel - 1] = axisVal;
				}
				
				if (channel == 3) {
					adjustedValue = axisVal * 50;
					step = (0.094f - 0.053f) / 50;
					offset = 0.053f;
				}
				else if (channel == 4) {
					adjustedValue = (1.0f + axisVal) / 2.0f * 50;
					step = (0.094f - 0.053f) / 50;
					offset = 0.053f;
				}
				else if (channel == 5) {
					if (axisVal == 0.0)
						return 0.061f;
					else if (axisVal == 1.0)
						return 0.066f;
					else if (axisVal == 2.0)
						return 0.072f;
					else if (axisVal == 3.0)
						return 0.078f;
					else if (axisVal == 4.0)
						return 0.086f;
					else
						return 0.092f;
				}
				else {
					adjustedValue = (1.0f + axisVal) / 2.0f * 50;
					step = (0.094f - 0.051f) / 50;
					offset = 0.051f;
				}
				return adjustedValue * step + offset;
			}
			
			protected void sendSignal(int channel, float dc) {
				if (!_sock.isConnected())
					//return;
				try {
					PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())));
					writer.write(channel + ":" + dc +"\n");
					writer.flush();
					Log.d("ioio-client", "Sent \"" + channel + ":" + dc + "\"");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			private void closeConnection() {
				try {
					PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_sock.getOutputStream())));
					writer.write("exit\n");
					writer.flush();
					Log.d("ioio-client", "Sent \"exit\"");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			public void run() {
				int count = 0;
				while (true) {
					if(++count == 10){
						//one heart beat
						count = 0;
						Calendar c = Calendar.getInstance();
						//System.out.println(c.getTimeInMillis());
						sendSignal(-1, c.getTimeInMillis());
					}
					if (isInterrupted()) {
						closeConnection();
						break;
					}
					else {
						for (int i = 0; i < 5; i++) {
							if (i == 4 && Math.abs(mode - pastModeValue) >= 1)
								sendSignal(i+1, toDutyCycle(mode, i+1));
							else if (i < 4 && Math.abs(axisValues[i] - pastAxisValues[i]) > 0.01)
								sendSignal(i+1, toDutyCycle(axisValues[i], i+1));
						}
					}
					try {
						sleep(10);
					} catch (InterruptedException e) {
						closeConnection();
						break;
					}
				}
			}
		
		}
		public class connect extends AsyncTask<String, Void, String> {
			
			@Override
			protected String doInBackground(String... input) {
				//TODO: get info and parse web
				String target = ip;
				String host = target.substring(0, target.indexOf(":"));
				int port = Integer.parseInt(target.substring(target.indexOf(":") + 1));
				_sock = new Socket();
				try {
					_sock.connect(new InetSocketAddress(host, port));
					Log.d("ioio-client", "Connected");
					_th.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onProgressUpdate(Void... values) {
			}
			
			@Override
			protected void onPostExecute(String result) {
				
			}



		}
		
		@Override
		public void onBackPressed()
		{
		     // code here to show dialog
			try{
				client.send("Close");
				client.disconnect();
			}catch(Exception e){
				
			}
			
		     super.onBackPressed();  // optional depending on your needs
		}

		@Override
		public boolean onNavigationItemSelected(int pos, long id) {
			// TODO Auto-generated method stub
			mode = pos;
			_mode.setText( modeNames[mode]);
			return false;
		}


}
