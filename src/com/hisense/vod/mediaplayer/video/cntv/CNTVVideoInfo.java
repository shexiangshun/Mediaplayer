package com.hisense.vod.mediaplayer.video.cntv;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.StatusManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;


public class CNTVVideoInfo extends VideoInfo {
	
	private static final String TAG="CNTVVideoInfo";	
	private static CNTVVideoView CNTVPlayer;
	private static FrameLayout mLayout;

	public CNTVVideoInfo(JSONObject json){
		super(json);
		Log.i(TAG," CNTVVideoInfo json called");
		HashMap<String,String> urls=new HashMap<String,String>();
		try {
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
	
	public static void resetPlayer(){
		if(CNTVPlayer!=null){
			mLayout.removeView(CNTVPlayer);
			CNTVPlayer=null;
		}
	}
	
	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context, IPlayListener listener) {
		// TODO Auto-generated method stub
		if(CNTVPlayer==null){
			Log.i(TAG," CNTV player is null, create new");
			mLayout=layout;
			CNTVPlayer = new CNTVVideoView(context,(IPlayListener)listener);
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			layout.addView(CNTVPlayer, 0, flParams);
			return CNTVPlayer;
		}
		Log.i(TAG," CNTV player is already exist");
		return CNTVPlayer;
	}	

	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub		
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_START, values);
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_SEEK, values);
	}

	@Override
	public void journalReportBuffering(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_BUFFERING, values);
	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_EXIT, values);
	}

	@Override
	public void journalReportResolutionChange(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_RESOLUTION_CHANGE, values);
	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,"-1");
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_END, values);	
	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_ERROR, values);
	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.CNTV, EVENT.VIDEO_PAYED, values);
	}
}
