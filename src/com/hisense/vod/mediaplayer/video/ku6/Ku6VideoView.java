package com.hisense.vod.mediaplayer.video.ku6;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.hisense.vod.mediaplayer.video.cntv.MyVideoView;

public class Ku6VideoView extends MyVideoView implements IPlayController {
	private static final String TAG="Ku6VideoView";
	private Context mContext;

	private IPlayListener mListener;
	private int mScreenWidth,mScreenHeight;
	private int position;	
	private MediaPlayer mediaPlayer;
	private boolean mSeekWhenPrepared=false;	
	
	public Ku6VideoView(Context context, IPlayListener listener) {
		super(context);		
		mContext=context;			
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
				if(what==701){
					mListener.onBufferStart();
				}else if (what==702){
					mListener.onBufferEnd();
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

	@Override
	public void setDataSource(VideoInfo video, boolean adPlayed, int pos) {
		// TODO Auto-generated method stub
		String res=video.getResolution();
		String Url=video.getUrls().get(res);
		this.setVideoPath(Url);
	}

	@Override
	public void start(int pos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub
		seekTo(pos);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		stopPlayback();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		return 0;
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
