package com.hisense.vod.mediaplayer.video.sohu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.sohutv.tv.player.interfaces.ISohuTVPlayerCallback;
import com.sohutv.tv.player.partner.SohuTvPlayer;

public class SohuVideoView implements IPlayController, ISohuTVPlayerCallback {
	private static final String TAG="SohuVideoView";
	private SohuTvPlayer mPlayer;
	private SohuVideoInfo mVideo;
	private Activity mActivity;
	private IPlayListener mListener;	
	private int mRemainTime=-1;	
	private boolean mAdStartCalled=false;
	private boolean mAdEndCalled=false;
	private boolean mPaused=false;
	private int mScreenWidth,mScreenHeight;
	private int mVideoWidth,mVideoHeight;
	
	public SohuTvPlayer createPlyaer(Activity context, FrameLayout layout,LayoutParams flParams, IPlayListener listener) {
		// TODO Auto-generated method stub
		mActivity=context;
		mListener=listener;
		mPlayer=new SohuTvPlayer(mActivity);
		mPlayer.setPauseADTopMarginPercent(20);
		mPlayer.setPlayerCallback(this);
		layout.addView(mPlayer,0,flParams);
		return mPlayer;		
	}
	
	@Override
	public void setDataSource(VideoInfo video, boolean adPlayed, int pos) {
		// TODO Auto-generated method stub
		mVideo=(SohuVideoInfo) video;		
		try {
			String resolution=mVideo.getResolution();			
			if(mVideo.getUrls().get(resolution).equals(Config.NONE_URL)){
				resolution=getOtherUrl(mVideo);
			}			
			if(resolution==null||resolution.equals(Config.NONE_URL)){
				VodError error=new VodError(VodError.ERROR_PERFIX_SOHU+"002","获取播放地址失败，请检查参数...");
				mListener.onVodError(error);
				return;
			}
			int definition=PlayListManager.getSohuDifinition(resolution);
			if(adPlayed){
				Log.i(TAG," use setDefinition  for skip ad when silo change");
				mPlayer.setDefinition(definition, pos);
			}else{
				Log.i(TAG," setdatasource , position="+pos);
				JSONObject param=new JSONObject();
				param.put("sid",mVideo.getSid());
				param.put("vid",mVideo.getVid());
				param.put("cid",mVideo.getCid());
				param.put("definition",definition);
				param.put("catecode", mVideo.getCateCode());
				param.put("position", String.valueOf(pos));
				mPlayer.setVideoParam(param.toString());
				mPlayer.setPauseADTopMarginPercent(20);
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i(TAG," set data source fatal exception:"+e.getMessage());
		}
	}
	
	private String getOtherUrl(SohuVideoInfo video) {
		// TODO Auto-generated method stub
		int curRes=Integer.parseInt(video.getResolution());
		int value[]={41,31,21,11};
	
		for(int i=0; i<3;i++){
			for(int j=0;j<4;j++){
				Log.i(TAG," value="+video.getUrls().get(String.valueOf(value[j]))+" abs="+Math.abs(curRes-value[j]));
				if(Math.abs(curRes-value[j])==10*(i+1)){
					String s=video.getUrls().get(String.valueOf(value[j]));					
					if(!s.equals(Config.NONE_URL)){
						Log.i(TAG," the new res="+String.valueOf(value[j]));
						video.setResolution(String.valueOf(value[j]));
						return s;
					}
				}
			}			
		}		
		return null;
	}
	
	@Override
	public void OnSeekCompleteListener() {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onSeekComplete();
		}
	}

	@Override
	public void adRemainTime(int time) {
		// TODO Auto-generated method stub
		Log.i(TAG," adRemainTime called time="+time+" mAdStartCalled="+mAdStartCalled+" mAdEndCalled="+mAdEndCalled);
		mRemainTime=time;
		if(time>0&&(!mAdStartCalled)){
			if(mListener!=null){
				mListener.onAdStart();
			}
			Log.i(TAG," onAdStart called");
			mAdStartCalled=true;
			mAdEndCalled=false;
		}
		
		if(time==-1&&(!mAdEndCalled)){
			if(mListener!=null){
				mListener.onAdEnd();
			}
			mAdStartCalled=false;
			mAdEndCalled=true;
			Log.i(TAG," onAdEnd called");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void throwableCallBack(Throwable arg0) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			Log.e(TAG," sohu throwable called  extra="+arg0.getMessage());
			String description=arg0.getMessage();
			String errorCode=VodError.ERROR_PERFIX_SOHU+VodError.ERROR_CODE_SOHU_THROWABLE;			
			mListener.onVodError(new VodError(errorCode,description));			
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onMovieComplete();
		}
	}

	@Override
	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onError(arg1, String.valueOf(arg2));
		}
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer arg0, int what, int extra) {
		// TODO Auto-generated method stub
		Log.i(TAG," onInfo called what="+what+" extra="+extra);
		if(mListener!=null){
			if(what==701){
				mListener.onBufferStart();
			}else if (what==702){
				mListener.onBufferEnd();
			}
		}
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		mVideoWidth=arg0.getVideoWidth();
		mVideoHeight=arg0.getVideoHeight();
		Log.i(TAG," onPrepared called ,mVideoWidth="+mVideoWidth+" mVideoHeight="+mVideoHeight);
		
		if(mListener!=null){
			Log.i(TAG," onPrepared called and isAdPlaying="+mPlayer.isAdPlaying());
			if(!mPlayer.isAdPlaying()){
				mListener.onPrepared();
			}else{
				mPlayer.start();
			}
		}
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}	

	@Override
	public void start() {
		// TODO Auto-generated method stub
		if(mPaused){
			mPlayer.resume();
			mPaused=false;
		}
		mPlayer.start();
	}
	

	@Override
	public void start(int pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub
		if(mPaused){
			mPlayer.resume();
			mPaused=false;
		}
		mPlayer.seekTo(pos);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		mPlayer.pause();
		mPaused=true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mPlayer.stop();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		mPlayer.release();
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return mPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return mPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return mPlayer.isPlaying();
	}
	
	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		return mRemainTime;
	}

	@Override
	public void setDisPlaySize(DisplaySize size) {
		// TODO Auto-generated method stub
		
		if(mScreenWidth*mScreenHeight==0){
			getScreenSize();
		}			
		switch(size){
		case ORIGINAL:
			if(mVideoHeight*mVideoWidth==0){
				Log.i(TAG," can not set to original for mVideoHeight*mVideoWidth=0 ");
			}else{
				int [] a =VideoInfo.calcSizeForAuto(mVideoWidth,mVideoHeight);
				Log.i(TAG,"mVideoWidth= "+a[0]+" mVideoHeight="+a[1]+" after calc");
				FrameLayout.LayoutParams flParams1 = new FrameLayout.LayoutParams(a[0],a[1], Gravity.CENTER);
				mPlayer.setLayoutParams(flParams1);				
				mPlayer.requestLayout();			
			}	
			break;
		case FULL_SCREEN:
			FrameLayout.LayoutParams flParams1 = new FrameLayout.LayoutParams(mScreenWidth,mScreenHeight, Gravity.CENTER);
			mPlayer.setLayoutParams(flParams1);				
			mPlayer.requestLayout();			
			Log.i(TAG," now set size "+mScreenWidth+"*"+mScreenHeight);
			break;
		case FULL_SCREEN_4_3:
			int width= (int)(mScreenHeight*4/3);	
			FrameLayout.LayoutParams flParams2 = new FrameLayout.LayoutParams(width,mScreenHeight, Gravity.CENTER);
			mPlayer.setLayoutParams(flParams2);	
			mPlayer.requestLayout();
			Log.i(TAG," now set size "+width+"*"+mScreenHeight);
			break;				
		}	
	}

	private void getScreenSize() {
		// TODO Auto-generated method stub
		Display display = mActivity.getWindowManager().getDefaultDisplay(); 
		mScreenWidth = display.getWidth();  
		mScreenHeight = display.getHeight();  
		Log.i(TAG," get ScreenSize mScreenWidth="+mScreenWidth+ " mScreenHeight="+mScreenHeight);
	}
	
	@Override
	public void setResolution(String res) {
		// TODO Auto-generated method stub
		int definition=PlayListManager.getSohuDifinition(res);
		int position=mPlayer.getCurrentPosition();
		mPlayer.setDefinition(definition, position);
	}

	@Override
	public void seekWhenPrepared(int headerTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityResume(Activity activity) {
		// TODO Auto-generated method stub
		mPlayer.onActivityResume(activity);
	}

	@Override
	public void onActivityStop(Activity activity) {
		// TODO Auto-generated method stub
		mPlayer.onActivityStop(activity);
	}

	@Override
	public void setDataSource(String url, boolean isFree, int previewTime) {
		// TODO Auto-generated method stub
		
	}
}
