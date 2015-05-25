package com.hisense.vod.mediaplayer.video.letv;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.hisense.vod.mediaplayer.video.sohu.SohuVideoInfo;
import com.letv.livesdk.LetvLiveSdkManager;
import com.letv.livesdk.OnLiveProgramUpdateListener;
import com.letv.livesdk.OnSdkStateChangeListener;
import com.letv.livesdk.PlayResultCallback;

public class LetvVideoView extends VideoView implements IPlayController {
	private static final String TAG="LetvVideoView";
	public static final String STATUS_INVALID="invalid";
	public static final String STATUS_OVER="over";
	public static final String STATUS_PLAYBACK="playback";
	public static final String STATUS_LIVE="live";
	public static final String STATUS_HIGHLIGHT="highlight";
	
	private Context mContext;
	private IPlayListener mListener;
	private int mScreenWidth,mScreenHeight;
	private LetvVideoInfo mVideoInfo;
	private long mStartPlayTime=0;
	private long mBuffreStartTime=0;
	private int mVideoWidth,mVideoHeight;
	private String mVt;
	
	private PlayResultCallback  mPlayCallback=new  PlayResultCallback(){
		@Override
		public void onPlayResult(int errorCode) {
			// TODO 自动生成的方法存根
			VodError error;
			Log.i(TAG, "onPlayResult called and errorCode="+errorCode);
			if(errorCode==PlayResultCallback.PLAY_ERROR_CODE_OK){
				Log.i(TAG," sdk play ok");
				return;
			}else if(errorCode==PlayResultCallback.PLAY_ERROR_CODE_INVALID_STREAM){
				Log.e(TAG, " play error for invalid stream ");
				error=new VodError(VodError.ERROR_PERFIX_LETV+"003", "无效的码率，请检查传入的参数！");
			}else if(errorCode==PlayResultCallback.PLAY_ERROR_CODE_UNAUTHORIZED){
				Log.e(TAG,"  play error for auth failed ");
				error=new VodError(VodError.ERROR_PERFIX_LETV+"001", "鉴权失败，退出播放！");
			}else if(errorCode==PlayResultCallback.PLAY_ERROR_CODE_WRONG_STATUS){
				Log.e(TAG," play error for wrong sdk status");
				error=new VodError(VodError.ERROR_PERFIX_LETV+"002", "状态错误，退出播放！");
			}else{
				error=new VodError(VodError.ERROR_PERFIX_LETV+"004", "未知错误，退出播放！");
				Log.e(TAG," play error for unknown reason");
			}
			mListener.onVodError(error);
		}
	};
	
	private OnLiveProgramUpdateListener mRefreshListener=new OnLiveProgramUpdateListener(){

		@Override
		public void onProgramUpdate() {
			// TODO 自动生成的方法存根
			
			int status=LetvLiveSdkManager.getProgramStatus(mVideoInfo.getLiveId());
			Log.i(TAG," OnLiveProgramUpdateListener called, status="+status);
			switch(status){
			case LetvLiveSdkManager.PROGRAM_INVALID:
				Log.i(TAG,"program is invalid ");
				mListener.onLetvStatusChange(STATUS_INVALID);
				break;
			case LetvLiveSdkManager.PROGRAM_STATUS_OVER:
				Log.i(TAG," program is  over");
				mListener.onLetvStatusChange(STATUS_OVER);
				break;
			case LetvLiveSdkManager.PROGRAM_STATUS_PLAYBACK:
				Log.i(TAG," change program from live to playback");
				String definition=PlayListManager.getLetvDefinition(mVideoInfo.getResolution());
				LetvLiveSdkManager.stopLiveProgram(mVideoInfo.getLiveId());
				LetvLiveSdkManager.playLiveProgram(mVideoInfo.getLiveId(), definition, LetvVideoView.this, mPlayCallback);
				mListener.onLetvStatusChange(STATUS_PLAYBACK);
				break;
			case LetvLiveSdkManager.PROGRAM_STATUS_LIVE:
				Log.i(TAG," live start");
				break;
			case LetvLiveSdkManager.PROGRAM_STATUS_HIGHLIGHTS:
				Log.i(TAG," now play the highlights");
				break;
			}
		}
	};
	
	MediaPlayer.OnVideoSizeChangedListener sizeListener=new MediaPlayer.OnVideoSizeChangedListener() {
		
		@Override
		public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
			// TODO Auto-generated method stub
			Log.i(TAG, " onVideoSizeChange called mp.width="+mp.getVideoWidth()+" mp.height="+mp.getVideoHeight()+" width="+width+" height="+height);
		}
	};
	
	MediaPlayer.OnPreparedListener prepareListener=new MediaPlayer.OnPreparedListener(){

		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			mListener.onPrepared();
			mStartPlayTime=System.currentTimeMillis();
			mVideoHeight=mp.getVideoHeight();
			mVideoWidth=mp.getVideoWidth();
			Log.i(TAG," onPrepared called, mVideoHeight="+mVideoHeight+ " mVideoWidth="+mVideoWidth);
			// LetvLiveSdkMagager.reportPlay(String ac, int ut, String vt, String ref,String st, String lid, String url,String cid);
			LetvLiveSdkManager.reportPlay("play",0,mVt,mVideoInfo.getRef(),mVideoInfo.getLiveId());
		}
	};
	
	MediaPlayer.OnCompletionListener completeListener=new MediaPlayer.OnCompletionListener(){

		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.i(TAG," oncompletion  called");
			if(mListener!=null){
				mListener.onMovieComplete();
			}
		}
	};
	
	MediaPlayer.OnErrorListener  errorListener=new MediaPlayer.OnErrorListener(){
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			Log.i(TAG," onError called what="+what+" extra="+extra);
			if(mListener!=null){
				mListener.onError( what, String.valueOf(extra));
			}
			return false;
		}
	};
	
	MediaPlayer.OnInfoListener infoListener=new MediaPlayer.OnInfoListener(){
		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			Log.i(TAG," onInfo called what="+what+" extra="+extra);
			if(mListener!=null){
				if(what==MediaPlayer.MEDIA_INFO_BUFFERING_START){
					mListener.onBufferStart();
					mBuffreStartTime=System.currentTimeMillis();
				}else if (what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
					mListener.onBufferEnd();
					// LetvLiveSdkMagager.reportPlay(String ac, int ut, String vt, String ref,String st, String lid, String url,String cid);
					LetvLiveSdkManager.reportPlay("block",0,mVt,mVideoInfo.getRef(),mVideoInfo.getLiveId()); 
				}else if(what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
					mListener.onFirstFrameStart();
				}
			}
			return false;
		}
	};
	
	MediaPlayer.OnSeekCompleteListener seekListener=new MediaPlayer.OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(MediaPlayer mp) {
			// TODO Auto-generated method stub
			if(mListener!=null){
				mListener.onSeekComplete();
			}
			Log.i(TAG," onseekComplete called and width="+mp.getVideoWidth()+" height="+mp.getVideoHeight());
		}
	};
	
	public  LetvVideoView(Context context, IPlayListener listener) {
		super(context);
		mContext=context;
		mListener=listener;
		setOnCompletionListener(completeListener);
		setOnErrorListener(errorListener);
		try{
			setOnInfoListener(infoListener);
		}catch(Throwable e){
			Log.i(TAG," sorry, the android version not supprot add infoListener in VideoView");
		}
		setOnPreparedListener(prepareListener);
		LetvLiveSdkManager.addLiveProgramUpdateListener(mRefreshListener);
	}
	
	@Override
	public void setDataSource(VideoInfo video, boolean adPlayed, int pos) {
		// TODO 自动生成的方法存根
		if(video==null){
			VodError error=new VodError(VodError.ERROR_PERFIX_LETV+"006","获取视频信息失败，请检查传入参数...");
			mListener.onVodError(error);
			return;
		}
		mVideoInfo=(LetvVideoInfo)video;
		String resolution=mVideoInfo.getResolution();
		String url=mVideoInfo.getUrls().get(resolution);
		if(url.equals(Config.NONE_URL)){
			resolution=getOtherUrl(mVideoInfo);
		}
		if(resolution==null||resolution.equals(Config.NONE_URL)){
			VodError error=new VodError(VodError.ERROR_PERFIX_LETV+"005","获取播放地址失败，请检查参数...");
			mListener.onVodError(error);
			return;
		}
		String definition=PlayListManager.getLetvDefinition(resolution);
		Log.i(TAG," setdatasource definition for letv="+definition);
		LetvLiveSdkManager.playLiveProgram(mVideoInfo.getLiveId(), definition, this, mPlayCallback);
		mVt=getLetvVt(resolution);
		LetvLiveSdkManager.reportPlay("init",0,mVt,mVideoInfo.getRef(),mVideoInfo.getLiveId());
	}

	private String getOtherUrl(LetvVideoInfo video){
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
	public void start(int pos) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void seek(int pos) {
		// TODO 自动生成的方法存根
		Log.i(TAG," live mode, not support seek");
		mListener.actionNotSupport("seek");
	}
	
	public void pause(){
		Log.i(TAG," live mode, not support pause");
		mListener.actionNotSupport("pause");
	}

	@Override
	public void stop() {
		// TODO 自动生成的方法存根
		stopPlayback();
	}

	@Override
	public void release() {
		// TODO 自动生成的方法存根
		Log.i(TAG," letv video view stop called ");
		LetvLiveSdkManager.reportPlay("time",(int) (System.currentTimeMillis()-mStartPlayTime),mVt,mVideoInfo.getRef(),mVideoInfo.getLiveId());
		LetvLiveSdkManager.reportPlay("end",0,mVt,mVideoInfo.getRef(),mVideoInfo.getLiveId());
		LetvLiveSdkManager.removeLiveProgramUpdateListener(mRefreshListener);
	}
	
	@Override
	public int getAdDownCount() {
		// TODO 自动生成的方法存根
		return 0;
	}

	@Override
	public void setDisPlaySize(DisplaySize size) {
		// TODO 自动生成的方法存根
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
				setLayoutParams(flParams1);
				requestLayout();
			}
			break;
		case FULL_SCREEN:
			FrameLayout.LayoutParams flParams1 = new FrameLayout.LayoutParams(mScreenWidth,mScreenHeight, Gravity.CENTER);
			setLayoutParams(flParams1);
			requestLayout();
			Log.i(TAG," now set size "+mScreenWidth+"*"+mScreenHeight);
			break;
		case FULL_SCREEN_4_3:
			int width= (int)(mScreenHeight*4/3);
			FrameLayout.LayoutParams flParams2 = new FrameLayout.LayoutParams(width,mScreenHeight, Gravity.CENTER);
			setLayoutParams(flParams2);
			requestLayout();
			Log.i(TAG," now set size "+width+"*"+mScreenHeight);
			break;
		}
	}
	
	private void getScreenSize() {
		// TODO Auto-generated method stub
		Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
		mScreenWidth = display.getWidth();
		mScreenHeight = display.getHeight();
		Log.i(TAG," get ScreenSize mScreenWidth="+mScreenWidth+ " mScreenHeight="+mScreenHeight);
	}

	@Override
	public void setResolution(String res) {
		// TODO 自动生成的方法存根
		String definition=PlayListManager.getLetvDefinition(res);
		Log.i(TAG," setResolution called and definition="+definition);
		LetvLiveSdkManager.stopLiveProgram(mVideoInfo.getLiveId());
		LetvLiveSdkManager.playLiveProgram(mVideoInfo.getLiveId(), definition, this, mPlayCallback);
		Toast.makeText(mContext, "清晰度为："+definition,Toast.LENGTH_LONG).show();
		mVt=getLetvVt(res);
	}

	private String getLetvVt(String res){
		if(res.equals("41")){
			return "1080p";
		}else if(res.equals("31")){
			return "720p";
		}else if(res.equals("21")){
			return "1300";
		}else{
			return "1000";
		}
	}
	
	@Override
	public void seekWhenPrepared(int mHeaderTime) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onActivityResume(Activity activity) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onActivityStop(Activity activity) {
		// TODO 自动生成的方法存根
	}

	@Override
	public void setDataSource(String url, boolean isFree, int previewTime) {
		// TODO Auto-generated method stub
		
	}
}
