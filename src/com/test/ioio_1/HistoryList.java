package com.test.ioio_1;



public class HistoryList {
	private String image, lat, lon, counts, currRun, currTime;
	
	public HistoryList(String im, String la, String lo, String count, String run, String time){
		image = im;
		lat = la;
		lon = lo;
		counts = count;
		currRun = run;
		currTime = time;
	}
	
	public String getImage(){
		return image;
	}
	
	public String getLat(){
		return lat;
	}
	
	public String getLon(){
		return lon;
	}
	
	public String getCounts(){
		return counts;
	}
	
	public String getRun(){
		return currRun;
	}
	
	public String getTime(){
		return currTime;
	}
	
	public void setImage(String im){
		image = im;
	}
	
	public void setLat(String la){
		lat = la;
	}
	
	public void setLon(String lo){
		lon = lo;
	}
	
	public void setCounts(String count){
		counts = count;
	}
	
	public void setRun(String run){
		currRun = run;
	}

	public void setTime(String time){
		currTime = time;
	}
}
