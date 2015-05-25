package com.hisense.vod.mediaplayer.video.cntv;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.CNTVLocalServerHelper;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.util.HttpRequest;
import com.hisense.vod.mediaplayer.util.StatusManager;
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.widget.FrameLayout;

public class CNTVVideoView extends MyVideoView implements IPlayController{
	private static final String PLAYCMD = "http://127.0.0.1:8090/Interface.mfg?Cmd=Play&Url=";
	private static final String CLOSECMD = "http://127.0.0.1:8090/Interface.mfg?Cmd=Close";
	private static final String SEEKCMD = "http://127.0.0.1:8090/Interface.mfg?Cmd=Seek";
	private static final String LOGINCMD="http://127.0.0.1:8070/Interface.mfg?Cmd=IDeviceLogin";
	private static final String TAG="CNTVVideoView";
	private Context mContext;
	private CNTVLocalServerHelper local;
	private IPlayListener mListener;
	private int mScreenWidth,mScreenHeight;
	private CNTVVideoInfo info;
	private int position;
	private MediaPlayer mediaPlayer;
	private boolean mSeekWhenPrepared=false;
	private Config mConfig;
	private AsyncTask mSeekTask;
	private int mVideoHeight,mVideoWidth;
	private int mPreviewTime=0;
	private boolean mIsFree=true;
	private boolean mPreviewCompleteCalled=false;
	
	public CNTVVideoView(Context context, IPlayListener listener) {
		super(context);
		mContext=context;
		local=new CNTVLocalServerHelper(context,PlayListManager.getPlatform());	
		mConfig=new Config(context,PlayListManager.getPlatform());
		mListener=listener;
		setOnCompletionListener(completeListener);
		setOnErrorListener(errorListener);
		setOnInfoListener(infoListener);
		setOnPreparedListener(prepareListener);
		setOnVideoSizeChangeListener(sizeListener);
		setOnSeekCompleteListener(seekListener);
	}
	
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
			mVideoHeight=mp.getVideoHeight();
			mVideoWidth=mp.getVideoWidth();
			Log.i(TAG," onPrepared called, mVideoHeight="+mVideoHeight+" mVideoWidth="+mVideoWidth);
			
			if(mSeekWhenPrepared){
				CNTVHttpSeek(String.valueOf(position));
				mSeekWhenPrepared=false;
			}
			
			if(mListener!=null){
				mListener.onPrepared();
			}
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
				mListener.onError(what, String.valueOf(extra));
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
					if(mConfig.ifShowBuffering()){
						mListener.onBufferStart();
					}
				}else if (what==MediaPlayer.MEDIA_INFO_BUFFERING_END){
					if(mConfig.ifShowBuffering()){
						mListener.onBufferEnd();
					}
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
	
	private void CNTVHttpStart(String CNTVUrl){
		new httpStartTask().execute(PLAYCMD+CNTVUrl);
	}
	
	private void  CNTVHttpSeek(String position){
		if(mConfig.isOldVersion()&&mConfig.getPlatform().equals("K681")){
			if(mSeekTask!=null){
				Log.i(TAG," Seek async task is running, cancel for new seek");
				mSeekTask.cancel(true);
			}
		}
		mSeekTask=new httpSeekTask().execute(SEEKCMD,position);
	}
	
	private void  CNTVHttpEnd(){
		new httpEndTask().execute(CLOSECMD);
	}
	
	class httpStartTask extends AsyncTask<String,Integer,String>{
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String result="";
				
			if(mConfig.ifCopyAssets()){
				Log.i(TAG," local init start");
				local.init();
			}
			
			if(!mConfig.isOldVersion()){
				String loginRet = HttpRequest.Get(LOGINCMD);
				
				if(loginRet.equals("111")||loginRet.equals("110")){
					Log.i(TAG," login successed");
					result="success";
				}else{
					Log.e(TAG, " login error and errorCode="+loginRet);
					return "login_error#"+loginRet;
				}
			}
			
			String playUrl=HttpRequest.Get(params[0]);
			if(playUrl.contains("http://127.0.0.1")){          //最新的播控组件会返回 http://127.0.0.1:8090/video.ts?id=xx的形式	
				Log.i(TAG," the latest YstCdnManager, request paly  successed");
				result="success";
				mListener.onAuthCompleted(playUrl);
			}else if(playUrl.equals("0")){                    //老版本的播控组件，其返回结果"0"表示成功，其他失败，其播放的url固定的为http://127.0.0.1:8090/video.ts
				Log.i(TAG," the old YstCdnManager, request paly  successed");
				result="success";
				mListener.onAuthCompleted("http://127.0.0.1:8090/video.ts");
			}else{
				return "play_error#"+playUrl;
			}
			return result;
		}
		
		protected void onPostExecute(String result) {
			String Msg="";
			if(result.equals("success")){
				Log.i(TAG,"CNTV http start and mediaplayer set data source success");
			}else{
				String ret[]=result.split("#");
				if(ret[0].equals("login_error")){
					Msg="认证失败";
				}else{
					Msg="获取播放URL失败";
				}
				Log.e(TAG,"CNTV http login error so quit player");
				StatusManager.setStatus(com.hisense.vod.mediaplayer.util.StatusManager.Status.ERROR);
			
				if(mListener!=null){
					Log.e(TAG," cntv  error called  msg="+Msg+" errorCode="+ret[1]);
					String description=Msg;
					String errorCode=VodError.ERROR_PERFIX_CNTV+ret[1];
					mListener.onVodError(new VodError(errorCode,description));
				}
			}
		}
	}
	
	class httpSeekTask extends AsyncTask<String,Integer,String>{
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret="-1";
			ret = HttpRequest.Get(params[0]);
			if(ret.equals("0")){
				try{
					seekTo(Integer.parseInt(params[1]));
					StatusManager.setStatus(com.hisense.vod.mediaplayer.util.StatusManager.Status.PLAYING);
					start();
				}catch(Exception e){
					Log.e(TAG," CNTVSeekTask error :"+e.getMessage());
				}		
				if(mConfig.isOldVersion()&&mConfig.getPlatform().equals("K681")){
					ret = HttpRequest.Get(params[0]);
					Log.i(TAG," seek http for 1 time and ret="+ret);
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						Log.i(TAG,"seek thread exception:"+e.getMessage());
					}
					ret = HttpRequest.Get(params[0]);
					Log.i(TAG," seek http for 2 time and ret="+ret);
					
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						Log.i(TAG,"seek thread exception:"+e.getMessage());
					}
					ret = HttpRequest.Get(params[0]);
					Log.i(TAG," seek http for 3 time and ret="+ret);
					try{
						Thread.sleep(1000);
					}catch(Exception e){
						Log.i(TAG,"seek thread exception:"+e.getMessage());
					}
					ret = HttpRequest.Get(params[0]);
					Log.i(TAG," seek http for 4 time and ret="+ret);
				}
				return "success";
			}else{
				Log.e(TAG," CNTV http seek error, so seekto "+Integer.parseInt(params[1])+" is not exec");
				return "error";
			}
		}
	}
	
	class httpEndTask extends AsyncTask<String,Integer,String >{
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret= HttpRequest.Get(params[0]);
			if(ret.equals("0")){
				Log.i(TAG," CNTV end task success");
			}else{
				Log.e(TAG," CNTV end task error");
			}
			return null;
		}
	}

	@Override
	public void setDataSource(VideoInfo video, boolean adPlayed, int startPos) {
		// TODO Auto-generated method stub
		info=(CNTVVideoInfo)video;
		String res=info.getResolution();
		String Url=info.getUrls().get(res);
		if(Url.equals(Config.NONE_URL)){
			Url=getOtherUrl(info);
		}
		
		if(Url==null||Url.equals(Config.NONE_URL)){
			mListener.onError(-111, "设置播放地址出错，请检查传入参数...");
			return;
		}
		
		if(startPos>0){
			mSeekWhenPrepared=true;
			position=startPos;
		}
		CNTVHttpStart(Url);
	}

	private String getOtherUrl(CNTVVideoInfo video) {
		// TODO Auto-generated method stub
		int curRes=Integer.parseInt(video.getResolution());
		int value[]={41,31,21,11};
	
		for(int i=0; i<3;i++){
			for(int j=0;j<4;j++){
				if(Math.abs(curRes-value[j])==10*(i+1)){
					String s=video.getUrls().get(String.valueOf(value[j]));
					if(!s.equals(Config.NONE_URL)){
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
		// TODO Auto-generated method stub
	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub
		CNTVHttpSeek(String.valueOf(pos));
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		Log.i(TAG," CNTV video view stop called ");
		stopPlayback();
	}
	
	@Override
	public void release() {
		// TODO Auto-generated method stub
		CNTVHttpEnd();
		this.stopPlayback();
	}

	@Override
	public void setDataSource(String url,boolean isFree,int previewTime) {
		// TODO Auto-generated method stub
		Log.i(TAG," setdataSorce called and url="+url+" isFree="+isFree+" previewTime="+previewTime);
		mIsFree=isFree;
		mPreviewTime=previewTime*1000;
		mPreviewCompleteCalled=false;
		if(!isFree){
			Log.i(TAG," preview mode, called onPreViewInfoReady!!!");
			mListener.onPreviewInfoReady(true,previewTime );
		}
		setVideoPath(url);
	}

	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getCurrentPosition(){
		int position=super.getCurrentPosition();
		if(!mIsFree){
			Log.i(TAG," check if preview Complete position="+position+" previewTime="+mPreviewTime);
			if(position>=mPreviewTime){
				if(!mPreviewCompleteCalled){
					Log.i(TAG," call onPreviewCompleted.");
					mListener.onPreviewCompleted();
					mPreviewCompleteCalled=true;
				}
				pause();
			}
		}
		return position;
	}

	@Override
	public void setDisPlaySize(DisplaySize size) {
		// TODO Auto-generated method stub
		
		if(mScreenWidth*mScreenHeight==0){
			getScreenSize();
		}
		mSize=size;
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
		// TODO Auto-generated method stub
		info.setResolution(res);
		position=getCurrentPosition();
		stop();
		setDataSource(info,true,position);
	}

	@Override
	public void seekWhenPrepared(int arg) {
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
}
