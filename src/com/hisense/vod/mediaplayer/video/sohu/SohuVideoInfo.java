package com.hisense.vod.mediaplayer.video.sohu;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;
import com.sohutv.tv.player.partner.SohuTvPlayer;

public class SohuVideoInfo extends VideoInfo {
	private static final String TAG="SohuVideoInfo";
	private static final String SOHU_SD="2";
	private static final String SOHU_HD="1";
	private static final String SOHU_FHD="21";
	private static final String SOHU_BD="31";
	private int mSid;							//sohu的sid
	private int mCid;							//sohu的Cid 
	private int mVid;							//sohu的Vid
	private int mCateCode;						//sohu的catecode
	
	private static SohuVideoView  mVideoView;
	private static SohuTvPlayer mPlayer;
	private static FrameLayout mLayout;	
	
	public SohuVideoInfo(JSONObject json){
		super(json);
		Log.i(TAG,"SohuVideoInfo called ");
		try {
			mSid=json.getInt("sid");
			mCid=json.getInt("cid");
			mVid=json.getInt("vid");
			mCateCode=json.getInt("catecode");
			String urls=json.getString("definition");
			setUrls(getSohuUrls(urls));			
		} catch (JSONException e) {
			Log.i(TAG,"SohuVideoInfo exception:"+e.getMessage());			
		}
	}	
	
	private HashMap<String, String> getSohuUrls(String urls) {
		// TODO Auto-generated method stub
		HashMap<String,String> map=new HashMap<String,String>();
		String[] s=urls.split(",");
		
		if(containResolution(s,SOHU_SD)){
			map.put(Config.SD, SOHU_SD);
		}else{
			map.put(Config.SD, Config.NONE_URL);
		}
		
		if(containResolution(s,SOHU_HD)){
			map.put(Config.HD, SOHU_HD);
		}else{
			map.put(Config.HD, Config.NONE_URL);
		}
		
		if(containResolution(s,SOHU_FHD)){
			map.put(Config.FHD, SOHU_FHD);
		}else{
			map.put(Config.FHD, Config.NONE_URL);
		}
		
		if(containResolution(s,SOHU_BD)){
			map.put(Config.BD, SOHU_BD);
		}else{
			map.put(Config.BD, Config.NONE_URL);
		}
		
		return map;
	}


	private boolean containResolution(String[] ss, String sohu_res) {
		// TODO Auto-generated method stub
		for(String s:ss){
			if(s.equals(sohu_res)){
				return true;
			}
		}		
		return false;
	}


	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context, IPlayListener listener) {
		// TODO Auto-generated method stub
		if(mPlayer==null){
			Log.i(TAG," SohuPalyer is null, create new");
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			mVideoView=new SohuVideoView();
			mLayout=layout;			
			mPlayer=mVideoView.createPlyaer((Activity)context, layout, flParams, listener);
			return mVideoView;
		}
		Log.i(TAG," SohuPalyer is already  exist");
		return mVideoView;
	}
	
	public static void resetPlayer() {
		// TODO Auto-generated method stub
		if(mPlayer!=null){
			mLayout.removeView(mPlayer);
			mPlayer=null;
			mVideoView=null;
		}
	}
	
	public int getSid(){
		return mSid;
	}
	
	public int getCid(){
		return mCid;
	}
	
	public int getVid(){
		return mVid;
	}
	
	public int getCateCode(){
		return mCateCode;
	}

	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_START, values);
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_SEEK, values);
	}

	@Override
	public void journalReportBuffering(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_BUFFERING, values);
	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_EXIT, values);
	}

	@Override
	public void journalReportResolutionChange(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_RESOLUTION_CHANGE, values);
	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_END, values);
	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_ERROR, values);
	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.SOHU, EVENT.VIDEO_PAYED, values);
	}
}
