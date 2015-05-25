package com.hisense.vod.mediaplayer.video;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.util.Config.PlayCrop;
import com.hisense.vod.mediaplayer.video.ad.AdVideoInfo;
import com.hisense.vod.mediaplayer.video.cntv.CNTVVideoInfo;
import com.hisense.vod.mediaplayer.video.ku6.Ku6VideoInfo;
import com.hisense.vod.mediaplayer.video.letv.LetvVideoInfo;
import com.hisense.vod.mediaplayer.video.qiyi.QiyiVideoInfo;
import com.hisense.vod.mediaplayer.video.sohu.SohuVideoInfo;
import com.hisense.vod.mediaplayer.video.wasu.WasuVideoInfo;
import com.qiyi.video.player.data.Definition;

public class PlayListManager {
	private static final int MAX_ITEMS=100;
	private static final String TAG="PlayListManager";
	private int mCount;
	private static String mResolution;
	private JSONObject mList;
	private int length;
	private int mPosition;
	private static DisplaySize mode=DisplaySize.ORIGINAL;								//默认的显示比例是自适应的比例
	private static PlayCrop crop=PlayCrop.SKIP;
	private static String mPlatform="unknown";	
	private static String mLicence;														//拍照方， cntv或者wasu
	private static String mResourceType;												//播放器启动来源，1为详情页，2为微信，3为语音
	private static String mPlayType;															//播放的方式，预览、直播、回看等
	private static String mPayType;															//支付的方式，免费、试看、已付费
	
	public PlayListManager(JSONObject json){
		try {
			mResolution=json.getString("resolution");
			JSONArray videos=json.getJSONArray("videos");
			length=videos.length();
			mList=json;
			mCount=json.getInt("index");
			if(mCount>length-1){
				mCount=0;
			}	
			mPosition=json.getInt("position");
			mLicence=json.getString("licence");
			mResourceType=json.getString("resourceType");
			mPlayType=json.getString("playType");
			mPayType=json.getString("payType");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG," playlist manager constructor exception: "+e.getMessage());
		}
		
		try{
			String s=json.getString("skip_header");
			if(s.equals("0")){
				crop=PlayCrop.SKIP;
			}else{
				crop=PlayCrop.NOT_SKIP;
			}
		}catch(JSONException e){
			Log.e(TAG,"get skip info exception: "+e.getMessage());
			crop=PlayCrop.NOT_SKIP;
		}	
		
		try{
			mPlatform=json.getString("platform");	
		}catch(JSONException e){
			Log.e(TAG,"get platform exception: "+e.getMessage());
			mPlatform="unknown";
		}
	}
	
	public void setCount(int count){
		mCount=count;
	}
	
	public int getCurrentCount(){
		return mCount;
	}
	
	public static String getPlayType(){
		return mPlayType;
	}
	
	public static String getPayType(){
		return mPayType;
	}
	
	public int getPlaybackPosition(){
		return mPosition;
	}
	
	public static DisplaySize getDisplaySize(){
		return mode;
	}
	
	public static void setDisplaySize(DisplaySize s){
		mode=s;
	}
	
	public static PlayCrop getPlayCrop(){
		return crop;
	}
	
	public static void setPlayCrop(PlayCrop c){
		crop=c;
	}
	
	public static String getLicence(){
		return mLicence;
	}
	
	public static String getResourceType(){
		return mResourceType;
	}
	
	public int getCount(){
		return length;
	}
	
	public  static String getResolution(){
		return mResolution;
	}
	
	public static void setResolution(String res, VideoInfo mVideoInfo){
		mResolution=res;
		mVideoInfo.setResolution(res);
	}
	
	public boolean hasNext(){
		return (mCount<length-1)&&(mCount<MAX_ITEMS);
	}

	public VideoInfo getCurrentVideo(){
		JSONArray videos=null;
		try {
			videos = mList.getJSONArray("videos");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG," get videos exception:"+e.getMessage());
		}
		VideoInfo info=getVideo(videos);
		return info;
	}
	
	private VideoInfo getNextVideo(){
		if(hasNext()){
			mCount++;
			JSONArray videos=null;
			try {
				videos = mList.getJSONArray("videos");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			VideoInfo info=getVideo(videos);
			return info;
		}
		return null;
	}
	
	private VideoInfo getVideo(JSONArray videos) {
		// TODO Auto-generated method stub		
		if(videos==null||length==0){
			return null;
		}		
		try {
			JSONObject video=videos.getJSONObject(mCount);
			Log.i(TAG," video is "+video.toString());
			String vendor=video.getString("vendor");		
			if(vendor.equals("CNTV")){
				CNTVVideoInfo cntv=new CNTVVideoInfo(video);
				cntv.setResolution(mResolution);
				return cntv;
			}else if(vendor.equals("QIYI")){
				QiyiVideoInfo qiyi=new QiyiVideoInfo(video);
				qiyi.setDefinition(getQiyiDefinition(mResolution));
				return qiyi;
			}else if(vendor.equals("WASU")){
				WasuVideoInfo wasu=new WasuVideoInfo(video);
				wasu.setResolution(mResolution);
				return wasu;
			}else if(vendor.equals("SOHU")){
				SohuVideoInfo sohu=new SohuVideoInfo(video);
				sohu.setResolution(mResolution);
				return sohu;
			}else if(vendor.equals("LETV")){
				LetvVideoInfo letv=new LetvVideoInfo(video);
				letv.setResolution(mResolution);
				return letv;
			}else if(vendor.equals("KU6")){
				Ku6VideoInfo ku6=new Ku6VideoInfo(video);
				ku6.setResolution(mResolution);
				return ku6;
			}else if(vendor.equals("AD")){
				AdVideoInfo ad=new AdVideoInfo(video);
				ad.setResolution(mResolution);
				return ad;
			}else{
				Log.i(TAG," the vendor is not support!");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG," get video exception:"+e.getMessage());
		}		
		return null;
	}
	
	public boolean shouldResetPlayerAndMoveToNext(){
		try{
			VideoInfo cur=getCurrentVideo();
			VideoInfo next=getNextVideo();
			if(next==null){
				return false;
			}
			
			if(!cur.getVendor().equals(next.getVendor())){
				return true;
			}
			return false;
		}catch(Exception e){
			Log.e(TAG," shouldResetPlayerAndMoveToNext exception:"+e.getMessage());
			return false;
		}
	}

	public static Definition getQiyiDefinition(String res) {
		// TODO Auto-generated method stub
		if(res.equals("11")){
			return Definition.DEFINITON_HIGH;
		}else if(res.equals("21")){
			return Definition.DEFINITON_HIGH;
		}else if(res.equals("31")){
			return Definition.DEFINITON_720P;
		}else{
			return Definition.DEFINITON_1080P;
		}
	}

	public static void resetPlayer() {
		// TODO Auto-generated method stub
		CNTVVideoInfo.resetPlayer();
		WasuVideoInfo.resetPlayer();
		QiyiVideoInfo.resetPlayer();
		SohuVideoInfo.resetPlayer();
		Ku6VideoInfo.resetPlayer();
		AdVideoInfo.resetPlayer();
		LetvVideoInfo.resetPlayer();
	}

	public static int getSohuDifinition(String res) {
		// TODO Auto-generated method stub
		if(res.equals("41")){
			return 31;
		}else if(res.equals("31")){
			return 21;
		}else if(res.equals("21")){
			return 2;
		}else{
			return 1;
		}
	}
	
	public static String getLetvDefinition(String res){
		if(res.equals("41")){
			return "1080p";
		}else if(res.equals("31")){
			return "720p";
		}else if(res.equals("21")){
			return "1300";
		}else{
			return "1000";
		}
	}

	public static String getPlatform() {
		// TODO 自动生成的方法存根
		return mPlatform;
	}
}
