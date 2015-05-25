package com.hisense.vod.mediaplayer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class TimeProcessor {
	public static final String TAG="TimeProcessor";
	
	public TimeProcessor(){	
	}
	
	public String getTime(){
		Date date=new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");//设置日期格式
		String time =df.format(date);	
		String s1[]=time.split(" ");
		String s[]=s1[1].split(":");
		String ret=s[0]+":"+s[1];	
		return ret;
	}
	
	public String getTimeString(int time) {
		// TODO Auto-generated method stub
		
		//if(StatusManager.DEBUG) Log.i(TAG," format time is "+time);
		String ret="";
		int allHours=(int)(time/1000);
		int hours=(int)(allHours/3600);
		if(hours>=0&&hours<=9){
			ret="0"+String.valueOf(hours)+":";
		}else{
			ret=String.valueOf(hours)+":";
		}
		int allMinutes=allHours%3600;
		int minutes=(int)(allMinutes/60);
		if(minutes>=0&&minutes<=9){
			ret=ret+"0"+String.valueOf(minutes)+":";
		}else{
			ret=ret+String.valueOf(minutes)+":";
		}		
		int seconds=allMinutes%60;
		if(seconds>=0&&seconds<=9){
			ret=ret+"0"+String.valueOf(seconds);
		}else{
			ret=ret+String.valueOf(seconds);
		}
		//if(StatusManager.DEBUG) Log.i(TAG," ret is "+ret);
		return ret;
	}

	public String getTipString(int pos) {
		// TODO 自动生成的方法存根
		String ret="第";
		int allHours=(int)(pos/1000);
		int hours=(int)(allHours/3600);
		if(hours>0&&hours<=9){
			ret+=String.valueOf(hours)+"小时";
		}
		int allMinutes=allHours%3600;
		int minutes=(int)(allMinutes/60);
		if(minutes>0){			
			ret+=String.valueOf(minutes)+"分钟";
		}		
		return ret;
	}
}
