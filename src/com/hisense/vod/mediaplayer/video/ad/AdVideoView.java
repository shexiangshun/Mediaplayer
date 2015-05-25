package com.hisense.vod.mediaplayer.video.ad;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.hisense.vod.mediaplayer.video.cntv.MyVideoView;

public class AdVideoView extends MyVideoView implements IPlayController {
	private static final String TAG="AdVideoView";
	private static final int UNKNOWN=0;
	private static final int PREPARING=1;
	private static final int PLAYING=2;
	private static final int COMPLETED=3;
	
	private Context mContext;
	private AdVideoInfo mAdInfo;
	private IPlayListener mListener;
	private int mStatus=UNKNOWN;
	private long mStartTime=0;
	private int mLeftTime=-1;
	private int mDuration=-1;
	private AsyncTask mTask;
	
	public AdVideoView(Context context, IPlayListener listener) {
		super(context);		
		mContext=context;			
		mListener=listener;		
		setOnCompletionListener(completeListener);
		setOnErrorListener(errorListener);
		setOnInfoListener(infoListener);		
		setOnPreparedListener(prepareListener);		
	}
	
	MediaPlayer.OnPreparedListener prepareListener=new MediaPlayer.OnPreparedListener(){
		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.i(TAG," onPrepared called!!!!");			
			if(mListener!=null){
				mListener.onAdStart();
				mListener.onPrepared();
				mStatus=PLAYING;
				mDuration=AdVideoView.this.getDuration();				
			}
		}		
	};
	
	MediaPlayer.OnCompletionListener completeListener=new MediaPlayer.OnCompletionListener(){

		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Log.i(TAG," oncompletion  called");		
			adEnd();
		}	
	};
	
	MediaPlayer.OnErrorListener  errorListener=new MediaPlayer.OnErrorListener(){
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			Log.i(TAG,"skip AD for onError called what="+what+" extra="+extra);
			adEnd();
			return false;
		}	
	};
	
	MediaPlayer.OnInfoListener infoListener=new MediaPlayer.OnInfoListener(){

		@Override
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
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
	};	

	private void adEnd(){
		Log.i(TAG,"adEnd called");
		mListener.onAdEnd();
		if(mTask!=null){
			mTask.cancel(true);
			mTask=null;
		}
		mStatus=COMPLETED;
		mListener.onMovieComplete();
	}
	
	@Override
	public void setDataSource(VideoInfo video, boolean adPlayed, int pos) {
		// TODO Auto-generated method stub		
		String res=video.getResolution();
		String Url=video.getUrls().get(res);
		
		mAdInfo=(AdVideoInfo)video;
		Log.i(TAG," setdatasource called and url="+Url);
		setVideoPath(Url);
		mStartTime=System.currentTimeMillis();
		mStatus=PREPARING;
		mTask=new checkTask().execute("fffff");			
	}

	class checkTask extends AsyncTask<String,Integer,String>{		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub			
			String result="";		
			for(;;){
				long now=System.currentTimeMillis();
				Log.i(TAG," AD has eslape "+(now-mStartTime)+"ms");
				if(now-mStartTime>mAdInfo.getTimeout()*1000){
					return "timeout";
				}
				if(mStatus==PLAYING){
					mLeftTime=mDuration-AdVideoView.this.getCurrentPosition();
					Log.i(TAG,"mLeftTime="+mLeftTime+" currentpos="+AdVideoView.this.getCurrentPosition());
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}				
			}		
		}		
		
		protected void onPostExecute(String result) {
			if(result.equals("timeout")){
				Log.i(TAG," AD end for timeout");
				adEnd();
			}
		}
	}
	
	@Override
	public void start(int pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		stopPlayback();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		stopPlayback();
	}



	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		if(mLeftTime<0){
			return 0;
		}else{
			return mLeftTime/1000;
		}
	}

	@Override
	public void setDisPlaySize(DisplaySize size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResolution(String res) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void setDataSource(String url, boolean isFree, int previewTime) {
		// TODO Auto-generated method stub
		
	}
}
