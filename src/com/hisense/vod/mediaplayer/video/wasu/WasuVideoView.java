package com.hisense.vod.mediaplayer.video.wasu;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.wasu.tvplayersdk.player.IMediaListener;
import com.wasu.tvplayersdk.player.PlayerOption;
import com.wasu.tvplayersdk.player.UrlProperty;
import com.wasu.tvplayersdk.player.WasuPlayerView;

public class WasuVideoView implements IPlayController, IMediaListener {
	private static final String TAG="WasuVideoView";
	private WasuPlayerView mPlayer;
	private WasuVideoInfo mVideo;
	private Activity mActivity;
	private IPlayListener mListener;
	private boolean isMovieStarted;
	private int save_pos=-1;
	private int mScreenWidth,mScreenHeight;
	private int mPosition;
	private boolean mAdPlaying=false;
	private int mVideoHeight,mVideoWidth;
	
	public WasuVideoView(){
		
	}

	public WasuPlayerView createPlyaer(Activity context, FrameLayout layout,FrameLayout.LayoutParams flParams, IPlayListener listener){
		mActivity=context;
		mListener=listener;
		PlayerOption option=new PlayerOption();
		mPlayer=new WasuPlayerView(context,option);
		mPlayer.addObserver(this);
		layout.addView(mPlayer,0,flParams);
		return mPlayer;
	}
	
	@Override
	public void setDataSource(VideoInfo video,boolean adPlayed,int startPos) {
		// TODO Auto-generated method stub
		mVideo=(WasuVideoInfo)video;
		String res=mVideo.getResolution();
		String playUrl=mVideo.getUrls().get(res);
		if(playUrl.equals(Config.NONE_URL)){
			playUrl=getOtherUrl(mVideo);
		}
		if(playUrl==null||playUrl.equals(Config.NONE_URL)){
			mListener.onError(-111, "设置播放地址出错，请检查传入参数...");
			return;
		}
		Log.i(TAG," set data called ResourceId="+mVideo.getResourceId()+" ResourceName="+mVideo.getResourceName()+ " price="+mVideo.getPrice()+" nodeid="+mVideo.getNodeId()
				+" url="+playUrl+" position="+startPos);
		UrlProperty urlProperty = new UrlProperty();
		urlProperty.setResourceId(mVideo.getResourceId());
		urlProperty.setResourceName(mVideo.getResourceName());
		urlProperty.setPrice(Integer.parseInt(mVideo.getPrice()));
		urlProperty.setNodeId(mVideo.getNodeId());
		if(adPlayed){
			urlProperty.setAllowForwardAd(false);
		}
		mPlayer.setVideoPath(playUrl, urlProperty);	
		if(startPos>0){
			Log.i(TAG,"setDataSource seekto "+startPos);
			mPlayer.seekTo(startPos);
			Log.i(TAG,"setDataSource start()!!!");
			mPlayer.start();
		}
		isMovieStarted=false;
		save_pos=0;
	}

	private String getOtherUrl(WasuVideoInfo video) {
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
	public void start() {
		// TODO Auto-generated method stub
		mPlayer.start();
	}

	@Override
	public void start(int pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub
		mPlayer.seekTo(pos);
		mPlayer.start();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		mPlayer.pause();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		mPlayer.stopPlayback();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
//		mPlayer.stopPlayback();		
		mPlayer=null;
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
	public void onAdStatusChanged(int status, int extra) {
		// TODO Auto-generated method stub
		Log.i(TAG," onAdStatusChanged called, status="+status+ " extra="+extra);
		if (status == IMediaListener.AD_START){ 
			Log.i(TAG," ad start the save position is "+save_pos);
			if(mListener!=null){
				mListener.onAdStart();
				mAdPlaying=true;
			}
		}else if(status==IMediaListener.AD_END){
			Log.i(TAG," ad end called");
			if(mListener!=null){
				mListener.onAdEnd();
				mAdPlaying=false;
			}
		}else{
			Log.i(TAG," ad other status= "+status);
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onMovieComplete();
		}		
	}

	@Override
	public void onError(MediaPlayer arg0, int what, int extra) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onError( what, String.valueOf(extra));
		}		
	}

	@Override
	public void onInfo(MediaPlayer arg0, int what, int extra) {
		// TODO Auto-generated method stub
		Log.i(TAG," onInfo called what="+what+" extra="+extra);
		if(mListener!=null){
			if(what==701){
				mListener.onBufferStart();
			}else if (what==702){
				mListener.onBufferEnd();
			}else {
				
			}
		}
	}

	@Override
	public void onPause(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepareComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub		
		mVideoWidth=mp.getVideoWidth();
		mVideoHeight=mp.getVideoHeight();
		Log.i(TAG,"onPrepareComplete called and mVideoWidth="+mp.getVideoWidth()+" mVideoHeight="+mp.getVideoHeight());
		if(!mAdPlaying){
			isMovieStarted=true;
		}
		if(mListener!=null){
			mListener.onPrepared();
		}
	}

	@Override
	public void onPreparing(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgress(int progress, int position, int arg2) {
		// TODO Auto-generated method stub
		if(!isMovieStarted){
			Log.i(TAG," progress="+progress+" and saved_progress="+save_pos);
			if(save_pos==-1){
				save_pos=progress;
				return;
			}
			
			if(progress>save_pos&&(progress-save_pos)<2000){
				mListener.onMovieStart();
				isMovieStarted=true;
				save_pos=-1;
			}else{
				save_pos=progress;
			}
		}
	}

	@Override
	public void onResume(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			mListener.onSeekComplete();
		}
		Log.i(TAG," onseekComplete called and width="+mp.getVideoWidth()+" height="+mp.getVideoHeight());
	}

	@Override
	public void onSeeking(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWasuError(int what, String extra) {
		// TODO Auto-generated method stub
		if(mListener!=null){
			Log.e(TAG," onWasuError called, what="+what+" extra="+extra);
			String description=extra;
			String errorCode=VodError.ERROR_PERFIX_WASU+what;
			if(what==-3){
				mVideo.reportVideoPayed(mActivity, 0);
				description="订单未支付或支付失败！";
				mListener.onPayResult(description);
				return;
			}
			mListener.onVodError(new VodError(errorCode,description));
		}
	}

	@Override
	public void onWasuPlayLimit(int arg0, String arg1) {
		// TODO Auto-generated method stub		
		if (arg0 == WASU_PLAY_IN_PREVIEW){
			Log.i(TAG," onWasuPlayLimit called, in preview mode, limitTime="+arg1);
			int limitTime=Integer.valueOf(arg1)*60*1000;
			mListener.onPlayLimitedPreView(limitTime);
		}else if(arg0 == WASU_PLAY_FREE){
			Log.i(TAG," onWasuPlayLimit called, pay complete and report video payed to server");
			mVideo.reportVideoPayed(mActivity, 1);
			mListener.onPlayLimitedPayed();
		}else if(arg0==WASU_PLAY_PAY_CONFIRM){
			Log.i(TAG," onWasuPlayLimit called, exceed the limit time, must pay");
			mListener.onPlayLimitedExceed();
		}
	}

	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		int cnt=(int)((mPlayer.getCurrentADDuration()-mPlayer.getCurrentADPosition())/1000)+1;
		return cnt;
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
		mVideo.setResolution(res);
		mPosition=getCurrentPosition();	
		stop();		
		String playUrl=mVideo.getUrls().get(res);
		Log.i(TAG," set data called ResourceId="+mVideo.getResourceId()+" ResourceName="+mVideo.getResourceName()+ "price="+mVideo.getPrice()+" nodeid="+mVideo.getNodeId()
				+" mPosition="+mPosition+" url="+playUrl);
		UrlProperty urlProperty2 = new UrlProperty();
		urlProperty2.setResourceId(mVideo.getResourceId());
		urlProperty2.setResourceName(mVideo.getResourceName());
		urlProperty2.setPrice(Integer.parseInt(mVideo.getPrice()));
		urlProperty2.setNodeId(mVideo.getNodeId());
		urlProperty2.setAllowForwardAd(false);
		mPlayer.setVideoPath(playUrl, urlProperty2);
		mPlayer.seekTo(mPosition);
		mPlayer.start();
	}

	@Override
	public void seekWhenPrepared(int headerTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResume(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStop(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDataSource(String url, boolean isFree, int previewTime) {
		// TODO Auto-generated method stub
		
	}
}
