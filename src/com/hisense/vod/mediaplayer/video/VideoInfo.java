package com.hisense.vod.mediaplayer.video;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.StatusManager;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;
import com.qiyi.video.player.IVideoStateListener;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.VideoView;

public abstract class VideoInfo {
	private static final String TAG="VideoInfo";
	private String mProgramId;
	private String mEpisodeId;
	private String mTitle;
	private String mSubTitle;
	private String mPrice;
	private String mVendor;
	private HashMap<String,String> mUrls;
	private static String mResolution;
	
	public VideoInfo(){
		
	}
	
	public VideoInfo(JSONObject json){
		try {
			mVendor=json.getString("vendor");
			mProgramId=json.getString("program_id");
			mEpisodeId=json.getString("episode_id");
			mTitle=json.getString("title");
			try{
				mPrice=json.getString("fee");
			}catch(Exception e){
				Log.i(TAG,"get price error, not String type");
				mPrice=String.valueOf(json.getInt("fee"));
			}
			mSubTitle=json.getString("subTitle");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG," json exception:"+e.getMessage());
		}
	}
	
	public void setResolution(String res) {
		// TODO Auto-generated method stub
		mResolution=res;
	}
	
	public String getResolution() {
		// TODO Auto-generated method stub
		return mResolution;
	}
	
	public String getProgramId(){
		return mProgramId;
	}
	
	public String getEpisodeId(){
		return mEpisodeId;
	}
	
	public String getTitle(){
		return mTitle;
	}

	public String getSubTtle(){
		return mSubTitle;
	}
	
	public String getPrice(){
		return mPrice;
	}
	
	public String getVendor(){
		return mVendor;
	}

	public HashMap<String, String> getUrls() {
		// TODO Auto-generated method stub
		return mUrls;
	}
	
	public void setUrls(HashMap<String,String> urls){
		mUrls=urls;
	}
	
	public  void reportVideoStart(Context context,String payType, String playType){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		long ts = System.currentTimeMillis()/1000;
		values.put(MapKey.TIMESLOT, Long.toString(ts));
		values.put(MapKey.PAY_TYPE,payType);
		values.put(MapKey.PLAY_TYPE,playType);
		values.put(MapKey.RSOURCE_TYPE, PlayListManager.getResourceType());
		values.put(MapKey.LICENCE,PlayListManager.getLicence());
		journalReportStart(context, values);
	}
	
	public void reportVideoSeek(Context context,long start,long end){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID,getProgramId() );
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		values.put(MapKey.START_TIMESLOT, Long.toString(start/1000));
		values.put(MapKey.END_TIMESLOT, Long.toString(end/1000));
		journalReportSeek(context, values);
	}
	
	public void reportVideoBuffering(Context context,long start,long end){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		values.put(MapKey.START_TIMESLOT, Long.toString(start/1000));
		values.put(MapKey.END_TIMESLOT, Long.toString(end/1000));
		journalReportBuffering(context,values);
	}
	
	public void reportVideoExit(Context context,long position,String payType,String playType){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		long ts = System.currentTimeMillis()/1000;
		values.put(MapKey.TIMESLOT, Long.toString(ts));
		values.put(MapKey.POSITION, Long.toString(position));         //要改为position
		values.put(MapKey.PAY_TYPE,payType);
		values.put(MapKey.PLAY_TYPE,playType);
		values.put(MapKey.RSOURCE_TYPE, PlayListManager.getResourceType());
		values.put(MapKey.LICENCE,PlayListManager.getLicence());
		journalReportExit(context, values);
	}
	
	public void reportVideoResolutionChange(Context context,String oldResolution, String newResolution){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		values.put(MapKey.OLD_RESOLUTION, oldResolution);
		values.put(MapKey.NEW_RESOLUTION, newResolution);
		journalReportResolutionChange(context, values);
	}
	
	public void reportVideoEnd(Context context,String  payType, String playType){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		long ts = System.currentTimeMillis()/1000;
		values.put(MapKey.TIMESLOT, Long.toString(ts));
		values.put(MapKey.PAY_TYPE,payType);
		values.put(MapKey.PLAY_TYPE,playType);
		values.put(MapKey.RSOURCE_TYPE, PlayListManager.getResourceType());
		values.put(MapKey.LICENCE,PlayListManager.getLicence());
		journalReportEnd(context, values);
	}
	
	public void reportVideoError(Context context,int position,String what,String extra,String msg){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		values.put(MapKey.POSITION, Long.toString(position));
		long ts = System.currentTimeMillis()/1000;
		values.put(MapKey.TIMESLOT, Long.toString(ts));
		values.put(MapKey.ERROR_CODE, what);
		values.put(MapKey.ERROR_EXTRA, extra);
		values.put(MapKey.ERROR_MESSAGE, msg);
		journalReportError(context, values);
	}
	
	public void reportVideoPayed(Context context,int payResult){
		Map<MapKey, String> values = new HashMap<MapKey, String>();
		values.put(MapKey.PROGRAM_ID, getProgramId());
		values.put(MapKey.EPISODE_ID, getEpisodeId());
		values.put(MapKey.RESOLUTION, PlayListManager.getResolution());
		long ts = System.currentTimeMillis()/1000;
		values.put(MapKey.TIMESLOT, Long.toString(ts));
		values.put(MapKey.PAY_RESULT,""+payResult);
		values.put(MapKey.PRICE, ""+getPrice());
		journalReportError(context, values);
	}
	
	public static int[] calcSizeForAuto(int videoWidth, int videoHeight) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"calcSizeForAuto called videoWidth="+videoWidth+" videoHeight"+videoHeight);
		int[] ret={0,0};
		float rate_16_9=(float)16/(float)9;
		float rate=(float)videoWidth/(float)videoHeight;
		Log.i(TAG," rate_16_9="+rate_16_9+" and rate="+rate);
		if(rate>=rate_16_9){
			ret[0]=1920;
			ret[1]=(int)(1920/rate);
		}else{
			ret[0]=(int)(1080*rate);
			ret[1]=1080;
		}
		return ret;
	}
	
	public abstract void journalReportStart(Context context, Map<MapKey, String> values);
	public abstract void journalReportSeek(Context context, Map<MapKey, String> values);
	public abstract void journalReportBuffering(Context context, Map<MapKey, String> values);
	public abstract void journalReportExit(Context context, Map<MapKey, String> values);
	public abstract void journalReportResolutionChange(Context context, Map<MapKey, String> values);
	public abstract void journalReportEnd(Context context, Map<MapKey, String> values);
	public abstract void journalReportError(Context context, Map<MapKey, String> values);
	public abstract void journalReportPayed(Context context, Map<MapKey, String> values);
	public abstract IPlayController getPlayer(FrameLayout layout ,Context context,IPlayListener listener);
//	public abstract void resetPlayer();
}
