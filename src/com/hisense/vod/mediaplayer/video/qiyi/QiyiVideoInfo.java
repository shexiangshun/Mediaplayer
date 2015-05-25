package com.hisense.vod.mediaplayer.video.qiyi;

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
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.EVENT;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.MapKey;
import com.jamdeo.tv.vod.player.thirdparty.VodSourcePlayerHelper.SOURCE;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;

public class QiyiVideoInfo extends VideoInfo implements IPlaybackInfo {
	private static final String TAG="QiyiVideoInfo";
	private String mAlbumId;
	private static Definition mDefinition;
	private String mTvId;
	private String mVid;
	private String mPaymentId;
	private static String mAuthId;
	private static QiyiVideoView qiyiView;
	private static QiyiVideoPlayer qiyiPlayer;
	
	public QiyiVideoInfo(JSONObject json){
		super(json);
		Log.i(TAG,"QIyiVideoInfo  comstructor called");
		try {
			mAlbumId=json.getString("vrsAlbumId");
			mTvId=json.getString("vrsTvId");
			mAuthId=json.getString("authId");
			mPaymentId=json.getString("paymentId");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG," constructor exception:"+e.getMessage());
		}
	}
	
	public void setDefinition(Definition def){
		mDefinition=def;
	}
	
	public static String getAuthId(){
		return mAuthId;
	}

	@Override
	public String getAlbumId() {
		// TODO Auto-generated method stub
		return mAlbumId;
	}

	public void setUrls(HashMap<String,String> urls){
		super.setUrls(urls);
		Log.i(TAG," set urls called urls:"+urls.toString());
		if(qiyiView!=null){
			qiyiView.updateDefinitions();
		}
	}
	
	@Override
	public Definition getDefinition() {
		// TODO Auto-generated method stub
		return mDefinition;
	}

	@Override
	public String getTvId() {
		// TODO Auto-generated method stub
		return mTvId;
	}

	@Override
	public String getVid() {
		// TODO Auto-generated method stub
		return mVid;
	}

	@Override
	public String getResolution() {
		// TODO Auto-generated method stub
		if(mDefinition.equals(Definition.DEFINITON_HIGH)){
			return "21";
		}else if(mDefinition.equals(Definition.DEFINITON_720P)){
			return "31";
		}else if(mDefinition.equals(Definition.DEFINITON_1080P)){
			return "41";
		}else{
			return "31";
		}
	}
	
	@Override
	public void setResolution(String res) {
		// TODO Auto-generated method stub
		mDefinition=PlayListManager.getQiyiDefinition(res);
	}
	
	@Override
	public  IPlayController getPlayer(FrameLayout layout, Context context, IPlayListener listener) {
		// TODO Auto-generated method stub		
		if(qiyiPlayer==null){
			qiyiView=new QiyiVideoView();
			Log.i(TAG," QiyiPalyer is null, create new");
		
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			qiyiPlayer=qiyiView.createPlayer(context, layout, flParams, listener);
		}
		Log.i(TAG," QiyiPalyer is exist");
		return qiyiView;
	}

	
	public static void resetPlayer() {
		// TODO Auto-generated method stub
		if(qiyiPlayer!=null){		
			qiyiPlayer.releasePlayer();			
			qiyiPlayer=null;
			qiyiView=null;			
		}
	}
	
	@Override
	public int getVideoSource() {
		// TODO 自动生成的方法存根
		return 0;
	}

	@Override
	public void journalReportStart(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,mPaymentId);
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_START, values);	
	}

	@Override
	public void journalReportSeek(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_SEEK, values);
	}

	@Override
	public void journalReportBuffering(Context context,	Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_BUFFERING, values);	
	}

	@Override
	public void journalReportExit(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,mPaymentId);
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_EXIT, values);
	}

	@Override
	public void journalReportResolutionChange(Context context,Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_RESOLUTION_CHANGE, values);
	}

	@Override
	public void journalReportEnd(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		values.put(MapKey.CONCERT_ID, " ");
		values.put(MapKey.PAYMENT_ID,mPaymentId);
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_END, values);		
	}

	@Override
	public void journalReportError(Context context, Map<MapKey, String> values) {
		// TODO Auto-generated method stub
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_ERROR, values);	
	}

	@Override
	public void journalReportPayed(Context context, Map<MapKey, String> values) {
		// TODO 自动生成的方法存根
		VodSourcePlayerHelper.journalReport(context, SOURCE.IQIYI, EVENT.VIDEO_PAYED, values);	
	}
}
