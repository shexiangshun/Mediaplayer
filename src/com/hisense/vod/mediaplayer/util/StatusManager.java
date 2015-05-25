package com.hisense.vod.mediaplayer.util;

public class StatusManager {

	public static final boolean DEBUG=true;
	
	//电视机对应的平台
	public static final String TV_NAME="PX1900";
	public static final String KU6="com.hisense.vod.mediaplayer.PLAY_KU6";
	public static final String CNTV="com.hisense.vod.mediaplayer.PLAY_CNTV";
	private static Status status=Status.UNKOWN;
	
	public static enum Status{
		UNKOWN,
		IDLE,
		PREPARING,
		PLAYING,
		PAUSE,
		BUFFERING,
		SEEKING,
		END,
		ERROR, 
		PLAY_AD;		
	}	
	
	public static Status getStatus(){
		synchronized(status){
			return status;
		}
	}
	
	public static void setStatus(Status s){
		synchronized(status){
			 status=s;
		}
	}	
}
