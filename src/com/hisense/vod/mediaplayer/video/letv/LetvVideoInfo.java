package com.hisense.vod.mediaplayer.video.letv;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.hisense.vod.mediaplayer.video.cntv.CNTVVideoView;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;

public class LetvVideoInfo extends VideoInfo {
	private static final String TAG="LetvVideoInfo";
	private static final String  LETV_SD="1000";
	private static final String  LETV_HD="1300";
	private static final String  LETV_FHD="720p";
	private static final String  LETV_BD="1080p";
	private static final String  LETV_FHD_CAPITAL="720P";
	private static final String  LETV_BD_CAPITAL="1080P";
	private String mLiveId;
	private String mStartTime;
	private String mEndTime;
	private String mRef;
	private static LetvVideoView mPlayer;
	private static FrameLayout mLayout;
	
	public LetvVideoInfo(JSONObject json){
		super(json);
		try{
			mLiveId=json.getString("liveId");
			mEndTime=json.getString("endTime");
			mStartTime=json.getString("startTime");
			JSONObject streams=json.getJSONObject("streams");
			setUrls(getLetvUrls(streams));
			mRef=json.getString("ref");
		}catch(Exception e){
			Log.e(TAG,"Constructor exception: "+e.getMessage());
		}
	}
	
	public static void resetPlayer(){
		if(mPlayer!=null){
			mLayout.removeView(mPlayer);
			mPlayer=null;
		}
	}
	
	private HashMap<String, String> getLetvUrls(JSONObject json) {
		// TODO Auto-generated method stub
		HashMap<String,String> map=new HashMap<String,String>();
		if(json.has(LETV_SD)){
			map.put(Config.SD, LETV_SD);
		}else{
			map.put(Config.SD, Config.NONE_URL);
		}
		if(json.has(LETV_HD)){
			map.put(Config.HD, LETV_HD);
		}else{
			map.put(Config.HD, Config.NONE_URL);
		}
		if(json.has(LETV_FHD)||json.has(LETV_FHD_CAPITAL)){
			map.put(Config.FHD, LETV_FHD);
		}else{
			map.put(Config.FHD, Config.NONE_URL);
		}
		if(json.has(LETV_BD)||json.has(LETV_BD_CAPITAL)){
			map.put(Config.BD, LETV_BD);
		}else{
			map.put(Config.BD, Config.NONE_URL);
		}
		return map;
	}
	
	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context, IPlayListener listener) {
		// TODO Auto-generated method stub
		if(mPlayer==null){
			Log.i(TAG," letv player is null, create new");
			mLayout=layout;
			mPlayer = new LetvVideoView(context,(IPlayListener)listener);
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			layout.addView(mPlayer, 0, flParams);
			return mPlayer;
		}
		Log.i(TAG," letv player is already exist");
		return mPlayer;
	}

	public String getLiveId(){
		return mLiveId;
	}
	
	public String getStartTime(){
		return mStartTime;
	}
	
	public String getEndTime(){
		return mEndTime;
	}
	
	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		values.put(MapKey.CONCERT_ID, mLiveId);
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_START, values);
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_SEEK, values);
	}

	@Override
	public void journalReportBuffering(Context context,
			Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_BUFFERING, values);
	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		values.put(MapKey.CONCERT_ID, mLiveId);
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_EXIT, values);
	}

	@Override
	public void journalReportResolutionChange(Context context,
			Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_RESOLUTION_CHANGE, values);
	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		values.put(MapKey.CONCERT_ID, mLiveId);
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_END, values);
	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_ERROR, values);
	}

	public String getRef() {
		// TODO 自动生成的方法存根
		return mRef;
	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.LETV, EVENT.VIDEO_PAYED, values);
	}
}
