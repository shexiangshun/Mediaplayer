package com.hisense.vod.mediaplayer.video.ku6;

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
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;

public class Ku6VideoInfo extends VideoInfo {
	private static final String TAG="Ku6VideoInfo";	
	private static Ku6VideoView Ku6Player;
	private static FrameLayout mLayout;

	public Ku6VideoInfo(JSONObject json){		
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
			e.printStackTrace();
		}		
	}
	
	public static void resetPlayer(){
		if(Ku6Player!=null){
			mLayout.removeView(Ku6Player);
			Ku6Player=null;
		}
	}
	
	@Override
	public IPlayController getPlayer(FrameLayout layout, Context context,IPlayListener listener) {
		// TODO Auto-generated method stub
		if(Ku6Player==null){
			Log.i(TAG," Ku6 player is null, create new");
			mLayout=layout;
			Ku6Player = new Ku6VideoView(context,listener);
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			layout.addView(Ku6Player, 0, flParams);
			return Ku6Player;
		}
		Log.i(TAG," Ku6 player is already exist");
		return Ku6Player;
	}
	
	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportBuffering(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportResolutionChange(Context context,Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub

	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		
	}	
}
