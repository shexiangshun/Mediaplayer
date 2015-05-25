package com.hisense.vod.mediaplayer.video.ad;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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

public class AdVideoInfo extends VideoInfo {

	private static final String TAG="AdVideoInfo";	
	private static AdVideoView mAdPlayer;
	private static FrameLayout mLayout;
	private int mTimeout;

	public AdVideoInfo(JSONObject json){		
		super(json);
		Log.i(TAG," CNTVVideoInfo json called");		
		HashMap<String,String> urls=new HashMap<String,String>();
		try {
			urls.put("11", json.getJSONObject("urls").getString("11"));
			urls.put("21", json.getJSONObject("urls").getString("21"));
			urls.put("31", json.getJSONObject("urls").getString("31"));
			urls.put("41", json.getJSONObject("urls").getString("41"));	
			setUrls(urls);
			mTimeout=json.getInt("timeout");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static void resetPlayer(){
		if(mAdPlayer!=null){
			mLayout.removeView(mAdPlayer);
			mAdPlayer=null;
		}
	}
	
	public int getTimeout(){
		return mTimeout;
	}
	
	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context,IPlayListener listener) {
		// TODO Auto-generated method stub
		if(mAdPlayer==null){
			Log.i(TAG," ad player is null, create new");
			mLayout=layout;
			mAdPlayer = new AdVideoView(context,listener);
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			layout.addView(mAdPlayer, 0, flParams);
			return mAdPlayer;
		}
		Log.i(TAG," AD player is already exist");
		return mAdPlayer;
	}
	
	
	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.AD, EVENT.VIDEO_START, values);
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void journalReportBuffering(Context context,	Map<MapKey, String> values) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void journalReportResolutionChange(Context context,
			Map<MapKey, String> values) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		
	}
}
