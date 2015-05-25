package com.hisense.vod.mediaplayer.video.wasu;

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
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;
import com.wasu.tvplayersdk.player.WasuPlayerView;

public class WasuVideoInfo extends VideoInfo {
	private static final String TAG="WasuVideoInfo";
	private String mResourceId;
	private String mResourceName;
	private String mNodeId;
	private static WasuVideoView  mVideoView;
	private static WasuPlayerView mPlayer;
	private static FrameLayout mLayout;
	
	public WasuVideoInfo(JSONObject json){
		super(json);	
		try {
			mResourceId=json.getString("resourceId");
			mResourceName=json.getString("resourceName");
			mNodeId=json.getString("nodeid");
			HashMap<String,String> urls=new HashMap<String,String>();
			urls.put("11", json.getJSONObject("urls").getString("11"));
			urls.put("21", json.getJSONObject("urls").getString("21"));
			urls.put("31", json.getJSONObject("urls").getString("31"));
			urls.put("41", json.getJSONObject("urls").getString("41"));
			setUrls(urls);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG," constructor exception:"+e.getMessage());
		}
	}
	
	public String getResourceId(){
		return mResourceId;
	}

	public String getResourceName(){
		return mResourceName;
	}
	
	public String getNodeId(){
		return mNodeId;
	}	
	
	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context, IPlayListener listener) {
		// TODO Auto-generated method stub
		if(mPlayer==null){
			Log.i(TAG," WasuPalyer is null, create new");
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			mVideoView=new WasuVideoView();
			mLayout=layout;
			mPlayer=mVideoView.createPlyaer((Activity)context, layout, flParams, listener);
			return mVideoView;
		}
		Log.i(TAG," WasuPalyer is already  exist");
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
	
	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_START, values);
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_SEEK, values);
	}

	@Override
	public void journalReportBuffering(Context context,
			Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_BUFFERING, values);
	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_EXIT, values);
	}

	@Override
	public void journalReportResolutionChange(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_RESOLUTION_CHANGE, values);
	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_END, values);		
	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_ERROR, values);
	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.WASU, EVENT.VIDEO_PAYED, values);
	}
}
