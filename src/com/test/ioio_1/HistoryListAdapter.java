package com.test.ioio_1;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryListAdapter extends ArrayAdapter<HistoryList> {
	  private final Context context;
	  private final ArrayList<HistoryList> hl;
	  Bitmap bitmap;
	  public HistoryListAdapter(Context context, ArrayList<HistoryList> hl) {
	    super(context, R.layout.history_list_row, hl);
	    this.context = context;
	    this.hl = hl;
	    
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    LayoutInflater inhlater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    //hl.get(position).getID();
	    
	    View rowView = inhlater.inflate(R.layout.history_list_row, parent, false);
	    rowView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//do something here if you want to
			}
	    	
	    });
	    TextView run = (TextView) rowView.findViewById(R.id.currRun);
	    TextView time = (TextView) rowView.findViewById(R.id.currTime);
	    TextView gps = (TextView) rowView.findViewById(R.id.GPS);
	    TextView counts = (TextView) rowView.findViewById(R.id.counts);
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.profilePicture);
	    run.setText(hl.get(position).getRun());
	    time.setText(hl.get(position).getTime());
	    gps.setText(hl.get(position).getLat() + " " + hl.get(position).getLon());
	    counts.setText(hl.get(position).getCounts());
	    
	    
	    	byte[] decodedString = Base64.decode(hl.get(position).getImage(), Base64.DEFAULT);
		    //Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		    InputStream inputStream  = new ByteArrayInputStream(decodedString);
		    Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
		    imageView.setImageBitmap(bitmap);
	    
	    //UrlImageViewHelper.setUrlDrawable(imageView, bitmap);
	    
	   
	    
	    return rowView;
	  }
	} 
